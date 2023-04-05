package com.dissertation.referencearchitecture.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.InvalidRegionException;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.referencearchitecture.remoteInterface.ReadRemoteInterface;
import com.dissertation.referencearchitecture.remoteInterface.WriteRemoteInterface;
import com.dissertation.referencearchitecture.remoteInterface.response.ROTError;
import com.dissertation.referencearchitecture.remoteInterface.response.ROTResponse;
import com.dissertation.referencearchitecture.remoteInterface.response.WriteError;
import com.dissertation.referencearchitecture.remoteInterface.response.WriteResponse;
import com.dissertation.utils.Utils;

import software.amazon.awssdk.regions.Region;

public class Client {
    private Map<String, WriteRemoteInterface> writeStubs;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;
    private ReadRemoteInterface readStub;
    private Region region;

    public Client(Region region) throws InvalidRegionException, RemoteException, NotBoundException {
        if(!Config.isRegion(region)) {
            throw new InvalidRegionException();
        }

        this.region = region;
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = Utils.MIN_TIMESTAMP;
        initStubs();
    }

    public void initStubs() throws RemoteException, NotBoundException {
        Set<String> partitions = Config.getPartitions(this.region);
        String readNodeId = String.format("r%s", this.region.toString());
        Registry registry = LocateRegistry.getRegistry();
        this.readStub = (ReadRemoteInterface) registry.lookup(readNodeId);
        this.writeStubs = new HashMap<>();

        for(String partition: partitions) {
            String writeNodeId = String.format("w%s", partition);
            WriteRemoteInterface writeStub = (WriteRemoteInterface) registry.lookup(writeNodeId);
            this.writeStubs.put(partition, writeStub);
        }
    }


    public ROTResponse requestROT(Set<String> keys) {
        try {
            for(String key: keys) {
                if(!Config.isKeyInRegion(this.region, key)) {
                    throw new KeyNotFoundException();
                }
            }
            ROTResponse rotResponse = this.readStub.rot(keys);
            if(!rotResponse.isError()) {
                pruneCache(rotResponse.getStableTime());
                return new ROTResponse(getReadResponse(rotResponse.getValues()), rotResponse.getStableTime());
            }
            return rotResponse;
        } catch (RemoteException | KeyNotFoundException e) {
            return new ROTError(e.toString());
        }
    }

    public WriteResponse requestWrite(String key, byte[] value) {
        try {
            String partition = Config.getKeyPartition(this.region, key);
            WriteRemoteInterface writeStub = this.writeStubs.get(partition);
            WriteResponse writeResponse = writeStub.write(key, value, this.lastWriteTimestamp);

            if(!writeResponse.isError()) {
                this.lastWriteTimestamp = writeResponse.getTimestamp();
                this.cache.put(key, new Version(key, value, this.lastWriteTimestamp));
            }
            return writeResponse;
        } catch (KeyNotFoundException | RemoteException e) {
            return new WriteError(e.toString());
        }
    }

    private void pruneCache(String stableTime) {
        List<String> toPrune = new ArrayList<>();
        for(Entry<String,Version> entry :this.cache.entrySet()) {
            if(entry.getValue().getTimestamp().compareTo(stableTime) <= 0) {
                toPrune.add(entry.getKey());
            }   
        }
        this.cache.keySet().removeAll(toPrune);
    }

    private Map<String, byte[]> getReadResponse(Map<String, byte[]> response) {
        Map<String, byte[]> values = new HashMap<>();

        for(Entry<String, byte[]> entry: response.entrySet()) {
            Version v = this.cache.getOrDefault(entry.getKey(), null);
            values.put(entry.getKey(), v != null ? v.getValue() : entry.getValue());
        }
        return values;
    }
}
