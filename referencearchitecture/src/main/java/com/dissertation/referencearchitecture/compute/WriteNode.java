package com.dissertation.referencearchitecture.compute;

import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.compute.clock.LogicalClock;
import com.dissertation.referencearchitecture.compute.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.compute.storage.Storage;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.remoteInterface.WriteRemoteInterface;

public class WriteNode extends ComputeNode implements WriteRemoteInterface {
    private Storage storage;  
    private LogicalClock logicalClock;
    private String partition;

    public WriteNode(Storage storage, ScheduledThreadPoolExecutor scheduler, String partition) throws URISyntaxException {
        super(scheduler, String.format("w%d", partition));
        this.storage = storage;
        this.logicalClock = new LogicalClock();
        this.partition = partition;
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new StoragePusher(this.storage, this.logicalClock, this.s3helper, this.partition), 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: java WriteNode <partition:String>");   
            return;
        }

        String partition = args[0];
        if(!Config.isPartition(partition)) {
            System.err.println(String.format("Error: Partition %p does not exist", partition));   
            return;
        }

        try {
            Storage storage = new Storage();
            storage.init(Config.getKeys(partition));

            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

            WriteNode writeNode = new WriteNode(storage, scheduler, partition);

            // Bind the remote object's stub in the registry
            WriteRemoteInterface stub = (WriteRemoteInterface) UnicastRemoteObject.exportObject(writeNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(writeNode.id, stub);
            
            writeNode.init();
        } catch (URISyntaxException e) {
            System.err.println("Could not connect with AWS S3");
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        }
    }

    @Override
    public long write(String key, Integer value, long lastWriteTimestamp) {  
        if(!Config.isKeyInPartition(this.partition, key)) {
            System.err.println(String.format("Error: Key %s not found", key));
            return -1;
        }

        try {
            long timestamp = this.logicalClock.internalEvent(lastWriteTimestamp);
            this.storage.put(key, timestamp, value);
            this.logicalClock.tick(lastWriteTimestamp);
            return timestamp;
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Key %s not found", key));
            return -1;
        }
    }
}