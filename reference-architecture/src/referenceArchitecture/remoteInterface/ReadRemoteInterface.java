package referenceArchitecture.remoteInterface;

import java.rmi.Remote;
import java.util.Map;
import java.util.Set;

public interface ReadRemoteInterface extends Remote {
    public Map<String, Integer> rot(Set<String> readSet);
}
