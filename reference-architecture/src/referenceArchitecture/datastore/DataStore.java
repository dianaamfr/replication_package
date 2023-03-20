package referenceArchitecture.datastore;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONObject;

import referenceArchitecture.config.Config;

public class DataStore implements DataStoreInterface {
    private final String id;
    private TreeMap<Integer, TreeMap<String, String>> objects;
    
    public DataStore() {
        this.objects = new TreeMap<>();
        this.id = "data-store";
        this.init();
    }

    private void init() {
        for(Integer partition: Config.getPartitions()) {
            objects.put(partition, new TreeMap<>());
        }
    }

    public static void main(String[] args) {
        DataStore dataStore = new DataStore();

        // Bind the remote object's stub in the registry
        try {
            DataStoreInterface stub = (DataStoreInterface) UnicastRemoteObject.exportObject(dataStore, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(dataStore.getId(), stub);
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        } 
    }

    @Override
    public String read(String key, Integer partition) throws RemoteException {
        if(!this.objects.containsKey(partition)){
            System.err.println("Error: Invalid partition");
            return null;
        } 

        if(this.objects.get(partition).isEmpty()) {
            //System.err.println("Error: Empty partition");
            return null;
        }

        JSONObject json = new JSONObject();

        if(key == null) {
            Entry<String, String> lastEntry = objects.get(partition).lastEntry();
            json.put("key", lastEntry.getKey());
            json.put("value", lastEntry.getValue());
        } else {
            Entry<String, String> higherEntry = objects.get(partition).higherEntry(key);
            if(higherEntry != null) {
                json.put("key", higherEntry.getKey());
                json.put("value", higherEntry.getValue());
            }
        }

        return json.toString(); 
    }

    @Override
    public void write(String key, String value, Integer partition) throws RemoteException {
        if(!this.objects.containsKey(partition)){
            System.err.println("Error: Invalid partition");
            return;
        }

        if(this.objects.get(partition).containsKey(key)) {
            //System.err.println("Warning: Repeated key");
            return;
        }

        this.objects.get(partition).put(key, value);
    }

    public String getId() {
        return this.id;
    }
}
