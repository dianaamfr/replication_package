package com.dissertation.evaluation.singleClient;

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

        ROTResponse rotResponse;
        try {
            rotResponse = this.client.requestROT(new HashSet<>(Arrays.asList(commands)));
        } catch (Exception e) {
            Utils.printException(e);
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("ROT response:");
        builder.append(String.format("%n  stableTime = %s", rotResponse.getStableTime()));
        for (Entry<String, KeyVersion> entry : rotResponse.getVersionsMap().entrySet()) {
            builder = this.addROTOutput(builder, entry);
        }
        System.out.println(builder.toString());
    }

    private void sendWriteRequest(String[] commands) {
        if (commands.length != 2) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        ByteString value = Utils.byteStringFromString(commands[1]);
        WriteResponse writeResponse;
        try {
            writeResponse = this.client.requestWrite(key, value);
        } catch (Exception e) {
            Utils.printException(e);
            return;
        }
        System.out.println(this.addWriteOutput(new StringBuilder(), key, commands[1], writeResponse.getWriteTimestamp())
                .toString());
    }

    private void sendCompareVersionAndWriteRequest(String[] commands) {
        if (commands.length < 3 || commands.length > 4) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        ByteString value = Utils.byteStringFromString(commands[1]);
        WriteResponse writeResponse;
        try {
            writeResponse = this.client.requestCompareVersionAndWrite(key, value, commands[2],
                    commands.length == 4 ? Utils.byteStringFromString(commands[3]) : null);
        } catch (Exception e) {
            Utils.printException(e);
            return;
        }

        System.out.println(this.addAtomicWriteOutput(new StringBuilder(), key, commands[1], writeResponse).toString());
    }

    private void sendCompareValueAndWriteRequest(String[] commands) {
        if (commands.length < 2 || commands.length > 3) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        ByteString value = Utils.byteStringFromString(commands[1]);
        ByteString expectedValue = commands.length == 3 ? Utils.byteStringFromString(commands[2]) : ByteString.EMPTY;
        WriteResponse writeResponse;

        try {
            writeResponse = this.client.requestCompareValueAndWrite(key, value, expectedValue);
        } catch (Exception e) {
            Utils.printException(e);
            return;
        }

        System.out.println(this.addAtomicWriteOutput(new StringBuilder(), key, commands[1], writeResponse).toString());
    }

    private StringBuilder addROTOutput(StringBuilder builder, Entry<String, KeyVersion> entry) {
        builder.append(String.format("%n  key = %s", entry.getKey()));
        builder.append(String.format("%n  value = %s", entry.getValue().getValue().isEmpty() ? null
                : Utils.stringFromByteString(entry.getValue().getValue())));
        builder.append(String.format("%n  version = %s%n", entry.getValue().getTimestamp()));
        return builder;
    }

    private StringBuilder addWriteOutput(StringBuilder builder, String key, String value, String timestamp) {
        builder.append(String.format("Write response:"));
        builder.append(String.format("%n  key = %s", key));
        builder.append(String.format("%n  value = %s", value));
        builder.append(String.format("%n  version = %s%n", timestamp));
        return builder;
    }

    public StringBuilder addAtomicWriteOutput(StringBuilder builder, String key, String value,
            WriteResponse writeResponse) {

        if (writeResponse.hasCurrentVersion()) {
            builder.append(String.format("Write failed:"));
            builder.append(String.format("%n Current version of %s:", key));
            builder.append(String.format("%n   %s%n", writeResponse.getCurrentVersion()));
        } else {
            addWriteOutput(builder, key, value, writeResponse.getWriteTimestamp());
        }
        return builder;
    }
}
