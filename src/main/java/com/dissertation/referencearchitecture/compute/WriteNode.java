package com.dissertation.referencearchitecture.compute;

import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.compute.clock.ClockSyncHandler;
import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.ClockState;
import com.dissertation.referencearchitecture.compute.clock.TimeProvider;
import com.dissertation.referencearchitecture.compute.clock.ClockState.State;
import com.dissertation.referencearchitecture.compute.storage.Storage;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.remoteInterface.WriteRemoteInterface;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

public class WriteNode extends ComputeNode implements WriteRemoteInterface {
    private Storage storage;  
    private StoragePusher storagePusher;
    private HLC hlc;
    private String partition;

    public WriteNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String partition, Storage storage, StoragePusher storagePusher, HLC hlc) throws URISyntaxException {
        super(scheduler, s3Helper, String.format("w%s", partition));
        this.partition = partition;
        this.storage = storage;        
        this.storagePusher = storagePusher;
        this.hlc = hlc;
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new ClockSyncHandler(this.hlc, this.s3Helper, this.storagePusher), Utils.SYNC_DELAY, Utils.SYNC_DELAY, TimeUnit.MILLISECONDS);
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
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);
            S3Helper s3Helper = new S3Helper();
            Storage storage = new Storage();
            StoragePusher storagePusher = new StoragePusher(storage, s3Helper, partition);
            HLC hlc = new HLC(new TimeProvider(scheduler, Utils.CLOCK_DELAY));

            WriteNode writeNode = new WriteNode(scheduler, s3Helper, partition, storage, storagePusher, hlc);

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

    // TODO: don't return null, use design pattern null object
    @Override
    public String write(String key, byte[] value, String lastWriteTimestamp) {  
        if(!Config.isKeyInPartition(this.partition, key)) {
            System.err.println(String.format("Error: Key %s not found", key));
            return null;
        }

        ClockState lastTimestamp;
        try {
            lastTimestamp = ClockState.fromString(lastWriteTimestamp, State.WRITE);
        } catch (InvalidTimestampException e) {
            System.err.println(String.format("Error: Invalid lastWriteTimestamp"));
            return null;
        }

        String writeTimestamp = null;
         try {
            writeTimestamp = this.hlc.writeEvent(lastTimestamp).toString();
            this.storage.put(key, writeTimestamp, value);
            boolean result = this.storagePusher.push(writeTimestamp);
            if(result == false) {
                // TODO: Should the timestamp be reset?
                this.storage.delete(key, writeTimestamp);
            }
        } catch (KeyNotFoundException e) {
            // TODO: Should the timestamp be reset?
            System.err.println(String.format("Error: Key %s not found", key));
        }
        this.hlc.writeComplete();
        return writeTimestamp;
    }
}