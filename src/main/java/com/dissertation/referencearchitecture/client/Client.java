package com.dissertation.referencearchitecture.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.dissertation.ROTRequest;
import com.dissertation.ROTResponse;
import com.dissertation.ROTServiceGrpc;
import com.dissertation.WriteRequest;
import com.dissertation.WriteResponse;
import com.dissertation.WriteServiceGrpc;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class Client {
    private Map<Integer, WriteServiceGrpc.WriteServiceBlockingStub> writeStubs;
    private ROTServiceGrpc.ROTServiceBlockingStub readStub;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;
    
    public Client(Address readAddress, List<Address> writeAddresses) {
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = Utils.MIN_TIMESTAMP;
        initStubs(readAddress, writeAddresses);
    }

    public void initStubs(Address readAddress, List<Address> writeAddresses) {
        this.readStub = ROTServiceGrpc.newBlockingStub(readAddress.getChannel());
        this.writeStubs = new HashMap<>();

        for (Address address : writeAddresses) {
            WriteServiceGrpc.WriteServiceBlockingStub writeStub = WriteServiceGrpc
                    .newBlockingStub(address.getChannel());
            this.writeStubs.put(address.getPartitionId(), writeStub);
        }
    }

    public ROTResponse requestROT(Set<String> keys) {
        // TODO: check if keys are available in region
        ROTRequest rotRequest = ROTRequest.newBuilder().addAllKeys(keys).build();
        ROTResponse rotResponse = this.readStub.rot(rotRequest);
        // TODO: prune cache
        // consult cache and return response with correct values
        return rotResponse;
        // try {
        // for(String key: keys) {
        // if(!Config.isKeyInRegion(this.region, key)) {
        // throw new KeyNotFoundException();
        // }
        // }
        // ROTResponse rotResponse = this.readStub.rot(
        // ROTRequest.newBuilder().addAllKeys(keys).build();
        // );

        // // if(!rotResponse.isError()) {
        // // pruneCache(rotResponse.getStableTime());
        // // return new ROTResponse(getReadResponse(rotResponse.getValues()),
        // rotResponse.getStableTime());
        // // }
        // return rotResponse;
        // } catch (KeyNotFoundException e) {
        // return new ROTError(e.toString());
        // }
    }

    public WriteResponse requestWrite(String key, byte[] value) {
        //try {
            int partitionId = Utils.getKeyPartitionId(key);
            // if(!this.writeStubs.containsKey(partitionId)) {
            //     // TODO: return error response
            // }
            WriteServiceGrpc.WriteServiceBlockingStub writeStub = this.writeStubs.get(partitionId);
            WriteRequest writeRequest = WriteRequest.newBuilder()
                .setKey(key)
                .setValue(ByteString.copyFrom(value))
                .setLastWriteTimestamp(this.lastWriteTimestamp)
                .build();

            WriteResponse writeResponse = writeStub.write(writeRequest);
            System.out.println(writeResponse.getWriteTimestamp());
            this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
            this.cache.put(key, new Version(key, value, this.lastWriteTimestamp));

            // if (!writeResponse.isError()) {
            //     this.lastWriteTimestamp = writeResponse.getTimestamp();
            //     this.cache.put(key, new Version(key, value, this.lastWriteTimestamp));
            // }
            return writeResponse;
        // } catch (KeyNotFoundException) {
        //     return new WriteError(e.toString());
        // }
    }

    private void pruneCache(String stableTime) {
        List<String> toPrune = new ArrayList<>();
        for (Entry<String, Version> entry : this.cache.entrySet()) {
            if (entry.getValue().getTimestamp().compareTo(stableTime) <= 0) {
                toPrune.add(entry.getKey());
            }
        }
        this.cache.keySet().removeAll(toPrune);
    }

    private Map<String, byte[]> getReadResponse(Map<String, byte[]> response) {
        Map<String, byte[]> values = new HashMap<>();

        for (Entry<String, byte[]> entry : response.entrySet()) {
            Version v = this.cache.getOrDefault(entry.getKey(), null);
            values.put(entry.getKey(), v != null ? v.getValue() : entry.getValue());
        }
        return values;
    }

}
