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
import com.dissertation.referencearchitecture.remoteInterface.ROTResponse;
import com.dissertation.referencearchitecture.remoteInterface.ReadRemoteInterface;
import com.dissertation.referencearchitecture.remoteInterface.WriteRemoteInterface;
import com.dissertation.utils.Utils;

public class Client {
    private Map<String, WriteRemoteInterface> writeStubs;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;
    private ReadRemoteInterface readStub;
    private String region;

    public Client(String region) throws InvalidRegionException, RemoteException, NotBoundException {
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
        String readNodeId = String.format("r%s", this.region);
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
        ROTResponse result = null;

        try {
            for(String key: keys) {
                if(!Config.isKeyInRegion(this.region, key)) {
                    throw new KeyNotFoundException();
                }
            }
            ROTResponse rotResponse = this.readStub.rot(keys);
            pruneCache(rotResponse.getStableTime());
            result = new ROTResponse(getReadResponse(rotResponse.getValues()), rotResponse.getStableTime());
        } catch (RemoteException e) {
            System.err.println("Error: Could not connect with server");
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Not all keys are available in region %s", this.region));
        }

        return result;
    }

    public String requestWrite(String key, byte[] value) {
        String result = null;

        try {
            String partition = Config.getKeyPartition(this.region, key);
            WriteRemoteInterface writeStub = this.writeStubs.get(partition);
            result = writeStub.write(key, value, this.lastWriteTimestamp);

            if(result != null) {
                this.lastWriteTimestamp = result;
                this.cache.put(key, new Version(key, value, this.lastWriteTimestamp));
            } else {
                System.err.println("Error: Write request failed");
            }
        } catch (KeyNotFoundException e) {
            System.err.println(String.format("Error: Key is not available in region %s", this.region));
        } catch (NumberFormatException e) {
            System.err.println("Error: Value must be an Integer");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Error: Could not connect with server");
        }
        return result;
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
