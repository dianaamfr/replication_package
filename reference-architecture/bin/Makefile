all:
	javac referenceArchitecture/*.java

rmi:
	rmiregistry &
	sleep 0.5

client:
	java referenceArchitecture.Client

server:
	java referenceArchitecture.Server