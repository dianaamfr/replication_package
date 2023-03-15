package referenceArchitecture.datastore;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeMap;

public class DataStore implements DataStoreInterface {
    private final String id;
    private TreeMap<String, Object> objects;
    
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
    public Object read(String key) throws RemoteException {
        if(key == null) {
            return objects.lastEntry();
        } 

        return objects.higherEntry(key); 
    }

    @Override
    public void write(String key, Object value) throws RemoteException {
        if(objects.containsKey(key)){
            //System.err.println("Warning: storing duplicate key");
            return;
        }
        System.out.println("HERE");
        objects.put(key, value);
        System.out.println(objects.toString());
    }

    public String getId() {
        return this.id;
    }
}
