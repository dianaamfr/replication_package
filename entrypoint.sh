#!/bin/bash
exec java -Dpartitions=${PARTITIONS} -Ds3Endpoint=${S3_ENDPOINT} -DclockRegion=${CLOCK_REGION} -DbucketSuffix=${BUCKET_SUFFIX} -DvisibilityLogs=${VISIBILITY_LOGS} -DgoodputLogs=${GOODPUT_LOGS} -Dcheckpointing=${CHECKPOINTING} -DlogDelay=${LOG_DELAY} -jar /app/${NODE}.jar ${@}
