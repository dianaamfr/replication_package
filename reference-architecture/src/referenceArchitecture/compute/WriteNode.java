package referenceArchitecture.compute;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import referenceArchitecture.remoteInterface.WriteRemoteInterface;

public class WriteNode implements WriteRemoteInterface {  
    public static final String id = "write-node";
    public static void main(String[] args) {
        WriteNode writeNode = new WriteNode();

        // Bind the remote object's stub in the registry
        Registry registry;
        try {
            WriteRemoteInterface stub = (WriteRemoteInterface) UnicastRemoteObject.exportObject(writeNode, 0);
            registry = LocateRegistry.getRegistry();
            registry.bind(id, stub);
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        }
    }

    @Override
    public Integer write(String key) {
        return 0;
    }
}