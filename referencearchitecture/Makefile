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

clientA:
	$(run)="$(classPath).client.Client" $(args)="A"

clientB:
	$(run)="$(classPath).client.Client" $(args)="B"

readNodeA:
	$(run)="$(classPath).compute.ReadNode" $(args)="A"

readNodeB:
	$(run)="$(classPath).compute.ReadNode" $(args)="B"

writeNode1:
	$(run)="$(classPath).compute.WriteNode" $(args)="1"

writeNode2:
	$(run)="$(classPath).compute.WriteNode" $(args)="2"

datastore:
	$(run)="$(classPath).datastore.DataStore"