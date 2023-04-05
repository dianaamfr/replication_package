.ONESHELL: # Applies to every targets in the file!
region = eu-north-1
partition1Bucket = reference-architecture-partition1
partition2Bucket = reference-architecture-partition2
clockBucket = reference-architecture-clock

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
	java -jar target/readNode-jar-with-dependencies.jar

readNodeNorth:
	java -jar target/readNode-jar-with-dependencies.jar $(region)

writeNode1:
	java -jar target/writeNode-jar-with-dependencies.jar $(partition1Bucket)

writeNode2:
	java -jar target/writeNode-jar-with-dependencies.jar $(partition2Bucket)

# Validation
client:
	java -jar target/clientInterface-jar-with-dependencies.jar

clientNorth:
	java -jar target/clientInterface-jar-with-dependencies.jar $(region)

writeGenerator:
	java -jar target/writeGenerator-jar-with-dependencies.jar

readGenerator:
	java -jar target/readGenerator-jar-with-dependencies.jar