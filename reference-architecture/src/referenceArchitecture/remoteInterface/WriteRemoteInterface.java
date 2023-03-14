package referenceArchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WriteRemoteInterface extends Remote {
    public String write(String key, Integer value, String lastWriteTimestamp) throws RemoteException;
}