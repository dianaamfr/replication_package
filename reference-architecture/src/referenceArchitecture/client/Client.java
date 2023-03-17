package referenceArchitecture.client;

import java.io.Writer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.config.Config;
import referenceArchitecture.remoteInterface.ReadRemoteInterface;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;
  
public class Client {
    private final Map<Integer, WriteRemoteInterface> writeStubs;
    private final ReadRemoteInterface readStub;
    private String region;

    public Client(Map<Integer, WriteRemoteInterface> writeStubs, ReadRemoteInterface readStub, String region) {
        this.writeStubs = writeStubs;
        this.readStub = readStub;
        this.region = region;
    }

    public static void main(String[] args) {
 
        try {
            if(args.length < 1) {
                System.err.println("Usage: java Client <region:String>");   
                return;
            }

            Registry registry = LocateRegistry.getRegistry();

            String region = args[0];
            if(!Config.isRegion(region)) {
                System.err.println("Error: Invalid Region");   
                return;
            }
            String readNodeId = String.format("r%s", region);
            ReadRemoteInterface readStub = (ReadRemoteInterface) registry.lookup(readNodeId);

            Map<Integer, WriteRemoteInterface> writeStubs = new HashMap<>();
            List<Integer> partitions = Config.getPartitions(region);
            for(Integer partition: partitions) {
                String writeNodeId = String.format("w%s%d", region, partition);
                WriteRemoteInterface writeStub = (WriteRemoteInterface) registry.lookup(writeNodeId);
                writeStubs.put(partition, writeStub);
            }

            Client client = new Client(writeStubs, readStub, region);
            client.run();
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Could not get registry");
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = null;
        
        do {   
            System.out.println("Enter an operation:");
            System.out.println("\tROT: R <key:String>+");
            System.out.println("\tWrite: W <key:String> <value:Integer>");
            input = scanner.nextLine();
            String[] commands = input.split(" ");

            switch(commands[0]) {
                case "R":
                    if(commands.length - 1 < 1) {
                        System.err.println("Error: Unsupported command");
                    } else {
                        requestROT(Arrays.copyOfRange(commands, 1, commands.length));
                    }
                    break;
                case "W":
                    if(commands.length - 1 != 2 || commands.length % 2 == 0) {
                        System.err.println("Error: Unsupported command");
                    } else {
                        requestWrite(Arrays.copyOfRange(commands, 1, commands.length));
                    }
                    break;
                default:
                    System.err.println("Error: Unsupported command");
                    break;
            }

        } while(input != null);

        scanner.close();
    }

    public void requestROT(String[] commands) {
        Set<String> keys = new HashSet<String>();
        Map<String, Integer> readResponse;
        try {
            for(String command: commands) {
                keys.add(command);
                if(!Config.isKeyInRegion(this.region, command)) {
                    throw new KeyNotFoundException();
                }
            }
            readResponse = this.readStub.rot(keys);
            System.out.println("ROT response: " + readResponse.toString());
        } catch (RemoteException e) {
            System.err.println("Error: Could not connect with server");
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Key is not available in region %s", this.region));
        }
    }

    public void requestWrite(String[] commands) {
        long writeResponse;
        try {
            Integer partition = Config.getKeyPartition(this.region, commands[0]);
            WriteRemoteInterface writeStub = this.writeStubs.get(partition);
            writeResponse = writeStub.write(commands[0], Integer.parseInt(commands[1]), null);
            System.out.println("Write response: " + writeResponse);
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Key is not available in region %s", this.region));
        } catch (NumberFormatException e) {
            System.err.println("Error: Value must be an Integer");
        } catch (RemoteException e) {
            System.err.println("Error: Could not connect with server");
        }
    }
}
