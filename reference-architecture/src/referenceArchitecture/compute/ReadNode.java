package referenceArchitecture.compute;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
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
import referenceArchitecture.compute.storage.ReaderStorage;
import referenceArchitecture.compute.storage.StoragePuller;
import referenceArchitecture.config.Config;
import referenceArchitecture.datastore.DataStoreInterface;
import referenceArchitecture.remoteInterface.ROTResponse;
import referenceArchitecture.remoteInterface.ReadRemoteInterface;

public class ReadNode extends ComputeNode implements ReadRemoteInterface {
    private ReaderStorage storage; 
    private static DataStoreInterface dataStoreStub;
    private static final String dataStoreId = "data-store";
    
    public ReadNode(ReaderStorage storage, ScheduledThreadPoolExecutor scheduler, String region) {
        super(scheduler, String.format("r%s", region));
        this.storage = storage;
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new StoragePuller(this.storage, dataStoreStub), 5000, 5000, TimeUnit.MILLISECONDS);
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

            // Get reference of data store
            dataStoreStub = (DataStoreInterface) registry.lookup(dataStoreId);
            readNode.init();
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        } catch (NotBoundException e) {
            System.err.println("Could not find the registry of the data store");
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
