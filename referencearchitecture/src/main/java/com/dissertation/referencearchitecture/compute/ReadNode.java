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

import com.dissertation.referencearchitecture.compute.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.compute.exceptions.KeyVersionNotFoundException;
import com.dissertation.referencearchitecture.compute.storage.ReaderStorage;
import com.dissertation.referencearchitecture.compute.storage.StoragePuller;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.remoteInterface.ROTResponse;
import com.dissertation.referencearchitecture.remoteInterface.ReadRemoteInterface;

public class ReadNode extends ComputeNode implements ReadRemoteInterface {
    private ReaderStorage storage; 
    
    public ReadNode(ReaderStorage storage, ScheduledThreadPoolExecutor scheduler, String region) throws URISyntaxException {
        super(scheduler, String.format("r%s", region));
        this.storage = storage;
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.storage, this.s3helper), 5000, 5000, TimeUnit.MILLISECONDS);
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
            ReaderStorage storage = new ReaderStorage(region);
            storage.init();

            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

            ReadNode readNode = new ReadNode(storage, scheduler, region);
            
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
        Map<String, Integer> values = new HashMap<>(readSet.size());
        long stableTime = this.storage.getStableTime();

        for (String key: readSet) {
            try {
                Integer value = this.storage.get(key, stableTime).getValue();
                values.put(key, value);
            } catch (KeyNotFoundException e) {
                values.put(key, null);
                System.err.println(String.format("Key %s not found", key));
            } catch (KeyVersionNotFoundException e) {
                values.put(key, null);
            }    
        }
        return new ROTResponse(values, stableTime);
    }
  
}
