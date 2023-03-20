package referenceArchitecture.datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataStoreInterface extends Remote {

    public void write(String key, String string, Integer partition) throws RemoteException;
    
    public String read(String key, Integer partition) throws RemoteException;
}
