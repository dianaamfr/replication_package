package com.dissertation.utils.log;

import com.dissertation.utils.Utils;

import software.amazon.awssdk.regions.Region;

public class Log {
   private final String version;
   private final String key;
   private final Region region;
   private final String nodeId;
   private final Operation operation;
   private final long time;

   public enum Operation {
      WRITE,
      STABLE,
      LOG_PULL,
      LOG_PUSH
   }

   public Log(String version, String key, String nodeId, Operation operation) {
      this.key = key;
      this.version = version;
      this.region = Utils.getCurrentRegion();
      this.nodeId = nodeId;
      this.time = System.currentTimeMillis();
      this.operation = operation;
   }

   public Log(String version, String nodeId, Operation operation) {
      this(version, "", nodeId, operation);
   }

   @Override
   public String toString() {
       return String.format("%s, %s, %s, %s, %s, %d", this.version, this.key, this.region.toString(), this.nodeId, this.operation.name(), this.time);
   }
}
