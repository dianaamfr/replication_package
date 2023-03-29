.ONESHELL: # Applies to every targets in the file!

args = -Dexec.args
classPath = com.dissertation.referencearchitecture
validationPath = com.dissertation.validation
run = mvn -q exec:java -Dexec.mainClass

# Setup
all:
	make emptyBuckets
	mvn clean install
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
	mvn -q clean
	
rmi:
	cd target/classes
	rmiregistry
	sleep 0.5

# Compute Nodes
readNodeWest:
	$(run)="$(classPath).compute.ReadNode"  $(args)="us-west-1" -e

readNodeEast:
	$(run)="$(classPath).compute.ReadNode"  $(args)="us-east-1" -e

writeNode1:
	$(run)="$(classPath).compute.WriteNode" $(args)="partition1" -e

writeNode2:
	$(run)="$(classPath).compute.WriteNode" $(args)="partition2" -e

writeNode3:
	$(run)="$(classPath).compute.WriteNode" $(args)="partition3" -e

# Validation
clientWest:
	$(run)="$(validationPath).ClientInterface" $(args)="us-west-1" -e

clientEast:
	$(run)="$(validationPath).ClientInterface" $(args)="us-east-1" -e
