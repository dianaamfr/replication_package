package com.dissertation.evaluation;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Scanner;
import com.dissertation.eventual.client.Client;
import com.dissertation.eventual.s3.S3ReadResponse;
import com.dissertation.eventual.s3.S3Response;


public class ClientInterface {
    private final Client client;
    private ClientInterface(Client client) {
        this.client = client;
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            (new ClientInterface(client)).run();
        } catch (URISyntaxException e) {
            System.err.println("Failed to create client");
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = null;

        do {
            System.out.println("Enter an operation:");
            System.out.println("  Read: R <key>");
            System.out.println("  Write: W <key> <value>");
            input = scanner.nextLine();
            String[] commands = input.split(" ");

            if (commands.length == 0) {
                System.err.println("Unsupported command");
                continue;
            }

            String[] params = Arrays.copyOfRange(commands, 1, commands.length);
            switch (commands[0]) {
                case "R":
                    this.sendReadRequest(params);
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

    private void sendReadRequest(String[] commands) {
        if (commands.length != 1) {
            System.err.println("Unsupported command");
            return;
        }

        S3ReadResponse readResponse = this.client.read(commands[0]);
        if (!readResponse.isError()) {
            System.out.println(this.readOutput(commands[0], readResponse.getContent()));
        } else {
            System.err.println(readResponse.getStatus());
        }
    }

    private void sendWriteRequest(String[] commands) {
        if (commands.length != 2) {
            System.err.println("Unsupported command");
            return;
        }

        String key = commands[0];
        String value = commands[1];
        S3Response writeResponse = this.client.write(key, value);

        if (!writeResponse.isError()) {
            System.out.println(this.writeOutput(key, value));
        } else {
            System.err.println(writeResponse.getStatus());
        }
    }

    private String readOutput(String key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("Read response:");
        builder.append(String.format("%n  key = %s", key));
        builder.append(String.format("%n  value = %s", value.isBlank() ? null : value));
        return builder.toString();
    }

    private String writeOutput(String key, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("Write response:");
        builder.append(String.format("%n  key = %s", key));
        builder.append(String.format("%n  value = %s", value));
        return builder.toString();
    }

}
