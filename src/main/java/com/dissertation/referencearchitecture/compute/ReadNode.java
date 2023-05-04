package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTRequest;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.ROTResponse.Builder;
import com.dissertation.referencearchitecture.ROTServiceGrpc.ROTServiceImplBase;
import com.dissertation.referencearchitecture.StableTimeServiceGrpc.StableTimeServiceImplBase;
import com.dissertation.referencearchitecture.StableTimeRequest;
import com.dissertation.referencearchitecture.StableTimeResponse;
import com.dissertation.referencearchitecture.compute.storage.ReaderStorage;
import com.dissertation.referencearchitecture.compute.storage.StoragePuller;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import software.amazon.awssdk.regions.Region;

public class ReadNode extends ComputeNode {
    private final String region;
    private final ReaderStorage storage;
    private final AtomicLong rotCounter;
    private static final String USAGE = "Usage: ReadNode <port:Int> (<partitionId:Int>)+";

    public ReadNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, ReaderStorage storage, Region region)
            throws URISyntaxException {
        super(scheduler, s3Helper, String.format("%s-%s", Utils.READ_NODE_ID, region.toString()));
        this.region = region.toString();
        this.storage = storage;
        this.rotCounter = new AtomicLong(0);
    }

    @Override
    public void init(Server server) throws IOException, InterruptedException {
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.s3Helper, this.storage, this.region, this.s3Logs),
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
            int port = Integer.parseInt(args[0]);
            for (int i = 1; i < args.length; i++) {
                partitionIds.add(Integer.parseInt(args[i]));
            }

            final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            final Region region = Utils.getCurrentRegion();
            final S3Helper s3Helper = new S3Helper(region);
            final ReaderStorage storage = new ReaderStorage();
            storage.init(partitionIds);

            final ReadNode readNode = new ReadNode(scheduler, s3Helper, storage, region);
            final ROTServiceImpl readService = readNode.new ROTServiceImpl();
            final StableTimeServiceImpl stableTimeService = readNode.new StableTimeServiceImpl();
            final Server server = ServerBuilder.forPort(port).addService(readService).addService(stableTimeService)
                    .build();
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
            // Define snapshot
            final long rotId = rotCounter.incrementAndGet();
            final String rotSnapshot = storage.getStableTime();

            // Get values within the snapshot
            Builder responseBuilder = ROTResponse.newBuilder().setId(rotId).setError(false);
            Map<String, KeyVersion> values = new HashMap<>(request.getKeysCount());
            for (String key : request.getKeysList()) {
                try {
                    Entry<String, ByteString> versionEntry = storage.get(key, rotSnapshot);
                    KeyVersion version = KeyVersion.newBuilder().setTimestamp(versionEntry.getKey())
                            .setValue(versionEntry.getValue()).build();
                    values.put(key, version);
                } catch (Exception e) {
                    responseBuilder.setStatus(e.toString());
                    responseBuilder.setError(true);
                }
            }

            if (!responseBuilder.getError()) {
                responseBuilder.setStableTime(rotSnapshot).putAllVersions(values);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
    }

    public class StableTimeServiceImpl extends StableTimeServiceImplBase {
        @Override
        public void stableTime(StableTimeRequest request, StreamObserver<StableTimeResponse> responseObserver) {
            responseObserver.onNext(StableTimeResponse.newBuilder().setStableTime(storage.getStableTime()).build());
            responseObserver.onCompleted();
        }
    }

}
