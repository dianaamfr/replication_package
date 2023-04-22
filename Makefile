.ONESHELL: # Applies to every targets in the file!
# Variables
suffix=-reference-architecture
partition1Bucket = p1-us-east-1$(suffix)
partition2Bucket = p2-us-east-1$(suffix)
clockBucket = clock$(suffix)
s3Endpoint = http://localhost:4566
region = us-east-1

partitions = 2
regionPartitions = 2

readPort1 = 8080
readIp1 = 0.0.0.0

writePort1 = 8081
writeIp1 = 0.0.0.0
partitionId1 = 1

writePort2 = 8082
writeIp2 = 0.0.0.0
partitionId2 = 2

readAddress = $(readPort1) $(readIp1)
writeAddresses = $(writePort1) $(writeIp1) $(partitionId1) $(writePort2) $(writeIp2) $(partitionId2)

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
	awslocal s3api create-bucket --bucket $(clockBucket) --region $(region)

emptyBuckets:
	awslocal s3 rm s3://$(partition1Bucket) --recursive
	awslocal s3 rm s3://$(partition2Bucket) --recursive
	awslocal s3 rm s3://$(clockBucket) --recursive
	
# Compute Nodes
readNode:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/readNode.jar $(readPort1) $(partitionId1) $(partitionId2)

writeNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId1) $(writePort1)

writeNode2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId2) $(writePort2)

# CLI
client:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/clientInterface.jar $(readAddress) $(writeAddresses)

# Validation

totalWrites = 100
keys = a b
writeDelay = 200

readTest1:
	java -Dlogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/busyReadGenerator.jar $(regionPartitions) $(readAddress) $(writeAddresses) $(totalWrites) $(keys)

writeTest1:
	java -Dlogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/constantWriteGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(writeDelay) $(totalWrites) $(keys)

totalReads = 100
readDelay = 500

readTest2:
	java -Dlogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/constantReadGenerator.jar $(regionPartitions) $(readAddress) $(writeAddresses) $(readDelay) $(totalReads) $(keys)

writeTest2:
	java -Dlogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/busyWriteGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(keys)

	
