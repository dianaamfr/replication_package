all:
	javac ./referenceArchitecture/client/*.java \
	./referenceArchitecture/compute/*.java \
	./referenceArchitecture/remoteInterface/*.java

clear:
	rm ./referenceArchitecture/client/*.class
	rm ./referenceArchitecture/compute/*.class
	rm ./referenceArchitecture/remoteInterface/*.class
	killall rmiregistry

rmi:
	rmiregistry &
	sleep 0.5

client:
	java referenceArchitecture.client.Client

readNode:
	java referenceArchitecture.compute.ReadNode

writeNode:
	java referenceArchitecture.compute.WriteNode