package referenceArchitecture.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import referenceArchitecture.remoteInterface.ReadRemoteInterface;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;
  
public class Client {
    public static final String writeNodeId = "write-node";
    public static final String readNodeId = "read-node";
    
    private static ReadRemoteInterface readStub;
    private static WriteRemoteInterface writeStub;

    public static void main(String[] args) {
 
        try {
            Registry registry = LocateRegistry.getRegistry();
            readStub = (ReadRemoteInterface) registry.lookup(readNodeId);
            writeStub = (WriteRemoteInterface) registry.lookup(writeNodeId);

            operationListener();
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Could not get registry");
        }
    }

    public static void operationListener() {
        Scanner scanner = new Scanner(System.in);
        String input = null;
        
        do {   
            System.out.println("Enter an operation:");
            System.out.println("\tR k k k");
            System.out.println("\tW k v");
            input = scanner.nextLine();
            String[] commands = input.split(" ");
            boolean result = true;

            switch(commands[0]) {
                case "R":
                    if(commands.length - 1 < 1) {
                        System.err.println("Unsupported command");
                    } else {
                        result = requestROT(Arrays.copyOfRange(commands, 1, commands.length));
                    }
                    break;
                case "W":
                    if(commands.length - 1 != 2 || commands.length % 2 == 0) {
                        System.err.println("Unsupported command");
                    } else {
                        result = requestWrite(Arrays.copyOfRange(commands, 1, commands.length));
                    }
                    break;
                default:
                    System.err.println("Unsupported command");
                    break;
            }

            if(result == false) {
                System.out.println("Operation failed");
            }

        } while(input != null);

        scanner.close();
    }

    public static boolean requestROT(String[] commands) {
        Set<String> keys = new HashSet<String>();
        Map<String, Integer> readResponse;
        try {
            for(String command: commands) {
                keys.add(command);
            }
            readResponse = readStub.rot(keys);
            System.out.println("ROT response: " + readResponse.toString());
        } catch (RemoteException e) {
            return false;
        }
        return true;
    }

    public static boolean requestWrite(String[] commands) {
        long writeResponse;
        try {
            writeResponse = writeStub.write(commands[0], Integer.parseInt(commands[1]), null);
            System.out.println("Client: " + writeResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
        return true;
    }
}
