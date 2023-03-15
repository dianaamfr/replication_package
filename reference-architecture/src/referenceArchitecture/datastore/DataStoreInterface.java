package referenceArchitecture.datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataStoreInterface extends Remote {

    public void write(String key, Object value) throws RemoteException;
    
    public Object read(String key) throws RemoteException;
}
