package com.dissertation.referencearchitecture.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.remoteInterface.ROTResponse;
import com.dissertation.referencearchitecture.remoteInterface.ReadRemoteInterface;
import com.dissertation.referencearchitecture.remoteInterface.WriteRemoteInterface;

public class Client {
    private final Map<String, WriteRemoteInterface> writeStubs;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;
    private final ReadRemoteInterface readStub;
    private String region;

    public Client(Map<String, WriteRemoteInterface> writeStubs, ReadRemoteInterface readStub, String region) {
        this.writeStubs = writeStubs;
        this.readStub = readStub;
        this.region = region;
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = "0.0";
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: java Client <region:String>");   
            return;
        }

        String region = args[0];
        if(!Config.isRegion(region)) {
            System.err.println("Error: Invalid Region");   
            return;
        }

        try {
            
            Registry registry = LocateRegistry.getRegistry();
            String readNodeId = String.format("r%s", region);
            ReadRemoteInterface readStub = (ReadRemoteInterface) registry.lookup(readNodeId);

            Map<String, WriteRemoteInterface> writeStubs = new HashMap<>();
            Set<String> partitions = Config.getPartitions(region);
            for(String partition: partitions) {
                String writeNodeId = String.format("w%s", partition);
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
            System.out.println("\tWrite: W <key:String> <value:Int>");
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

    private void requestROT(String[] commands) {
        Set<String> keys = new HashSet<String>();
        try {
            for(String command: commands) {
                keys.add(command);
                if(!Config.isKeyInRegion(this.region, command)) {
                    throw new KeyNotFoundException();
                }
            }
            ROTResponse rotResponse = this.readStub.rot(keys);
            pruneCache(rotResponse.getStableTime());
            System.out.println(String.format("ROT response: %s at %s", getReadResponse(rotResponse.getValues()).toString(), rotResponse.getStableTime()));
        } catch (RemoteException e) {
            System.err.println("Error: Could not connect with server");
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Not all keys are available in region %s", this.region));
        }
    }

    private void requestWrite(String[] commands) {
        try {
            String key = commands[0];
            Integer value = Integer.parseInt(commands[1]);
            String partition = Config.getKeyPartition(this.region, key);
            WriteRemoteInterface writeStub = this.writeStubs.get(partition);
            String response = writeStub.write(key, value, this.lastWriteTimestamp);

            if(response != null) {
                this.lastWriteTimestamp = response;
                this.cache.put(key, new Version(key, value, this.lastWriteTimestamp));
                System.out.println(String.format("Write response: %s = %d at %s ", key, value, this.lastWriteTimestamp));
            } else {
                System.out.println("Error: Write request failed");
            }
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Key is not available in region %s", this.region));
        } catch (NumberFormatException e) {
            System.err.println("Error: Value must be an Integer");
        } catch (RemoteException e) {
            System.err.println("Error: Could not connect with server");
        }
    }

    private void pruneCache(String stableTime) {
        List<String> toPrune = new ArrayList<>();
        for(Entry<String,Version> entry :this.cache.entrySet()) {
            if(entry.getValue().getTimestamp().compareTo(stableTime) <= 0) {
                toPrune.add(entry.getKey());
            }   
        }
        this.cache.keySet().removeAll(toPrune);
    }

    private Map<String, Integer> getReadResponse(Map<String, Integer> response) {
        Map<String, Integer> values = new HashMap<>();

        for(Entry<String, Integer> entry: response.entrySet()) {
            Version v = this.cache.getOrDefault(entry.getKey(), null);
            if(v != null) {
                values.put(entry.getKey(), v.getValue());
            } else {
                values.put(entry.getKey(), entry.getValue());
            }
        }
        
        return values;
    }
}
