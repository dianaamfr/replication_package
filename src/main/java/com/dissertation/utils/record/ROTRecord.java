package com.dissertation.utils.record;

import org.json.JSONObject;

public class ROTRecord extends Record {
    private final long id;
    private final Phase phase;

    public ROTRecord(NodeType nodeType, LogType logType, String nodeId, long id, Phase phase, long time) {
        super(nodeType, nodeId, logType, time);
        this.id = id;
        this.phase = phase;
    }

    public ROTRecord(NodeType nodeType, LogType logType, String nodeId, Phase phase, long id) {
        super(nodeType, nodeId, logType);
        this.id = id;
        this.phase = phase;
    }

    @Override
   public JSONObject toJson() {
      return super.toJson()
            .put("id", this.id)
            .put("phase", this.phase);
   }
}
