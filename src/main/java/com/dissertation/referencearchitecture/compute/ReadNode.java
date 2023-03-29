package com.dissertation.referencearchitecture.compute;

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

import com.dissertation.referencearchitecture.compute.storage.ReaderStorage;
import com.dissertation.referencearchitecture.compute.storage.StoragePuller;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.exceptions.KeyVersionNotFoundException;
import com.dissertation.referencearchitecture.remoteInterface.ROTResponse;
import com.dissertation.referencearchitecture.remoteInterface.ReadRemoteInterface;
import com.dissertation.referencearchitecture.s3.S3Helper;

public class ReadNode extends ComputeNode implements ReadRemoteInterface {
    private ReaderStorage storage; 
    public static final int PUSH_DELAY = 10000;
    
    public ReadNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String region, ReaderStorage storage) throws URISyntaxException {
        super(scheduler, s3Helper, String.format("r%s", region));
        this.storage = storage;
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.storage, this.s3Helper), PUSH_DELAY, PUSH_DELAY, TimeUnit.MILLISECONDS);
    }
    
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: java ReadNode <region:String>");   
            return;
        }

        String region = args[0];
        if(!Config.isRegion(region)) {
            System.err.println("Error: Invalid Region");   
            return;
        }
        
        try {
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            S3Helper s3Helper = new S3Helper();
            ReaderStorage storage = new ReaderStorage(region);
            storage.init();

            ReadNode readNode = new ReadNode(scheduler, s3Helper, region, storage);
            
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
            } catch (KeyNotFoundException e) {
                values.put(key, new byte[0]);
            } catch (KeyVersionNotFoundException e) {
                values.put(key, new byte[0]);
            }    
        }
        return new ROTResponse(values, stableTime);
    }
}
