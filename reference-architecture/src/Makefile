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

readNodeA:
	java referenceArchitecture.compute.ReadNode A

writeNodeA1:
	java referenceArchitecture.compute.WriteNode A 1

writeNodeA2:
	java referenceArchitecture.compute.WriteNode A 2

readNodeB:
	java referenceArchitecture.compute.ReadNode B

writeNodeB1:
	java referenceArchitecture.compute.WriteNode B 1

datastore:
	java referenceArchitecture.datastore.DataStore