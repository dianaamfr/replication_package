.ONESHELL: # Applies to every targets in the file!

args = -Dexec.args
classPath = com.dissertation.referencearchitecture
run = mvn -q exec:java -Dexec.mainClass

all:
	mvn clean install
	make rmi

clear:
	mvn -q clean;
	
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
