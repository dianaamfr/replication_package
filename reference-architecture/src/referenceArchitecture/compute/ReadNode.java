package referenceArchitecture.compute;

import java.nio.channels.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import referenceArchitecture.remoteInterface.ReadRemoteInterface;

public class ReadNode implements ReadRemoteInterface {
    public static final String id = "read-node";
    
    public static void main(String[] args) {
        ReadNode readNode = new ReadNode();

        // Bind the remote object's stub in the registry
        try {
            ReadRemoteInterface stub = (ReadRemoteInterface) UnicastRemoteObject.exportObject(readNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(id, stub);
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        } catch (java.rmi.AlreadyBoundException e) {
            System.err.println("Could not bind to registry");            
        }
    }

    @Override
    public Map<String, Integer> rot(Set<String> readSet) {
        return new HashMap<String,Integer>();
    }
    
}
