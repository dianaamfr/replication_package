#!/bin/bash
exec java -Dpartitions=${PARTITIONS} -Ds3Endpoint=${S3_ENDPOINT} -DbucketSuffix=${BUCKET_SUFFIX} -Dlogs=${LOGS} -jar /app/${NODE}.jar ${@}
