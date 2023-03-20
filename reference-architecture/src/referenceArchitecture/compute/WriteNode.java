package referenceArchitecture.compute;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import referenceArchitecture.compute.clock.LogicalClock;
import referenceArchitecture.compute.exceptions.KeyNotFoundException;
import referenceArchitecture.compute.storage.Storage;
import referenceArchitecture.compute.storage.StoragePusher;
import referenceArchitecture.config.Config;
import referenceArchitecture.datastore.DataStoreInterface;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;

public class WriteNode extends ComputeNode implements WriteRemoteInterface {
    private Storage storage;  
    private LogicalClock logicalClock;
    private Integer partition;
    private static DataStoreInterface dataStoreStub;
    private static final String dataStoreId = "data-store";

    public WriteNode(Storage storage, ScheduledThreadPoolExecutor scheduler, Integer partition) {
        super(scheduler, String.format("w%d", partition));
        this.storage = storage;
        this.logicalClock = new LogicalClock();
        this.partition = partition;
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new StoragePusher(storage, dataStoreStub, logicalClock, this.partition), 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: java WriteNode <partition:Int>");   
            return;
        }

        try {
            Integer partition = Integer.parseInt(args[0]);

            if(!Config.isPartition(partition)) {
                System.err.println(String.format("Error: Partition %p does not exist", partition));   
                return;
            }
    
            Storage storage = new Storage();
            storage.init(Config.getKeys(partition));
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(0);
            WriteNode writeNode = new WriteNode(storage, scheduler, partition);

            // Bind the remote object's stub in the registry
            WriteRemoteInterface stub = (WriteRemoteInterface) UnicastRemoteObject.exportObject(writeNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(writeNode.id, stub);

            // Get reference of data store
            dataStoreStub = (DataStoreInterface) registry.lookup(dataStoreId);
            writeNode.init();
        } catch (RemoteException e) {
            System.err.println("Could not get registry");
        } catch (AlreadyBoundException e) {
            System.err.println("Could not bind to registry");
        } catch (NotBoundException e) {
            System.err.println("Could not find the registry of the data store");
        } catch (NumberFormatException e) {
            System.err.println("Invalid partition");
            System.err.println("Usage: java WriteNode <region:String> <partion:Int>");   
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