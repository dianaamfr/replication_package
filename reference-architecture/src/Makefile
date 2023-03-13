all:
	javac ./referenceArchitecture/client/*.java \
	./referenceArchitecture/compute/*.java \
	./referenceArchitecture/remoteInterface/*.java

clear:
	del ./referenceArchitecture/client/*/*.class && \
	./referenceArchitecture/compute/*.class && \
	./referenceArchitecture/remoteInterface/*.class

rmi:
	rmiregistry &
	sleep 0.5

client:
	java referenceArchitecture.client.Client

readNode:
	java referenceArchitecture.compute.ReadNode

writeNode:
	java referenceArchitecture.compute.WriteNode