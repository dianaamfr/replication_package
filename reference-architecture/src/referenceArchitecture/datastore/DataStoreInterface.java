package referenceArchitecture.datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataStoreInterface extends Remote {

    public void write(String key, String string) throws RemoteException;
    
    public String read(String key) throws RemoteException;
}
