.ONESHELL: # Applies to every targets in the file!

args = -Dexec.args
classPath = com.dissertation.referencearchitecture
run = mvn -q exec:java -Dexec.mainClass

all:
	make emptyBuckets
	mvn clean install
	make rmi

createBuckets:
	awslocal s3api create-bucket --bucket partition1 --region us-east-1
	awslocal s3api create-bucket --bucket partition2 --region us-east-1
	awslocal s3api create-bucket --bucket partition3 --region us-west-1 --create-bucket-configuration LocationConstraint=us-west-1

emptyBuckets:
	awslocal s3 rm s3://partition1 --recursive
	awslocal s3 rm s3://partition2 --recursive
	awslocal s3 rm s3://partition3 --recursive

clear:
	mvn -q clean
	
rmi:
	cd target/classes
	rmiregistry
	sleep 0.5

rmiStop:
	killall rmiregistry

clientWest:
	$(run)="$(classPath).client.Client" $(args)="us-west-1"

clientEast:
	$(run)="$(classPath).client.Client" $(args)="us-east-1"

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
