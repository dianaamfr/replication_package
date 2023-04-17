.ONESHELL: # Applies to every targets in the file!
# Variables
suffix=-reference-architecture
partition1Bucket = p1-us-east-1$(suffix)
partition2Bucket = p2-us-east-1$(suffix)
s3Endpoint = http://localhost:4566
region = us-east-1

partitions = 2
region1Partitions = 2

localIp = 0.0.0.0
readPort1 = 8080
writePort1 = 8081
writePort2 = 8082

partitionId1 = 1
partitionId2 = 2

readAddress1 = $(readPort1) $(localIp)
writeAddresses1 = $(writePort1) $(localIp) $(partitionId1) $(writePort2) $(localIp) $(partitionId2)

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
	
# Compute Nodes
readNode1:
	java -DwriteLogs=true -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/readNode.jar $(readPort1) $(partitionId1) $(partitionId2)

writeNode1:
	java -DwriteLogs=true -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId1) $(writePort1)

writeNode2:
	java -DwriteLogs=true -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId2) $(writePort2)

# CLI
client1:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/clientInterface.jar $(readAddress1) $(writeAddresses1)

# Validation
readDelay1 = 100
totalReads1 = 500

readTest1:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/readGenerator.jar $(region1Partitions) $(readAddress1) $(writeAddresses1) $(readDelay1) $(totalReads1)


writeDelay1 = 1
bytes1 = 8
writesPerPartition1 = 100

writeTest1:
	java -DwriteLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeGenerator.jar  $(region1Partitions) $(readAddress1) $(writeAddresses1) $(writeDelay1) $(bytes1) $(writesPerPartition1)

	
