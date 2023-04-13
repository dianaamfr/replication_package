.ONESHELL: # Applies to every targets in the file!
# Variables
suffix=-reference-architecture
partition1Bucket = partition1$(suffix)
partition2Bucket = partition2$(suffix)
clockBucket = clock$(suffix)
s3Endpoint = http://localhost:4566
region = eu-north-1

partitions = 2
region1Partitions = 2
region = eu-north-1

readPort1 = 8080
readIp1 = 0.0.0.0

writePort1 = 8081
writeIp1 = 0.0.0.0
partitionId1 = 1

writePort2 = 8082
writeIp2 = 0.0.0.0
partitionId2 = 2

readAddress1 = $(readPort1) $(readIp1)
writeAddresses1 = $(writePort1) $(writeIp1) $(partitionId1) $(writePort2) $(writeIp2) $(partitionId2)

# Setup
all:
	mvn clean
	mvn package

clear:
	rm -rf logs/*.json
	mvn -q clean

# LocalStack
createBuckets:
	awslocal s3api create-bucket --bucket $(partition1Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(partition2Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(clockBucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)

emptyBuckets:
	awslocal s3 rm s3://$(partition1Bucket) --recursive
	awslocal s3 rm s3://$(partition2Bucket) --recursive
	awslocal s3 rm s3://$(clockBucket) --recursive
	
# Compute Nodes
readNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/readNode.jar $(readPort1) $(partitionId1) $(partitionId2)

writeNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId1) $(writePort1)

writeNode2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId2) $(writePort2)

# CLI
client1:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/clientInterface.jar $(readAddress1) $(writeAddresses1)

# Validation
readDelay1 = 1
totalReads1 = 100

readTest1:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/readGenerator.jar $(region1Partitions) $(readAddress1) $(writeAddresses1) $(readDelay1) $(totalReads1)


writeDelay1 = 1
bytes1 = 8
writesPerPartition1 = 50

writeTest1:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeGenerator.jar  $(region1Partitions) $(readAddress1) $(writeAddresses1) $(writeDelay1) $(bytes1) $(writesPerPartition1)

	
