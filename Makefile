.ONESHELL: # Applies to every targets in the file!
region = eu-north-1
partition1Bucket = reference-architecture-partition1
partition2Bucket = reference-architecture-partition2
clockBucket = reference-architecture-clock
s3Endpoint = http://localhost:4566
serverPort = 8080

# Setup
all:
	make emptyBuckets
	mvn package
	make rmi

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
	
rmi:
	cd target/classes
	rmiregistry
	sleep 0.5

# Compute Nodes
readNode:
	java -jar target/readNode.jar

readNodeNorth:
	java -Ds3Endpoint=$(s3Endpoint) -DserverPort=$(serverPort) -jar target/readNode.jar $(region)

writeNode1:
	java -Ds3Endpoint=$(s3Endpoint) -DserverPort=$(serverPort) -jar target/writeNode.jar $(partition1Bucket)

writeNode2:
	java -Ds3Endpoint=$(s3Endpoint) -DserverPort=$(serverPort) -jar target/writeNode.jar $(partition2Bucket)

# Validation
client:
	java -jar target/clientInterface.jar

clientNorth:
	java -jar target/clientInterface.jar $(region)

writeGenerator:
	java -jar target/writeGenerator.jar

readGenerator:
	java -jar target/readGenerator.jar