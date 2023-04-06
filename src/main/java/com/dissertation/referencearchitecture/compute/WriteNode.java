package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.compute.clock.ClockSyncHandler;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.WriteRequest;
import com.dissertation.WriteResponse;
import com.dissertation.WriteServiceGrpc.WriteServiceImplBase;
import com.dissertation.referencearchitecture.compute.clock.ClockState;
import com.dissertation.referencearchitecture.compute.clock.TimeProvider;
import com.dissertation.referencearchitecture.compute.clock.ClockState.State;
import com.dissertation.referencearchitecture.compute.storage.Storage;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class WriteNode extends ComputeNode {
    private Storage storage;  
    private StoragePusher storagePusher;
    private HLC hlc;
    private int partition;

    public WriteNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, int partition, Storage storage, StoragePusher storagePusher, HLC hlc) throws URISyntaxException, IOException, InterruptedException {
        super(scheduler, s3Helper, String.format("w%s", partition));
        this.partition = partition;
        this.storage = storage;        
        this.storagePusher = storagePusher;
        this.hlc = hlc;
    }

    @Override
    public void init(Server server) throws IOException, InterruptedException {
        super.init(server);
        this.scheduler.scheduleWithFixedDelay(new ClockSyncHandler(this.hlc, this.s3Helper, this.storagePusher), Utils.SYNC_DELAY, Utils.SYNC_DELAY, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Usage: java WriteNode <partitionId:Int> <port:Int>");   
            return;
        }

        try {
            int partition = Integer.valueOf(args[0]);
            int port = Integer.valueOf(args[1]);
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);
            S3Helper s3Helper = new S3Helper(Utils.getCurrentRegion());
            Storage storage = new Storage();
            StoragePusher storagePusher = new StoragePusher(storage, s3Helper, partition);
            HLC hlc = new HLC(new TimeProvider(scheduler, Utils.CLOCK_DELAY));

            WriteNode writeNode = new WriteNode(scheduler, s3Helper, partition, storage, storagePusher, hlc);
            WriteServiceImpl writeService = writeNode.new WriteServiceImpl();
            Server server = ServerBuilder.forPort(port).addService(writeService).build();
            writeNode.init(server);
        } catch (URISyntaxException e) {
            System.err.println("Could not connect with AWS S3");
        } catch (IOException e) {
            System.err.println("Could not start server");
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number");
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public class WriteServiceImpl extends WriteServiceImplBase {
        @Override
        public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
            System.out.println(request.getKey());
            System.out.println(request.getValue());
            System.out.println(request.getLastWriteTimestamp());

            WriteResponse response = WriteResponse.newBuilder().build();
           
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    

    // public WriteResponse write(String key, byte[] value, String lastWriteTimestamp) {  
    //     if(Utils.getKeyPartitionId(key) != this.partition) {
    //         return new WriteError(String.format("Key %s not found", key));
    //     }

    //     ClockState lastTimestamp;
    //     try {
    //         lastTimestamp = ClockState.fromString(lastWriteTimestamp, State.WRITE);
    //     } catch (InvalidTimestampException e) {
    //         return new WriteError(e.toString());
    //     }

    //     String writeTimestamp = Utils.MIN_TIMESTAMP;
    //     WriteResponse writeResponse;
    //     try {
    //         writeTimestamp = this.hlc.writeEvent(lastTimestamp).toString();
    //         this.storage.put(key, writeTimestamp, value);
    //         if(this.storagePusher.push(writeTimestamp)) {
    //             writeResponse = new WriteResponse(writeTimestamp);
    //         } else {
    //             this.storage.delete(key, writeTimestamp);
    //             writeResponse = new WriteError("Write to data store failed");
    //         }
    //     } catch (KeyNotFoundException e) {
    //         writeResponse = new WriteError(String.format("Key %s not found", key));
    //     } catch(Exception e) {
    //         writeResponse = new WriteError(e.toString());
    //     }

    //     this.hlc.writeComplete();
    //     return writeResponse;
    // }
}