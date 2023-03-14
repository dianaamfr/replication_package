package referenceArchitecture.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import referenceArchitecture.remoteInterface.ReadRemoteInterface;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;
  
public class Client {
    public static final String writeNodeId = "write-node";
    public static final String readNodeId = "read-node";
    
    private static ReadRemoteInterface readStub;
    private static WriteRemoteInterface writeStub;
    
    enum Operation {
        ROT,
        WRITE,
    }    

    public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.getRegistry();
            readStub = (ReadRemoteInterface) registry.lookup(readNodeId);
            writeStub = (WriteRemoteInterface) registry.lookup(writeNodeId);

            // Test RMI
            requestOperation(Operation.ROT);
            requestOperation(Operation.WRITE);
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Could not get registry");
        }
    }

    public static void requestOperation(Operation operation) {

        switch (operation) {
            case ROT:
                Map<String, Integer> readResponse;
                try {
                    Set<String> keys = new HashSet<String>();
                    keys.add("x");
                    keys.add("y");
                    readResponse = readStub.rot(keys);
                    System.out.println("Client: " + readResponse.toString());
                } catch (RemoteException e) {
                    System.err.println("Client: ROT operation failed");
                    e.printStackTrace();
                }
                break;
            case WRITE:
                String writeResponse;
                try {
                    writeResponse = writeStub.write("x", 2, null);
                    System.out.println("Client: " + writeResponse);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.err.println("Client: Write operation failed");
                }
                break;
            default:
                break;
        }
    }
}
