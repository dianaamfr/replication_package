package referenceArchitecture.remoteInterface;

import java.rmi.Remote;

public interface WriteRemoteInterface extends Remote {
    public Integer write(String key);
}