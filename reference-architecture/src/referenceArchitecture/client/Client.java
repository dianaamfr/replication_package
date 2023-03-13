package referenceArchitecture.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import referenceArchitecture.remoteInterface.ReadRemoteInterface;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;
  
public class Client {
    public static final String writeNodeId = "write-node";
    public static final String readNodeId = "read-node";
    
    enum Operation {
        ROT,
        WRITE,
    }    

    public static void main(String[] args) {

        Registry registry;
        try {
            registry = LocateRegistry.getRegistry();
            ReadRemoteInterface readStub = (ReadRemoteInterface) registry.lookup(readNodeId);
            WriteRemoteInterface writeStub = (WriteRemoteInterface) registry.lookup(writeNodeId);

            // Test RMI
            Map<String, Integer> readResponse = readStub.rot(null);
            Integer writeResponse = writeStub.write(null);

            System.out.println("Client: " + readResponse.toString());
            System.out.println("Client: " + writeResponse.toString());

        } catch (RemoteException | NotBoundException e) {
            System.err.println("Could not get registry");
        }
    }

    public void requestOperation(Operation operation) {
        switch (operation) {
            case ROT:
                break;
            case WRITE:
                break;
            default:
                break;
        }
    }
}
