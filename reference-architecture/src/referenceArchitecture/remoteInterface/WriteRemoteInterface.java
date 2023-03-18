package referenceArchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WriteRemoteInterface extends Remote {
    public long write(String key, Integer value, long lastWriteTimestamp) throws RemoteException;
}