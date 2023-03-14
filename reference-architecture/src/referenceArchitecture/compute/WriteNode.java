package referenceArchitecture.compute;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import referenceArchitecture.compute.storage.Storage;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;

public class WriteNode extends ComputeNode implements WriteRemoteInterface {  
    public static final String id = "write-node";
    public static final String timestamp = "RANDOM_TIMESTAMP";

    public WriteNode(Storage storage, ScheduledThreadPoolExecutor scheduler) {
        super(storage, scheduler);
    }

    public static void main(String[] args) {
        Storage storage = new Storage();
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(0);
        WriteNode writeNode = new WriteNode(storage, scheduler);

        // Bind the remote object's stub in the registry
        try {
            WriteRemoteInterface stub = (WriteRemoteInterface) UnicastRemoteObject.exportObject(writeNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(id, stub);
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        }
    }

    @Override
    public String write(String key, Integer value, String lastWriteTimestamp) {
        storage.put(key, timestamp, value);
        return timestamp;
    }
}