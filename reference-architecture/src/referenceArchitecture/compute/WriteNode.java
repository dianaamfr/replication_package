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
import referenceArchitecture.compute.storage.Storage;
import referenceArchitecture.compute.storage.StoragePusher;
import referenceArchitecture.datastore.DataStoreInterface;
import referenceArchitecture.remoteInterface.WriteRemoteInterface;

public class WriteNode extends ComputeNode implements WriteRemoteInterface {  
    private LogicalClock logicalClock;
    private static DataStoreInterface dataStoreStub;
    private static final String dataStoreId = "data-store";

    public WriteNode(Storage storage, ScheduledThreadPoolExecutor scheduler, String region, Integer partition) {
        super(storage, scheduler, String.format("w%s%d", region, partition), region);
        this.logicalClock = new LogicalClock();
    }

    public void init() {
        this.scheduler.scheduleWithFixedDelay(new StoragePusher(storage, dataStoreStub, logicalClock), 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Usage: java WriteNode <region:String> <partition:Integer>");   
            return;
        }

        Storage storage = new Storage();
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(0);
        WriteNode writeNode = new WriteNode(storage, scheduler, args[0], Integer.parseInt(args[1]));

        try {
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
            System.err.println("Usage: java WriteNode <region:String> <partion:Integer>");   
        }
    }

    @Override
    public long write(String key, Integer value, Long lastWriteTimestamp) {
        // TODO: to support multiple writers,use lastWriteTimestamp (it can be null)
        long timestamp = this.logicalClock.internalEvent();
        this.storage.put(key, timestamp, value);
        return timestamp;
    }
}