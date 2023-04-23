#!/bin/bash
exec java -Dpartitions=${PARTITIONS} -Ds3Endpoint=${S3_ENDPOINT} -DbucketSuffix=${BUCKET_SUFFIX} -DlatencyLogs=${LATENCY_LOGS} -DgoodputLogs=${GOODPUT_LOGS} -jar /app/${NODE}.jar ${@}
