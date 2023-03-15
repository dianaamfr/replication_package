package referenceArchitecture.dataStore;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class DataStore implements DataStoreInterface {
    private static final String id = "data-store";
    Map<String, Object> objects;

    public DataStore() {
        this.objects = new HashMap<>();
    }

    @Override
    public void write(String key, Object value) throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'write'");
    }

    @Override
    public Object read(String key) throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

}
