package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.StableTimeServiceGrpc;
import com.dissertation.referencearchitecture.WriteRequest;
import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.referencearchitecture.WriteResponse.Builder;
import com.dissertation.referencearchitecture.WriteServiceGrpc.WriteServiceImplBase;
import com.dissertation.referencearchitecture.compute.clock.HLCState;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.TimeProvider;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.compute.storage.WriterStorage;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.WriteRequestLog;
import com.dissertation.validation.logs.WriteResponseLog;
import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import software.amazon.awssdk.regions.Region;

public class WriteNode extends ComputeNode {
    private final int partition;
    private final WriterStorage storage;
    private final HLC hlc;
    private final String region;
    private final Lock writeLock;
    private final List<StableTimeServiceGrpc.StableTimeServiceBlockingStub> stableTimeStubs;
    private AtomicInteger stableTimeCounter;
    private static final String USAGE = "Usage: WriteNode <partition> <port> (<readPort> <readIp>)+";

    public WriteNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, int partition, WriterStorage storage,
            HLC hlc, Region region, List<StableTimeServiceGrpc.StableTimeServiceBlockingStub> stableTimeStubs)
            throws URISyntaxException, IOException, InterruptedException {
        super(scheduler, s3Helper, String.format("%s-%d", Utils.WRITE_NODE_ID, partition));
        this.partition = partition;
        this.region = region.toString();
        this.storage = storage;
        this.hlc = hlc;
        this.writeLock = new ReentrantLock();
        this.stableTimeStubs = stableTimeStubs;
        this.stableTimeCounter = new AtomicInteger(Utils.CHECKPOINT_FREQUENCY);
    }

    @Override
    public void init(Server server) throws IOException, InterruptedException {
        this.scheduler.scheduleWithFixedDelay(
                new StoragePusher(this.s3Helper, this.storage, this.hlc, this.partition, this.region,
                        this.stableTimeStubs, stableTimeCounter, this.s3Logs),
                Utils.PUSH_DELAY,
                Utils.PUSH_DELAY, TimeUnit.MILLISECONDS);
        super.init(server);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(USAGE);
            return;
        }

        try {
            int partition = Integer.parseInt(args[0]);
            int port = Integer.parseInt(args[1]);
            final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(3);
            final Region region = Utils.getCurrentRegion();
            final S3Helper s3Helper = new S3Helper(region);
            final WriterStorage storage = new WriterStorage();
            final TimeProvider timeProvider = new TimeProvider(scheduler, Utils.CLOCK_DELAY);
            final HLC hlc = new HLC(timeProvider);

            // Connect with readNodes
            final List<StableTimeServiceGrpc.StableTimeServiceBlockingStub> stableTimeStubs = new ArrayList<>();
            for (int i = 2; i < args.length; i += 2) {
                final Address readAddress = new Address(Integer.parseInt(args[i]), args[i + 1]);
                stableTimeStubs.add(StableTimeServiceGrpc.newBlockingStub(readAddress.getChannel()));
            }

            final WriteNode writeNode = new WriteNode(scheduler, s3Helper, partition, storage, hlc, region,
                    stableTimeStubs);

            final WriteServiceImpl writeService = writeNode.new WriteServiceImpl();
            final Server server = ServerBuilder.forPort(port).addService(writeService).build();
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
            long t1 = System.currentTimeMillis();
            HLCState lastTime = new HLCState();
            Builder responseBuilder = WriteResponse.newBuilder().setError(false);

            if (Utils.getKeyPartitionId(request.getKey()) != partition) {
                responseBuilder.setStatus(String.format("Key %s not found", request.getKey()))
                        .setError(true);
            } else {
                try {
                    lastTime = HLCState.fromRecvTimestamp(request.getLastWriteTimestamp());
                } catch (InvalidTimestampException e) {
                    responseBuilder.setStatus(e.toString()).setError(true);
                }
            }

            if (!responseBuilder.getError()) {
                writeLock.lock();
                String writeTimestamp = performWrite(lastTime, request);
                writeLock.unlock();
                responseBuilder.setWriteTimestamp(writeTimestamp);
                long t2 = System.currentTimeMillis();

                if (Utils.VISIBILITY_LOGS) {
                    logs.add(new WriteRequestLog(request.getKey(), partition, t1));
                    logs.add(new WriteResponseLog(request.getKey(), partition, writeTimestamp, t2));
                }
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }

        @Override
        public void atomicWrite(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
            HLCState lastTime = new HLCState();
            Builder responseBuilder = WriteResponse.newBuilder().setError(false);

            if (Utils.getKeyPartitionId(request.getKey()) != partition) {
                responseBuilder.setStatus(String.format("Key %s not found", request.getKey()))
                        .setError(true);
            } else if (!request.hasExpectedValue() && !request.hasExpectedVersion()) {
                responseBuilder.setStatus("No expected value or version provided.")
                        .setError(true);
            } else {
                try {
                    lastTime = HLCState.fromRecvTimestamp(request.getLastWriteTimestamp());
                } catch (InvalidTimestampException e) {
                    responseBuilder.setStatus(e.toString()).setError(true);
                }
            }

            if (!responseBuilder.getError()) {
                writeLock.lock();
                Entry<String, ByteString> lastVersion = storage.getLastVersion(request.getKey());
                if (isExpectedVersion(lastVersion, request)) {
                    String writeTimestamp = performWrite(lastTime, request);
                    writeLock.unlock();
                    responseBuilder.setWriteTimestamp(writeTimestamp);
                } else {
                    writeLock.unlock();
                    KeyVersion currentVersion = KeyVersion.newBuilder().setTimestamp(lastVersion.getKey())
                            .setValue(lastVersion.getValue()).build();
                    responseBuilder
                            .setStatus("Write failed. Current version doesn't match.")
                            .setError(true)
                            .setCurrentVersion(currentVersion);
                }
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
    }

    private String performWrite(HLCState lastTime, WriteRequest request) {
        HLCState writeTime = this.hlc.startWrite(lastTime);
        String writeTimestamp = writeTime.toString();
        this.storage.put(request.getKey(), writeTimestamp, request.getValue());
        this.hlc.endWrite(HLCState.fromLastWriteTimestamp(writeTimestamp));
        return writeTimestamp;
    }

    private boolean isExpectedVersion(Entry<String, ByteString> lastVersion, WriteRequest request) {
        if (request.hasExpectedVersion() && request.hasExpectedValue()) {
            return isExpectedVersion(lastVersion, request.getExpectedVersion(), request.getExpectedValue());
        }
        return request.hasExpectedVersion() ? this.isExpectedVersion(lastVersion, request.getExpectedVersion())
                : this.isExpectedValue(lastVersion, request.getExpectedValue());
    }

    private boolean isExpectedVersion(Entry<String, ByteString> lastVersion, String expectedVersion,
            ByteString expectedValue) {
        return lastVersion.getKey().equals(expectedVersion) && lastVersion.getValue().equals(expectedValue);
    }

    private boolean isExpectedVersion(Entry<String, ByteString> lastVersion, String expectedVersion) {
        return lastVersion.getKey().equals(expectedVersion);
    }

    private boolean isExpectedValue(Entry<String, ByteString> lastVersion, ByteString expectedValue) {
        return lastVersion.getValue().equals(expectedValue);
    }
}