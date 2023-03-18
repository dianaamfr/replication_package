package referenceArchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ReadRemoteInterface extends Remote {   
    public ROTResponse rot(Set<String> readSet) throws RemoteException;
}
