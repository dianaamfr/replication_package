package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.WriteRequest;
import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.referencearchitecture.WriteResponse.Builder;
import com.dissertation.referencearchitecture.WriteServiceGrpc.WriteServiceImplBase;
import com.dissertation.referencearchitecture.compute.clock.ClockState;
import com.dissertation.referencearchitecture.compute.clock.TimeProvider;
import com.dissertation.referencearchitecture.compute.clock.ClockState.State;
import com.dissertation.referencearchitecture.compute.storage.Storage;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;
import com.dissertation.utils.record.WriteRecord;
import com.dissertation.utils.record.Record.NodeType;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class WriteNode extends ComputeNode {
    private Storage storage;
    private HLC hlc;
    private int partition;
    private static final String USAGE = "Usage: WriteNode <partition> <port>";

    public WriteNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, int partition, Storage storage,
            HLC hlc) throws URISyntaxException, IOException, InterruptedException {
        super(scheduler, s3Helper, String.format("%s-%d", Utils.WRITE_NODE_ID, partition));
        this.partition = partition;
        this.storage = storage;
        this.hlc = hlc;
    }

    @Override
    public void init(Server server) throws IOException, InterruptedException {
        this.scheduler.scheduleWithFixedDelay(new StoragePusher(this.hlc, this.storage, this.s3Helper, this.partition, this.id, this.logs), Utils.PUSH_DELAY,
        Utils.PUSH_DELAY, TimeUnit.MILLISECONDS);
        super.init(server);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(USAGE);
            return;
        }

        try {
            int partition = Integer.valueOf(args[0]);
            int port = Integer.valueOf(args[1]);
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);
            S3Helper s3Helper = new S3Helper(Utils.getCurrentRegion());
            Storage storage = new Storage();
            HLC hlc = new HLC(new TimeProvider(scheduler, Utils.CLOCK_DELAY));

            WriteNode writeNode = new WriteNode(scheduler, s3Helper, partition, storage, hlc);
            WriteServiceImpl writeService = writeNode.new WriteServiceImpl();
            Server server = ServerBuilder.forPort(port).addService(writeService).build();
            writeNode.init(server);
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

    public class WriteServiceImpl extends WriteServiceImplBase {
        @Override
        public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
            ClockState lastTime = new ClockState();
            ClockState writeTime = new ClockState();
            Builder responseBuilder = WriteResponse.newBuilder().setError(false);

            if (Utils.getKeyPartitionId(request.getKey()) != partition) {
                responseBuilder
                        .setStatus(String.format("Key %s not found", request.getKey()))
                        .setError(true);
            } else {
                try {
                    lastTime = ClockState.fromString(request.getLastWriteTimestamp(), State.WRITE);
                } catch (InvalidTimestampException e) {
                    responseBuilder
                            .setStatus(e.toString())
                            .setError(true);
                }
            }

            if (!responseBuilder.getError()) {
                writeTime = hlc.writeEvent(lastTime);
                storage.put(request.getKey(), writeTime.toString(), request.getValue());
                responseBuilder.setWriteTimestamp(writeTime.toString());
                hlc.writeComplete();
                hlc.setSafePushTime(writeTime);

                logs.add(new WriteRecord(
                    NodeType.WRITER,
                    id,
                    writeTime.toString(), 
                    request.getKey(), 
                    partition,
                    false));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
    }
}