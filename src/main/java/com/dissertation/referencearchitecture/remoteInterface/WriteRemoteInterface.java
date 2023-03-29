package com.dissertation.referencearchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WriteRemoteInterface extends Remote {
    public String write(String key, byte[] value, String lastWriteTimestamp) throws RemoteException;
}