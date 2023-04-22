package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.client.Client;
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
            System.out.println("\tROT: R <key:String>+");
            System.out.println("\tWrite: W <key:String> <value:Int>");
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
            builder.append(String.format("ROT at %s:", rotResponse.getStableTime()));
            for (Entry<String, ByteString> entry : rotResponse.getValuesMap().entrySet()) {
                builder.append(
                        String.format("\n\t%s = %s", entry.getKey(),
                                entry.getValue().isEmpty() ? null : Utils.stringFromByteString(entry.getValue())));
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
            System.out.println(String.format(
                    "Write response: %s = %s at %s ",
                    key, commands[1], writeResponse.getWriteTimestamp()));
        } else {
            System.err.println(writeResponse.getStatus());
        }
    }
}
