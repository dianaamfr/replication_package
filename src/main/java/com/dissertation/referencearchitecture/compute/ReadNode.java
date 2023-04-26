package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.ROTRequest;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.ROTResponse.Builder;
import com.dissertation.referencearchitecture.ROTServiceGrpc.ROTServiceImplBase;
import com.dissertation.referencearchitecture.compute.storage.ReaderStorage;
import com.dissertation.referencearchitecture.compute.storage.StoragePuller;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import software.amazon.awssdk.regions.Region;

public class ReadNode extends ComputeNode {
    private ReaderStorage storage;
    private long rotId;
    private static final String USAGE = "Usage: ReadNode <port:Int> (<partitionId:Int>)+";
    private final String region;

    public ReadNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, ReaderStorage storage, Region region)
            throws URISyntaxException {
        super(scheduler, s3Helper, String.format("%s-%s", Utils.READ_NODE_ID, region.toString()));
        this.storage = storage;
        this.rotId = 1;
        this.region = region.toString();
    }

    @Override
    public void init(Server server) throws IOException, InterruptedException {
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.storage, this.s3Helper, this.s3Logs, this.region),
                Utils.PULL_DELAY,
                Utils.PULL_DELAY, TimeUnit.MILLISECONDS);
        super.init(server);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(USAGE);
            return;
        }

        try {
            Set<Integer> partitionIds = new HashSet<>();
            int port = Integer.valueOf(args[0]);
            for (int i = 1; i < args.length; i++) {
                partitionIds.add(Integer.valueOf(args[i]));
            }
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            Region region = Utils.getCurrentRegion();
            S3Helper s3Helper = new S3Helper(region);
            ReaderStorage storage = new ReaderStorage();
            storage.init(partitionIds);

            ReadNode readNode = new ReadNode(scheduler, s3Helper, storage, region);
            ROTServiceImpl readService = readNode.new ROTServiceImpl();
            Server server = ServerBuilder.forPort(port).addService(readService).build();
            readNode.init(server);
        } catch (URISyntaxException e) {
            System.err.println("Could not connect with AWS S3");
        } catch (IOException e) {
            System.err.println("Could not start server");
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public class ROTServiceImpl extends ROTServiceImplBase {
        @Override
        public void rot(ROTRequest request, StreamObserver<ROTResponse> responseObserver) {
            String stableTime = storage.getStableTime();
            Builder responseBuilder = ROTResponse.newBuilder().setId(rotId).setError(false);
            Map<String, ByteString> values = new HashMap<>(request.getKeysCount());

            for (String key : request.getKeysList()) {
                try {
                    ByteString value = storage.get(key, stableTime).getValue();
                    values.put(key, value);
                } catch (KeyNotFoundException | KeyVersionNotFoundException e) {
                    values.put(key, ByteString.EMPTY);
                } catch (Exception e) {
                    responseBuilder.setStatus(e.toString());
                    responseBuilder.setError(true);
                }
            }

            if (!responseBuilder.getError()) {
                responseBuilder
                        .setStableTime(stableTime)
                        .putAllValues(values);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            rotId++;
        }
    }
}
