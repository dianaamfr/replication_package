package com.dissertation.validation.logs;

import org.json.JSONObject;

public class ROTRequestLog extends Log {
    private final long id;

    public ROTRequestLog(NodeType nodeType, String nodeId, long id, long time) {
        super(nodeType, nodeId, LogType.ROT_REQUEST, time);
        this.id = id;
    }

    public ROTRequestLog(NodeType nodeType, String nodeId, long id) {
        super(nodeType, nodeId, LogType.ROT_REQUEST);
        this.id = id;
    }

    @Override
   public JSONObject toJson() {
      return super.toJson()
            .put("id", this.id);
   }
}
