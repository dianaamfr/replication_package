all:
	javac ./referenceArchitecture/*/*.java
	javac ./referenceArchitecture/*/*/*.java

clear:
	rm ./referenceArchitecture/*/*.class
	rm ./referenceArchitecture/*/*/*.class

rmi:
	rmiregistry &
	sleep 0.5

stop-rmi:
	killall rmiregistry

client:
	java referenceArchitecture.client.Client

readNode:
	java referenceArchitecture.compute.ReadNode

writeNode:
	java referenceArchitecture.compute.WriteNode