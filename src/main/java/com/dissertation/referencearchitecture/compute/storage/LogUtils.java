package com.dissertation.referencearchitecture.compute.storage;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class LogUtils {

    public static JSONObject toJson(ConcurrentMap<String, VersionChain> state, long clockValue) {
        JSONObject stateJson = new JSONObject();
        JSONArray versionChainsJson = new JSONArray();

        // For each key
        for(Entry<String, VersionChain> versionChain: state.entrySet()) {
            JSONObject versionChainJson = new JSONObject(); // Version chain of the key
            JSONArray versionsJson = new JSONArray(); // Array of versions of a key
            // For each version of the key that belongs to the snapshot defined by clockValue
            for(Entry<Long, Integer> version: versionChain.getValue().getVersionChain(clockValue).entrySet()) {
                JSONObject versionJson = new JSONObject();
                versionJson.put("key", version.getKey().toString());
                versionJson.put("value", version.getValue());
                versionsJson.put(versionJson);
            }
            versionChainJson.put("key", versionChain.getKey());
            versionChainJson.put("value", versionsJson);
            versionChainsJson.put(versionChainJson);
        }

        stateJson.put("state", versionChainsJson);
        return stateJson;
    }
}
