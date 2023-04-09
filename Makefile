.ONESHELL: # Applies to every targets in the file!
# Variables
partition1Bucket = reference-architecture-partition1
partition2Bucket = reference-architecture-partition2
partition3Bucket = reference-architecture-partition3
clockBucket = reference-architecture-clock
s3Endpoint = http://localhost:4566

partitions = 3
region1Partitions = 2
region2Partitions = 1
region = eu-north-1

readPort1 = 8080
readIp1 = 0.0.0.0

writePort1 = 8081
writeIp1 = 0.0.0.0
partitionId1 = 1

writePort2 = 8082
writeIp2 = 0.0.0.0
partitionId2 = 2

readPort2 = 8083
readIp2 = 0.0.0.0

writePort3 = 8084
writeIp3 = 0.0.0.0
partitionId3 = 3

readAddress1 = $(readPort1) $(readIp1)
writeAddresses1 = $(writePort1) $(writeIp1) $(partitionId1) $(writePort2) $(writeIp2) $(partitionId2)

readAddress2 = $(readPort2) $(readIp2)
writeAddresses2 = $(writePort3) $(writeIp3) $(partitionId3)

# Setup
all:
	make emptyBuckets
	mvn package

createBuckets:
	awslocal s3api create-bucket --bucket $(partition1Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(partition2Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(partition3Bucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)
	awslocal s3api create-bucket --bucket $(clockBucket) --region $(region) --create-bucket-configuration LocationConstraint=$(region)

emptyBuckets:
	awslocal s3 rm s3://$(partition1Bucket) --recursive
	awslocal s3 rm s3://$(partition2Bucket) --recursive
	awslocal s3 rm s3://$(partition3Bucket) --recursive
	awslocal s3 rm s3://$(clockBucket) --recursive

clear:
	make emptyBuckets
	mvn -q clean
	
# Compute Nodes
readNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/readNode.jar $(readPort1) $(partitionId1) $(partitionId2)

readNode2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/readNode.jar $(readPort2) $(partitionId3)

writeNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/writeNode.jar $(partitionId1) $(writePort1)

writeNode2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/writeNode.jar $(partitionId2) $(writePort2)

writeNode3:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -jar target/writeNode.jar $(partitionId3) $(writePort3)

# Validation
client1:
	java -Dpartitions=$(partitions) -jar target/clientInterface.jar $(readAddress1) $(writeAddresses1)

client2:
	java -Dpartitions=$(partitions) -jar target/clientInterface.jar $(readAddress2) $(writeAddresses2)

writeGenerator1:
	java -Dpartitions=$(partitions) -jar target/writeGenerator.jar  $(region1Partitions) $(readAddress1) $(writeAddresses1)

readGenerator1:
	java -Dpartitions=$(partitions) -jar target/readGenerator.jar $(region1Partitions) $(readAddress1) $(writeAddresses1)

writeGenerator2:
	java -Dpartitions=$(partitions) -jar target/writeGenerator.jar  $(region2Partitions) $(readAddress2) $(writeAddresses2)

readGenerator2:
	java -Dpartitions=$(partitions) -jar target/readGenerator.jar $(region2Partitions) $(readAddress2) $(writeAddresses2)
