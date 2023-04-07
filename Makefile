.ONESHELL: # Applies to every targets in the file!
# Variables
partition1Bucket = reference-architecture-partition1
partition2Bucket = reference-architecture-partition2
clockBucket = reference-architecture-clock
s3Endpoint = http://localhost:4566

partitions = 2
regionPartitions = 2

readPort = 8080
readIp = 0.0.0.0

writePort1 = 8081
writeIp1 = 0.0.0.0
partitionId1 = 1

writePort2 = 8082
writeIp2 = 0.0.0.0
partitionId2 = 2

readAddress = $(readPort) $(readIp)
writeAddresses = $(writePort1) $(writeIp1) $(partitionId1) $(writePort2) $(writeIp2) $(partitionId2)

# Setup
all:
	make emptyBuckets
	mvn package

createBuckets:
	awslocal s3api create-bucket --bucket $(partition1Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(partition2Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(clockBucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)

emptyBuckets:
	awslocal s3 rm s3://$(partition1Bucket) --recursive
	awslocal s3 rm s3://$(partition2Bucket) --recursive
	awslocal s3 rm s3://$(clockBucket) --recursive

clear:
	make emptyBuckets
	mvn -q clean
	
# Compute Nodes
readNode:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/readNode.jar $(readPort) $(partitionId1) $(partitionId2)

writeNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/writeNode.jar $(partitionId1) $(writePort1)

writeNode2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/writeNode.jar $(partitionId2) $(writePort2)

# Validation
client:
	java -Dpartitions=$(partitions) -jar target/clientInterface.jar $(readAddress) $(writeAddresses)

writeGenerator:
	java -Dpartitions=$(partitions) -jar target/writeGenerator.jar

readGenerator:
	java -Dpartitions=$(partitions) -jar target/readGenerator.jar $(regionPartitions) $(readAddress) $(writeAddresses)