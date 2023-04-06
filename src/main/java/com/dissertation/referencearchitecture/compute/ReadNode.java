package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.ROTRequest;
import com.dissertation.ROTResponse;
import com.dissertation.ROTServiceGrpc.ROTServiceImplBase;
import com.dissertation.referencearchitecture.compute.storage.ReaderStorage;
import com.dissertation.referencearchitecture.compute.storage.StoragePuller;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import software.amazon.awssdk.regions.Region;

public class ReadNode extends ComputeNode {
    private ReaderStorage storage; 
 
    public ReadNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, Region region, ReaderStorage storage) throws URISyntaxException {
        super(scheduler, s3Helper, String.format("r%s", region.toString()));
        this.storage = storage;
    }

    @Override
    public void init(Server server) throws IOException, InterruptedException {
        super.init(server);
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.storage, this.s3Helper), Utils.PULL_DELAY, Utils.PULL_DELAY, TimeUnit.MILLISECONDS);
    }
    
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: ReadNode <port:Int>");
            return;
        } 

        Region region = Utils.getCurrentRegion();
        // if(!Config.isRegion(region)) {
        //     System.err.println("Invalid region");   
        //     return;
        // }
  
        try {
            int port = Integer.valueOf(args[0]);
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            S3Helper s3Helper = new S3Helper(region);
            ReaderStorage storage = new ReaderStorage(region);
            storage.init();

            ReadNode readNode = new ReadNode(scheduler, s3Helper, region, storage);
            ROTServiceImpl readService = readNode.new ROTServiceImpl();
            Server server = ServerBuilder.forPort(port).addService(readService).build();
            readNode.init(server);
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

    public class ROTServiceImpl extends ROTServiceImplBase {
        @Override
        public void rot(ROTRequest request, StreamObserver<ROTResponse> responseObserver) {
            System.out.println(request.getKeysList());
            ROTResponse response = ROTResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }   

/*     public ROTResponse rot(Set<String> readSet) {
        Map<String, byte[]> values = new HashMap<>(readSet.size());
        String stableTime = this.storage.getStableTime();

        for (String key: readSet) {
            try {
                byte[] value = this.storage.get(key, stableTime).getValue();
                values.put(key, value);
            } catch (KeyNotFoundException | KeyVersionNotFoundException e) {
                values.put(key, new byte[0]);
            } catch (Exception e) {
                return new ROTError(e.toString());
            }    
        }
        return new ROTResponse(values, stableTime);
    } */
}
