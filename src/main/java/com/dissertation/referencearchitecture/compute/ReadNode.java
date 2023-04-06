package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.compute.service.ROTServiceImpl;
import com.dissertation.referencearchitecture.compute.storage.ReaderStorage;
import com.dissertation.referencearchitecture.compute.storage.StoragePuller;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
import com.dissertation.referencearchitecture.remoteInterface.ReadRemoteInterface;
import com.dissertation.referencearchitecture.remoteInterface.response.ROTError;
import com.dissertation.referencearchitecture.remoteInterface.response.ROTResponse;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import software.amazon.awssdk.regions.Region;

public class ReadNode extends ComputeNode implements ReadRemoteInterface {
    private ReaderStorage storage; 
 
    public ReadNode(Server server, ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, Region region, ReaderStorage storage) throws URISyntaxException {
        super(server, scheduler, s3Helper, String.format("r%s", region.toString()));
        this.storage = storage;
    }

    @Override
    public void init() throws IOException, InterruptedException {
        super.init();
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.storage, this.s3Helper), Utils.PULL_DELAY, Utils.PULL_DELAY, TimeUnit.MILLISECONDS);
    }
    
    public static void main(String[] args) {
        Region region;
        if(args.length < 1) {
            region = Utils.getCurrentRegion();
        } else {
            region = Region.of(args[0]);
        }
        
        if(!Config.isRegion(region)) {
            System.err.println("Error: Invalid region");   
            return;
        }
  
        try {
            int port = Integer.valueOf(System.getProperty("serverPort"));
            Server server = ServerBuilder.forPort(port).addService(new ROTServiceImpl()).build();

            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            S3Helper s3Helper = new S3Helper(region);
            ReaderStorage storage = new ReaderStorage(region);
            storage.init();

            ReadNode readNode = new ReadNode(server, scheduler, s3Helper, region, storage);
            
            // Bind the remote object's stub in the registry
            ReadRemoteInterface stub = (ReadRemoteInterface) UnicastRemoteObject.exportObject(readNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(readNode.id, stub);

            readNode.init();
        } catch (URISyntaxException e) {
            System.err.println("Could not connect with AWS S3");
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ROTResponse rot(Set<String> readSet) {
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
    }
}
