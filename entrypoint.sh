#!/bin/bash
exec java -Dpartitions=${PARTITIONS} -Ds3Endpoint=${S3_ENDPOINT} -DbucketSuffix=${BUCKET_SUFFIX} -DvisibilityLogs=${VISIBILITY_LOGS} -DgoodputLogs=${GOODPUT_LOGS} -jar /app/${NODE}.jar ${@}
