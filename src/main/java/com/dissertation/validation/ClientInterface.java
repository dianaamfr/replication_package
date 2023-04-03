package com.dissertation.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.referencearchitecture.remoteInterface.response.ROTResponse;
import com.dissertation.referencearchitecture.remoteInterface.response.WriteResponse;
import com.dissertation.utils.Utils;

public class ClientInterface {
    private final Client client;

    private ClientInterface(Client client) {
        this.client = client;
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: java Client <region:String>");   
            return;
        }

        String region = args[0];
        try {
            Client client = new Client(region);
            (new ClientInterface(client)).run();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
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

            if(commands.length == 0) {
                System.err.println("Error: Unsupported command");
                continue;
            }

            String[] params = Arrays.copyOfRange(commands, 1, commands.length);
            switch(commands[0]) {
                case "R":
                    this.sendROTRequest(params);
                    break;
                case "W":
                    this.sendWriteRequest(params);
                    break;
                default:
                    System.err.println("Error: Unsupported command");
                    break;
            }

        } while(input != null);

        scanner.close();
    }

    private void sendROTRequest(String[] commands) {
        if(commands.length < 1) {
            System.err.println("Error: Unsupported command");
            return;
        } 
        
        ROTResponse result = this.client.requestROT(new HashSet<>(Arrays.asList(commands)));
        if(!result.isError()) {
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("ROT at %s:", result.getStableTime()));
            for(Entry<String, byte[]> entry: result.getValues().entrySet()) {
                builder.append(String.format("\n\t%s = %s", entry.getKey(), Utils.stringFromByteArray(entry.getValue())));
            }
            System.out.println(builder.toString());
        } else {
            System.err.println(result.getStatus());
        }
    }

    private void sendWriteRequest(String[] commands) {
        if(commands.length != 2) {
            System.err.println("Error: Unsupported command");
            return;
        }

        String key = commands[0];
        byte[] value = Utils.byteArrayFromString(commands[1]);
        WriteResponse result = this.client.requestWrite(key, value);
        
        if(!result.isError()) {
            System.out.println(String.format("Write response: %s = %s at %s ", key, commands[1], result));
        } else {
            System.err.println(result.getStatus());
        }
    }
}
