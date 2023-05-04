package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class ClientInterface {
    private final Client client;
    private static final String USAGE = "Usage: ClientInterface <readPort> <readAddress> (<writePort:Int> <writeIp:String>)+";

    private ClientInterface(Client client) {
        this.client = client;
    }

    public static void main(String[] args) {
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 5 || (args.length - 2) % 3 != 0) {
            System.err.println(USAGE);
            return;
        }

        try {
            readAddress = new Address(Integer.parseInt(args[0]), args[1]);
            for (int i = 2; i < args.length; i += 3) {
                writeAddresses.add(new Address(Integer.parseInt(args[i]), args[i + 1], Integer.parseInt(args[i + 2])));
            }

            Client client = new Client(readAddress, writeAddresses);
            (new ClientInterface(client)).run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = null;

        do {
            System.out.println("Enter an operation:");
            System.out.println("  ROT: R <key>+");
            System.out.println("  Write: W <key> <value>");
            System.out.println("  CAS Write:");
            System.out.println("    WA <key> <value> <expectedVersion>");
            System.out.println("    WB <key> <value> <expectedVersion> <expectedValue>");
            System.out.println("    WC <key> <value> <expectedValue>?");
            input = scanner.nextLine();
            String[] commands = input.split(" ");

            if (commands.length == 0) {
                System.err.println("Unsupported command");
                continue;
            }

            String[] params = Arrays.copyOfRange(commands, 1, commands.length);
            switch (commands[0]) {
                case "R":
                    this.sendROTRequest(params);
                    break;
                case "W":
                    this.sendWriteRequest(params);
                    break;
                case "WA":
                case "WB":
                    this.sendCompareVersionAndWriteRequest(params);
                    break;
                case "WC":
                    this.sendCompareValueAndWriteRequest(params);
                    break;
                default:
                    System.err.println("Unsupported command");
                    break;
            }

        } while (input != null);

        scanner.close();
    }

    private void sendROTRequest(String[] commands) {
        if (commands.length < 1) {
            System.err.println("Unsupported command");
            return;
        }

        ROTResponse rotResponse = this.client.requestROT(new HashSet<>(Arrays.asList(commands)));
        if (!rotResponse.getError()) {
            StringBuilder builder = new StringBuilder();
            builder.append("ROT response:");
            builder.append(String.format("%n  stableTime = %s", rotResponse.getStableTime()));
            for (Entry<String, KeyVersion> entry : rotResponse.getVersionsMap().entrySet()) {
                builder = this.addROTOutput(builder, entry);
            }
            System.out.println(builder.toString());
        } else {
            System.err.println(rotResponse.getStatus());
        }
    }

    private void sendWriteRequest(String[] commands) {
        if (commands.length != 2) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        ByteString value = Utils.byteStringFromString(commands[1]);
        WriteResponse writeResponse = this.client.requestWrite(key, value);

        if (!writeResponse.getError()) {
            System.out.println(this.addWriteOutput(new StringBuilder(), key, commands[1], writeResponse.getWriteTimestamp()).toString());
        } else {
            System.err.println(writeResponse.getStatus());
        }
    }

    private void sendCompareVersionAndWriteRequest(String[] commands) {
        if (commands.length < 3 || commands.length > 4) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        ByteString value = Utils.byteStringFromString(commands[1]);
        WriteResponse writeResponse = this.client.requestCompareVersionAndWrite(key, value, commands[2], commands.length == 4 ? Utils.byteStringFromString(commands[3]) : null);

        if (!writeResponse.getError()) {
            System.out.println(this.addWriteOutput(new StringBuilder(), key, commands[1], writeResponse.getWriteTimestamp()).toString());
        } else {
            System.err.println(this.addAtomicWriteErrorOutput(new StringBuilder(), key, writeResponse).toString());
        }
    }

    private void sendCompareValueAndWriteRequest(String[] commands) {
        if (commands.length < 2 || commands.length > 3) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        ByteString value = Utils.byteStringFromString(commands[1]);
        ByteString expectedValue = commands.length == 3 ? Utils.byteStringFromString(commands[2]) : ByteString.EMPTY;
        WriteResponse writeResponse = this.client.requestCompareValueAndWrite(key, value, expectedValue);

        if (!writeResponse.getError()) {
            System.out.println(this.addWriteOutput(new StringBuilder(), key, commands[1], writeResponse.getWriteTimestamp()).toString());
        } else {
            System.err.println(this.addAtomicWriteErrorOutput(new StringBuilder(), key, writeResponse).toString());
        }
    }

    // private void sendReadVersionRequest(String[] commands) {
    //     if (commands.length != 2) {
    //         System.err.println("Unsupported command");
    //         return;
    //     }

    //     String key = commands[0];
    //     String version = commands[1];
    //     ReadVersionResponse readVersionResponse = this.client.requestReadVersion(key, commands[1]);

    //     if (!readVersionResponse.getError()) {
    //         StringBuilder builder = new StringBuilder();
    
    //         builder = this.addReadVersionOutput(builder, key, version, readVersionResponse);
    //         System.out.println(builder.toString());
    //     } else {
    //         System.err.println(readVersionResponse.getStatus());
    //     }
    // }

    private StringBuilder addROTOutput(StringBuilder builder, Entry<String, KeyVersion> entry) {
        builder.append(String.format("%n  key = %s", entry.getKey()));
        builder.append(String.format("%n  value = %s", entry.getValue().getValue().isEmpty() ? null : Utils.stringFromByteString(entry.getValue().getValue())));
        builder.append(String.format("%n  version = %s%n", entry.getValue().getTimestamp()));
        return builder;
    }

    // private StringBuilder addReadVersionOutput(StringBuilder builder, String key, String version, ReadVersionResponse readVersionResponse) {
    //     builder.append("Read Version response:");
    //     builder.append(String.format("%n  stableTime = %s", readVersionResponse.getStableTime()));
    //     builder.append(String.format("%n  key = %s", key));
    //     builder.append(String.format("%n  value = %s", readVersionResponse.getValue().isEmpty() ? null : Utils.stringFromByteString(readVersionResponse.getValue())));
    //     builder.append(String.format("%n  version = %s%n", version));
    //     return builder;
    // }

    private StringBuilder addWriteOutput(StringBuilder builder, String key, String value, String timestamp) {
        builder.append(String.format("Write response:"));
        builder.append(String.format("%n  key = %s", key));
        builder.append(String.format("%n  value = %s", value));
        builder.append(String.format("%n  version = %s%n", timestamp));
        return builder;
    }

    public StringBuilder addAtomicWriteErrorOutput(StringBuilder builder, String key, WriteResponse writeResponse) {

        builder.append(String.format(writeResponse.getStatus()));

        if(writeResponse.hasCurrentVersion()) {
            builder.append(String.format("%n Current version of %s:", key));
            builder.append(String.format("%n   %s", writeResponse.getCurrentVersion()));
        } 
        return builder;
    }
}
