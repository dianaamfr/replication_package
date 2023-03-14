package referenceArchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WriteRemoteInterface extends Remote {
    public Integer write(String key) throws RemoteException;
}