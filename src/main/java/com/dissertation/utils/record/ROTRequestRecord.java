package com.dissertation.utils.record;

import org.json.JSONObject;

import com.google.protobuf.ProtocolStringList;

public class ROTRequestRecord extends Record {
    private final long id;
    private final ProtocolStringList keys;

    public ROTRequestRecord(NodeType nodeType, String nodeId, long id, ProtocolStringList keys, long time) {
        super(nodeType, nodeId, LogType.ROT_RECEIVE, time);
        this.id = id;
        this.keys = keys;
    }

    public ROTRequestRecord(NodeType nodeType, String nodeId, long id, ProtocolStringList keys) {
        super(nodeType, nodeId, LogType.ROT_RECEIVE);
        this.id = id;
        this.keys = keys;
    }

    @Override
   public JSONObject toJson() {
      return super.toJson()
            .put("id", this.id)
            .put("keys", this.keys);
   }
}
