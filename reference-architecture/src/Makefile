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

client:
	java referenceArchitecture.client.Client

readNode:
	java referenceArchitecture.compute.ReadNode

writeNode:
	java referenceArchitecture.compute.WriteNode

datastore:
	java referenceArchitecture.datastore.DataStore