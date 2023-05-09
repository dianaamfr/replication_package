.ONESHELL: # Applies to every targets in the file!
# Variables
suffix=-reference-architecture
partition1Bucket = p1-us-east-1$(suffix)
partition2Bucket = p2-us-east-1$(suffix)
s3Endpoint = http://localhost:4566
region = us-east-1

partitions = 2
regionPartitions = 2

# Setup
all:
	mvn clean
	mvn package

clear:
	rm -rf logs/*.json
	mvn -q clean

# LocalStack
createBuckets:
	awslocal s3api create-bucket --bucket $(partition1Bucket) --region $(region)
	awslocal s3api create-bucket --bucket $(partition2Bucket) --region $(region)

emptyBuckets:
	awslocal s3 rm s3://$(partition1Bucket) --recursive
	awslocal s3 rm s3://$(partition2Bucket) --recursive
	
# CLI
client:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/clientInterface.jar

# Validation

totalWrites = 110
keys = a b
writeDelay = 200

readTest1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/busyReadGenerator.jar $(totalWrites) $(keys)

writeTest1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/constantWriteGenerator.jar $(writeDelay) $(totalWrites) $(keys)

readDelay = 50

readTest2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/constantReadGenerator.jar $(readDelay) $(keys)

writeTest2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/busyWriteGenerator.jar $(keys)

	
