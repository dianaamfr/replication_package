package referenceArchitecture.compute;

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

import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.compute.exceptions.KeyVersionNotFoundException;
import referenceArchitecture.compute.storage.Storage;
import referenceArchitecture.compute.storage.StorageUpdater;
import referenceArchitecture.remoteInterface.ReadRemoteInterface;

public class ReadNode extends ComputeNode implements ReadRemoteInterface {
    private static final String id = "read-node";

    public ReadNode(Storage storage, ScheduledThreadPoolExecutor scheduler) {
        super(storage, scheduler);
        this.scheduler.scheduleWithFixedDelay(new StorageUpdater(storage), 500, 500, TimeUnit.MILLISECONDS);
        
        // Test ROT
        storage.put("x", "1", 4);
        storage.put("y", "2", 5);
        storage.put("x", "3", 6);
    }
    
    public static void main(String[] args) {
        Storage storage = new Storage();
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        ReadNode readNode = new ReadNode(storage, scheduler);

        // Bind the remote object's stub in the registry
        try {
            ReadRemoteInterface stub = (ReadRemoteInterface) UnicastRemoteObject.exportObject(readNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(id, stub);
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        } 
    }

    @Override
    public Map<String, Integer> rot(Set<String> readSet) {
        Map<String, Integer> values = new HashMap<>(readSet.size());
        String stableTime = storage.getStableTime();

        for (String key: readSet) {
            try {
                Integer value = storage.get(key, stableTime).getValue();
                values.put(key, value);
            } catch (KeyNotFoundException e) {
                e.printStackTrace();
            } catch (KeyVersionNotFoundException e) {
                e.printStackTrace();
            }    
        }
        return values;
    }
  
}
