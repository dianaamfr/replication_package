package referenceArchitecture.datastore;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONObject;

public class DataStore implements DataStoreInterface {
    private final String id;
    private TreeMap<String, String> objects;
    
    public DataStore() {
        this.objects = new TreeMap<>();
        this.id = "data-store";
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
    public String read(String key) throws RemoteException {
        JSONObject json = new JSONObject();
        if(key == null) {
            Entry<String, String> lastEntry = objects.lastEntry();
            json.put(lastEntry.getKey(), lastEntry.getValue());
        } else {
            Entry<String, String> higherEntry = objects.higherEntry(key);
            json.put(higherEntry.getKey(), higherEntry.getValue());
        }

        return json.toString(); 
    }

    @Override
    public void write(String key, String value) throws RemoteException {
        if(objects.containsKey(key)){
            //System.err.println("Warning: storing duplicate key");
            return;
        }
        System.out.println(value);
        objects.put(key, value);
    }

    public String getId() {
        return this.id;
    }
}
