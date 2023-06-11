# Dissertation Work

A prototype implementation of a cloud-native causally consistent system. 

## Description

This repository holds a prototype implementation of a candidate reference architecture for a cloud-native causally consistent read-heavy system. It represents part of the work developed for my Master's dissertation. With this prototype, we aim to study the feasibility of a candidate reference architecture and identify any impediments and possible improvements.

For a more detailed description of the reference architecture please refer to [Reference Architecture](#candidate-reference-architecture).

This repository also holds an eventually consistent version of the prototype which served as baseline for comparison with the causally consistent prototype. Finally, it contains the necessary scripts to test the prototype in AWS, the scripts used for the evaluation and the plots and tables that resulted from our analysis.

## Prototype Features
### Features
- **Storage Layer**: Amazon S3 Simple Storage Service.
- **Compute Layer**: Read Nodes provide ROTs from the stable frontier whereas Write Nodes provide single-key writes and atomic writes.
- **Client Layer**: Forwards the operations to the appropriate Compute Nodes. Stores the client's writes until they are known to be stable.
- **Communication**: gRPC framework.
- **Clock**: Hybrid Logical Clock.
- **Consistency**: Provides Causal Consistency: ROTs are always performed from a stable time, read-you-writes is guaranteed for multiple writers by storing unstable writes in the client cache and monotonic writes are ensured by including the previous write timestamp of the client in subsequent requests.
- **Clock Synchronization**: Write Nodes asynchronously persists their last clock value in an S3 bucket. In the absence of updates, they fetch the last clock value from other partitions. If the fetched clock value is higher than its own, it advances its clock.
- **Check-pointing**: Periodically, write nodes replace a prefix of their log with a checkpoint. For that purpose, they get the minimum stable time from the Read Nodes that replicate their partition and only remove versions below the stable frontier established by that time (always keeping the latest stable version of each key).


## Structure
### Causally Consistent Prototype
A Maven project with the following structure:

`src/main` comprises the project's source code and follows the structure below:
- `java/com/dissertation`:
    - `referencearchitecture`: Comprises the classes that implement the candidate reference architecture.
        - `client`: Contains the `Client` class, which can be used to issue ROTs and write operations. It connects with the `ReadNode` of its region and with the `WriteNodes` of its region's partitions through gRPC. It keeps a "cache" with its unstable writes and his last write timestamp.
        - `compute`: Contains the `ReadNode` and `WriteNode`, respectively responsible for handling ROTs of a region and writes of a partition. Also contains the `storage` package, which comprises the classes used to store the log in-memory and to pull and push the log to the data store. Furthermore, it stores the classes related to the implementation of the Hybrid Logical Clock in the `clock` package.
        - `s3`: Provide the necessary functions to perform put and get operations in AWS S3.
    - `utils`: Util functions, constants and classes.
    - `evaluation`: Comprises classes that can be used to evaluate the prototype:
        - `logs`: Classes that represent logs of each relevant operation. Used for validating the prototype.
        - `singleClient`:
            - `ClientInterface`: To test the prototype through a command-line interface.
            - `BusyReadGenerator`: A single-threaded reader that issues read requests with no delay to the keys provided in the arguments.
            - `ConstantWriteGenerator`: A single-threaded writer that issues a configurable number of write requests at a fixed rate, alternating between the provided set of keys.
            - `ConstantReadGenerator`: A single-threaded reader that issues read requests with a fixed delay to all the keys provided in the arguments for the given amount of time.
            - `BusyWriteGenerator`: A single-threaded writer that issues write requests with no delay, alternating between the provided set of keys.
        - `multiClient`:
                - `BusyReadGenerator`: A multi-threaded reader that uses a client instance per thread to issue read requests with no delay.
            - `ConstantWriteGenerator`: A multi-threaded writer that uses a client instance per thread to issue write requests at a fixed rate.
            - `ConstantReadGenerator`: A multi-threaded reader that uses a client instance per thread to issue read requests with a fixed delay for the given amount of time.
            - `BusyWriteGenerator`: A multi-threaded writer that  uses a client instance per thread to issue write requests with no delay.
- `proto`: Holds the Protocol Buffers file that defines the services provided by read and write nodes.

### Eventually Consistent Baseline
For comparison with our causally consistent prototype, which uses S3 as the storage layer, we developed an eventually consistent baseline where clients issue read and write requests directly to S3. It consists of a Maven project with the following structure:

`eventual/src/main/java/com/dissertation`:
- `eventual`: Comprises the classes that implement the eventually consistent baseline
    - `client`: Contains the `Client` class, which can be used to issue read and write operations to S3.
    - `s3`: Provides the necessary functions to perform put and get operations in AWS S3.
    - `utils`: Util functions, constants and classes.
- `evaluation`: Comprises classes that can be used to evaluate the baseline:
    - `logs`: Classes that represent logs of each relevant operation. Used for validating the baseline.
    - `ClientInterface`: To test the prototype through a command-line interface.
    - `BusyReadGenerator`: A single-threaded reader that issues read requests with no delay to the keys provided in the arguments.
    - `ConstantWriteGenerator`: A single-threaded writer that issues a configurable number of write requests at a fixed rate, alternating between the provided set of keys.
    - `ConstantReadGenerator`: A single-threaded reader that issues read requests with a fixed delay to all the keys provided in the arguments for the given amount of time.
    - `BusyWriteGenerator`: A single-threaded writer that issues write requests with no delay, alternating between the provided set of keys.

### Evaluation
Holds the scripts that were used to evaluate the prototype in AWS and the source code that was used to perform the evaluation. It also holds the logs and results of the evaluation.

`evaluation`:
- `logs`: Stores the evaluation logs generated when running the evaluation scripts in AWS.
- `results`: Stores the plots and other results of the analysis of the logs. The `single_client` directory stores the results from the evaluation performed with the single-client load generators while the `multi_client` directory stores the results from the multi-client load generators.
- `aws-scripts`: Holds useful scripts for running each component of the prototype with docker and to generate the logs.
- `data_analysis`: A python package to analyze the logs generated during the evaluation and generate the necessary plots and tables.
- `README.md`: Describes the tests performed during the evaluation and includes the necessary commands to replicate them.

## Dependencies
**To test locally**:
- [LocalStack CLI](https://docs.localstack.cloud/getting-started/installation/) and [AWS Command Line Interface](https://docs.localstack.cloud/user-guide/integrations/aws-cli/).
- OpenJDK
- Maven

**To test in AWS**:
- EC2 instances must have docker installed, expose port 8080 and have access to S3 buckets.
- S3 buckets for each partition.

## Execution Instructions
### LocalStack (locally)
The project can be tested locally using LocalStack.

> For simplicity we provide some predefined commands in the `Makefile` of the root folder to test the prototype and also in the `Makefile` of the `eventual` folder to test the baseline.

#### Causally Consistent Prototype
**Setup S3 buckets and servers**:
1. Open a terminal and start LocalStack: `localstack start` 
2. Open a new terminal in the root folder
3. Build the project: `mvn package`
4. Create the buckets for each partition, always using the same suffix:
    `awslocal s3api create-bucket --bucket p<partitionId>-<region><suffix> --region <region>`
    *e.g. `awslocal s3api create-bucket --bucket p1-us-east-1-reference-architecture --region us-east-1` for partition 1* 
5. Create the clock bucket using the same suffix:
    `awslocal s3api create-bucket --bucket clock-reference-architecture --region <region>`
6. Start the Read Nodes, specifying the total number of partitions in the system (`nPartitions`) and the ids of the partitions that the read node keeps a copy of:
    `java -Ds3Endpoint=http://localhost:4566 -Dpartitions=<nPartitions> -DbucketSuffix=<suffix> -jar target/readNode.jar <readPort> <partitionId>*`
7. Start the Write Nodes, specifying the total number of partitions in the system (`nPartitions`), the id of the partition that the write node is responsible for and the read nodes that track his partition:
    `java -Ds3Endpoint=http://localhost:4566 -Dpartitions=<nPartitions> -DbucketSuffix=<suffix> -jar target/writeNode.jar <partitionId> <writePort> <readPort> <readIp>`

**Issue read and write requests**:
Using the command-line interface, you can test the behavior of the prototype locally.
1. Start the desired number of clients:
    `java -Dpartitions=<nPartitions> -DbucketSuffix=$(suffix) -jar target/clientInterface.jar <readPort> <readIp> (<writePort> <writeIp> <partitionId>)*`
2. Issue the desired ROT, write or atomic write requests:
    - ROT: `R x y` (reads the values of keys x and y)
    - Write: `W x 3` (writes the value 3 to key x)
    - Atomic write type A: `WA x 3 00000001685206329008-00000000000000000038` (writes the value 3 to key x if the current version of x has the specified timestamp)
    - Atomic write type B: `WB x 3 00000001685206329008-00000000000000000038 4` (writes the value 3 to key x if the current version of x has the specified timestamp and the value 4)
    - Atomic write type C: `WC x 3 4` (writes the value 3 to key x if the current version of x has the value 4)

#### Eventually Consistent Baseline
**Setup S3 buckets**:
1. Open a terminal and start LocalStack: `localstack start` 
2. Open a new terminal in the `eventual` folder
3. Build the project: `mvn package`
4. Create the buckets for each partition, always using the same suffix:
    `awslocal s3api create-bucket --bucket p<partitionId>-<region><suffix> --region <region>`
    *e.g. `awslocal s3api create-bucket --bucket p1-us-east-1-reference-architecture --region us-east-1` for partition 1* 

**Issue read and write requests**:
Using the command-line interface, you can test the behavior of the baseline locally.
1. Start the desired number of clients:
    `java -Ds3Endpoint=http://localhost:4566 -Dpartitions=<nPartitions> -DbucketSuffix=<suffix> -jar target/clientInterface.jar`
2. Issue the desired read and write requests:
    - Read: `R x` (reads the value of key x)
    - Write: `W x 3` (writes the value 3 to key x)

### AWS
For the evaluation in AWS, we built a Docker image for the prototype and for the baseline, namely `dianaamfreitas/dissertation` and `dianaamfreitas/dissertation-eventual`. The images supports linux/amd64 and linux/arm64 and our tests were performed using Ubuntu 22.04 LTS arm64.
To test goodput we used the image with tag `v14.0.0-goodput`. To test visibility we used the image with tag `v14.0.0-visibility` and for other metrics we used `v14.0.0`. All these have checkpointing and log pruning disabled. For the prototype's full version (with checkpointing and pruning) use version `v14.0.0-checkpointing`.

#### Causally Consistent Prototype
1. Create an EC2 instance for each system component (read and write nodes and cli or load generators) setting the user data to the contents of `docker-install.sh`, which installs docker on the instance. Make sure to set the security group's inbound rules so that port 8080 accepts TCP requests from the client instances.
2. Set the IAM Role of read and write nodes' instances to enable access to S3 buckets.
3. Update the `BUCKET_SUFFIX` of each component's script in `aws_scripts`. 
4. Setup the buckets using the `BUCKET_SUFFIX` defined above (e.g. the bucket of partition 1 must be named p1-region`BUCKET_SUFFIX`, and the clock bucket must be named clock`BUCKET_SUFFIX`). Setup the replication between buckets.
5. Connect to each instance and use the provided `aws_scripts` to run the read and write nodes:
    - **Read Node**: 
    `./readNode.sh <imageTag> <totalPartitions> <port> <regionPartitionsIds>`
    - **Write Node**: 
    `./writeNode.sh <imageTag> <totalPartitions> <port> <partitionId> (<readPort> <readIp>)+`
6. Run the CLI or the load generators provided in the `aws_scripts` directory:
    - **CLI**: 
    `./cli.sh <imageTag> <totalPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+`
    - Single-client generators:
        - **BusyReadGenerator**: 
        `./busyReadGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <expectedWrites> <keysPerRead> <key>+` 
        - **ConstantWriteGenerator**: ``
        - **BusyWriteGenerator**: 
        `./busyWriteGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <key>+`
        - **ConstantReadGenerator**: 
        `./constantReadGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <readDelay> <readTime> <keysPerRead> <key>+`
    - Multi-client generators: 
        - **BusyReadGenerator**: 
        `./multiBusyReadGenerator.sh <imageTag> <totalPartitions> <regionReadNodes> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <readTime> <keysPerRead> <keysPerPartition> <readClients>`
        - **ConstantWriteGenerator**: 
        `./multiConstantWriteGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <delay> <writesPerClient> <keysPerPartition> <writeClients>`
        - **BusyWriteGenerator**: 
        `./multiBusyWriteGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <keysPerPartition> <writeClients>`
        - **ConstantReadGenerator**: 
        `./multiConstantReadGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <readDelay> <readTime> <keysPerRead> <keyPerPartition> <readClients>`
> To replicate the experiments that generated our results, please refer to the `README` of the `evaluation` directory available [here](https://github.com/dianaamfr/Dissertation-Work/tree/evaluation/README.md).

### Eventually Consistent Baseline
<!-- TODO -->

## Candidate Reference Architecture
<!-- TODO: update according to the thesis document and update image too -->

![Candidate Reference Architecture](images/reference-architecture.png)

**Clock**: 
Hybrid Logical Clock

**Assumptions**: 
- The Eventually consistent Data Store (ECDS) allows writing to a specific region/partition.
- The ECDS allows reading by region, partition and timestamp.
- A *Read Node* in region R must only respond to read requests for partitions stored in region R.
- A *Write Node P* must only perform write requests in partition P. 
- Partitions are disjoint.
- Clients are sticky to a *Read Node* of their region.

**Overview**:
- The Client Layer forwards writes and ROTs to the Compute Layer through a client library. Writes are forwarded to the *Write Node* of the partition that is responsible for the data item that is written. ROTs are forwarded to the *Read Node* of the nearest region.
- The client stores his writes in cache until he knows that they are stable. 
- There is a *Write Node* per partition that orders the writes to that partition, updates the partition’s log and persists it to the ECDS.
- Replication is handled by the ECDS, so the *Write Node* just needs to write each log in one region and the ECDS will replicate it to the others.
- *Read Nodes* read asynchronously and periodically from the ECDS. They must keep track of the *stableTime* (a time below which the compute node knows all writes).
- *Read Nodes* must determine the *stableTime* from which ROTs can be performed. The client must store the timestamp of his last write (*lastWriteTimestamp*) so that it ensures monotonic writes. That timestamp must be sent in the next write request so that the server may update his clock and ensure that the second write is time stamped with a higher version than the first.
- Given that there are multiple writers, clocks must be synchronized to ensure writes become visible.
- When a *Read Node* receives a ROT request, it reads from the *stableTime* and sends back the *stableTime* together with the requested values so that the client may prune his cache and determine which values to return. After pruning the cache, if the client has a version in his cache of one of the requested items, he must return the value in his cache to ensure read-your-writes.
