.ONESHELL: # Applies to every targets in the file!

# Setup
all:
	make emptyBuckets
	mvn package
	make rmi

createBuckets:
	awslocal s3api create-bucket --bucket partition1 --region us-east-1
	awslocal s3api create-bucket --bucket partition2 --region us-east-1
	awslocal s3api create-bucket --bucket partition3 --region us-west-1 --create-bucket-configuration LocationConstraint=us-west-1
	awslocal s3api create-bucket --bucket clock --region us-east-1

emptyBuckets:
	awslocal s3 rm s3://partition1 --recursive
	awslocal s3 rm s3://partition2 --recursive
	awslocal s3 rm s3://partition3 --recursive
	awslocal s3 rm s3://clock --recursive

clear:
	make emptyBuckets
	mvn -q clean
	
rmi:
	cd target/classes
	rmiregistry
	sleep 0.5

# Compute Nodes
readNodeWest:
	java -jar target/readNode-jar-with-dependencies.jar us-west-1

readNodeEast:
	java -jar target/readNode-jar-with-dependencies.jar us-east-1

writeNode1:
	java -jar target/writeNode-jar-with-dependencies.jar partition1

writeNode2:
	java -jar target/writeNode-jar-with-dependencies.jar partition2

writeNode3:
	java -jar target/writeNode-jar-with-dependencies.jar partition3

# Validation
clientWest:
	java -jar target/clientInterface-jar-with-dependencies.jar us-west-1

clientEast:
	java -jar target/clientInterface-jar-with-dependencies.jar us-east-1

writeGenerator:
	java -jar target/writeGenerator-jar-with-dependencies.jar

readGenerator:
	java -jar target/readGenerator-jar-with-dependencies.jar