.ONESHELL: 

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

###############
# Local Setup #
###############

all:
	mvn clean
	mvn package

clear:
	rm -rf logs/*.json
	mvn -q clean

createBuckets:
	awslocal s3api create-bucket --bucket $(partition1Bucket) --region $(region)
	awslocal s3api create-bucket --bucket $(partition2Bucket) --region $(region)
	awslocal s3api create-bucket --bucket $(clockBucket) --region $(region)

emptyBuckets:
	awslocal s3 rm s3://$(partition1Bucket) --recursive
	awslocal s3 rm s3://$(partition2Bucket) --recursive
	awslocal s3 rm s3://$(clockBucket) --recursive


#################
# Compute Nodes #
#################

readNode:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/readNode.jar $(readPort1) $(partitionId1) $(partitionId2)

writeNode1:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId1) $(writePort1) $(readAddress)

writeNode2:
	java -Ds3Endpoint=$(s3Endpoint) -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/writeNode.jar $(partitionId2) $(writePort2) $(readAddress)

#######
# CLI #
#######

client:
	java -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/clientInterface.jar $(readAddress) $(writeAddresses)


#################################
# Single Client Load Generators #
#################################

keys = a b
writeDelay = 50
totalWrites = 110

keysPerRead = 2
readDelay = 50
readTime = 20000

# Single client read and write generators to measure latency with constant throughput 
singleBusyRead:
	java -DvisibilityLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/busyReadGenerator.jar $(regionPartitions) $(readAddress) $(writeAddresses) $(totalWrites) $(keysPerRead) $(keys)

singleConstantWrite:
	java -DvisibilityLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/constantWriteGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(writeDelay) $(totalWrites) $(keys)


# Single client read and write generators to measure goodput with constant latency
singleConstantRead:
	java -DgoodputLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/constantReadGenerator.jar $(regionPartitions) $(readAddress) $(writeAddresses) $(readDelay) $(readTime) $(keysPerRead) $(keys)

singleBusyWrite:
	java -DgoodputLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/busyWriteGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(keys)


##############################################
######## Multi Client Load Generators ########
##############################################

writeClients = 1
keysPerPartition = 10
writesPerClient = 100

multiReadTime = 20000
readClients = 19

# 0.95 readers, 0.05 writers
# 5 to 95 (100)
# 30 to 570 (600)
# 60 to 1140 (1200)
# 90 to 1710 (1800)
# 120 to 2280 (2400)
# 150 to 2850 (3000)

# Multi client read and write generators to measure latency with constant throughput 
multiBusyRead:
	java -DvisibilityLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/multiBusyReadGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(multiReadTime) $(keysPerRead) $(keysPerPartition) $(readClients)

multiConstantWrite:
	java -DvisibilityLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/multiConstantWriteGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(writeDelay) $(writesPerClient) $(keysPerPartition) $(writeClients)

# Multi client read and write generators to measure goodput with constant latency
multiConstantRead:
	java -DvisibilityLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/multiConstantReadGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(readDelay) $(multiReadTime) $(keysPerRead) $(keysPerPartition) $(readClients)

multiBusyWrite:
	java -DvisibilityLogs=true -Dpartitions=$(partitions) -DbucketSuffix=$(suffix) -jar target/multiBusyWriteGenerator.jar  $(regionPartitions) $(readAddress) $(writeAddresses) $(keysPerPartition) $(writeClients)
