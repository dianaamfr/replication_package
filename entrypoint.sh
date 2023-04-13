#!/bin/bash
exec java -Dpartitions=${PARTITIONS} -Ds3Endpoint=${S3_ENDPOINT} -DbucketSuffix=${BUCKET_SUFFIX} -DrotLogs=${ROT_LOGS} -DwriteLogs=${WRITE_LOGS} -jar /app/${NODE}.jar ${@}
