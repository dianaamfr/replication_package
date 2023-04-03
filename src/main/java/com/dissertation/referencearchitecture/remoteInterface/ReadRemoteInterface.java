package com.dissertation.referencearchitecture.remoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import com.dissertation.referencearchitecture.remoteInterface.response.ROTResponse;

public interface ReadRemoteInterface extends Remote {   
    public ROTResponse rot(Set<String> readSet) throws RemoteException;
}
