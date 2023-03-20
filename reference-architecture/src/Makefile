all:
	jar xf ./lib/json-20140107.jar
	javac ./referenceArchitecture/*/*.java
	javac ./referenceArchitecture/*/*/*.java

clear:
	rm ./referenceArchitecture/*/*.class
	rm ./referenceArchitecture/*/*/*.class
	make rmiStop

rmi:
	rmiregistry &
	sleep 0.5

rmiStop:
	killall rmiregistry

clientA:
	java referenceArchitecture.client.Client A

clientB:
	java referenceArchitecture.client.Client B

readNodeA:
	java referenceArchitecture.compute.ReadNode A

writeNode1:
	java referenceArchitecture.compute.WriteNode 1

writeNode2:
	java referenceArchitecture.compute.WriteNode 2

readNodeB:
	java referenceArchitecture.compute.ReadNode B

datastore:
	java referenceArchitecture.datastore.DataStore