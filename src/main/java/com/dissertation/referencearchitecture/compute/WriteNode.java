package com.dissertation.referencearchitecture.compute;

import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import com.dissertation.referencearchitecture.compute.clock.HLC;
import com.dissertation.referencearchitecture.compute.clock.HybridTimestamp;
import com.dissertation.referencearchitecture.compute.clock.SystemTimeProvider;
import com.dissertation.referencearchitecture.compute.storage.Storage;
import com.dissertation.referencearchitecture.compute.storage.StoragePusher;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.InvalidTimestampException;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.remoteInterface.WriteRemoteInterface;
import com.dissertation.referencearchitecture.s3.S3Helper;

public class WriteNode extends ComputeNode implements WriteRemoteInterface {
    private Storage storage;  
    private StoragePusher storagePusher;
    private HLC hlc;
    private String partition;
    private ReentrantLock mutex;

    public WriteNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String partition, Storage storage, StoragePusher storagePusher, HLC hlc, ReentrantLock mutex) throws URISyntaxException {
        super(scheduler, s3Helper, String.format("w%s", partition));
        this.partition = partition;
        this.storage = storage;        
        this.storagePusher = storagePusher;
        this.hlc = hlc;
        this.mutex = mutex;
    }

    public void init() {
        //this.scheduler.scheduleWithFixedDelay(new ClockSyncHandler(this.logicalClock, this.s3helper, this.mutex), 5000, 5000, TimeUnit.MILLISECONDS);
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
            // TODO: number of threads
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(3);
            S3Helper s3Helper = new S3Helper();
            Storage storage = new Storage();
            StoragePusher storagePusher = new StoragePusher(storage, s3Helper, partition);
            HLC hlc = new HLC(new SystemTimeProvider(scheduler, 10000));
            ReentrantLock mutex = new ReentrantLock();
        
            WriteNode writeNode = new WriteNode(scheduler, s3Helper, partition, storage, storagePusher, hlc, mutex);

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
    public String write(String key, Integer value, String lastWriteTimestamp) {  
        if(!Config.isKeyInPartition(this.partition, key)) {
            System.err.println(String.format("Error: Key %s not found", key));
            return null;
        }

        HybridTimestamp lastTimestamp;
        try {
            lastTimestamp = HybridTimestamp.fromString(lastWriteTimestamp);
        } catch (InvalidTimestampException e) {
            System.err.println(String.format("Error: Invalid lastWriteTimestamp"));
            return null;
        }

        String writeTimestamp = null;
        mutex.lock();
         try {
            this.hlc.writeEvent(lastTimestamp);
            writeTimestamp = this.hlc.getTimestamp().toString();
            this.storage.put(key, writeTimestamp, value);
            boolean result = this.storagePusher.push(writeTimestamp);
            if(result == false) {
                writeTimestamp = null;
                // TODO: reset timestamp and storage (?)
            }
        } catch (KeyNotFoundException e) {
            // TODO: reset timestamp
            System.err.println(String.format("Error: Key %s not found", key));
        } finally {
            this.mutex.unlock();
        }
        return writeTimestamp;
    }
}