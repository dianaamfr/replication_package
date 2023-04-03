package com.dissertation.referencearchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.dissertation.referencearchitecture.remoteInterface.response.WriteResponse;

public interface WriteRemoteInterface extends Remote {
    public WriteResponse write(String key, byte[] value, String lastWriteTimestamp) throws RemoteException;
}