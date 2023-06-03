# Dissertation Work

A prototype implementation of a cloud-native causally consistent system. 

## Description

This repository holds a prototype implementation of a candidate reference architecture for a cloud-native causally consistent read-heavy system. With this prototype, we aim to study the feasibility of a candidate reference architecture and identify any impediments and possible improvements at an early stage.

For a more detailed description of the reference architecture please refer to [Reference Architecture](#candidate-reference-architecture).

Furthermore, it holds an eventually consistent version of the prototype which served as baseline for comparison with the causally consistent prototype. Finally, it contains the necessary scripts to test the prototype in AWS, the scripts used for the evaluation and the plots and tables that resulted from our analysis.

## Prototype Features
### Features
- **Storage Layer**: Amazon S3 Simple Storage Service.
- **Compute Layer**: Read Nodes provide ROTs from the stable frontier whereas Write Nodes provide single-key writes and atomic writes.
- **Client Layer**: Forwards the operations to the appropriate Compute Nodes. Stores the client's writes until they are known to be stable.
- **Communication**: gRPC framework.
- **Clock**: Hybrid Logical Clock.
- **Consistency**: Provides Causal Consistency: ROTs are always performed from a stable time, read-you-writes is guaranteed for multiple writers by storing unstable writes in the client cache and monotonic writes are ensured by including the previous write timestamp of the client in subsequent requests.
- **Clock Synchronization**: Write Nodes asynchronously persists their last clock value in an S3 bucket. In the absence of updates, they fetch the last clock value that has been stored. If the fetched clock value is higher than its own, it advances its clock.
- **Check-pointing**: Periodically, write nodes replace a prefix of their log with a checkpoint. For that purpose, they get the minimum stable time from the Read Nodes that replicate their partition and only remove versions below the stable frontier established by that time (always keeping the latest stable version of each key).

## Getting Started

### Structure
**Causally Consistent Prototype**
A Maven project with the following structure:
- `src/main` comprises the project's source code and follows the structure below:
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
    - `proto`: Holds the `.proto` file that defines the services provided by read and write nodes.

**Eventually Consistent Baseline**
For comparison with our causally consistent prototype, which uses S3 as the storage layer, we developed an eventually consistent baseline where clients issue read and write requests directly to S3. It consists of a Maven project with the following structure:
- `eventual/src/main/java/com/dissertation`:
    - `eventual`:  Comprises the classes that implement the eventually consistent baseline
        - `client`: Contains the `Client` class, which can be used to issue read and write operations to S3.
        - `s3`: Provide the necessary functions to perform put and get operations in AWS S3.
        - 
    - `evaluation`: Comprises classes that can be used to evaluate the baseline:
        - `logs`: Classes that represent logs of each relevant operation. Used for validating the baseline.
        - `ClientInterface`: To test the prototype through a command-line interface.
        - `BusyReadGenerator`: A single-threaded reader that issues read requests with no delay to the keys provided in the arguments.
        - `ConstantWriteGenerator`: A single-threaded writer that issues a configurable number of write requests at a fixed rate, alternating between the provided set of keys.
        - `ConstantReadGenerator`: A single-threaded reader that issues read requests with a fixed delay to all the keys provided in the arguments for the given amount of time.
        - `BusyWriteGenerator`: A single-threaded writer that issues write requests with no delay, alternating between the provided set of keys.
    - `utils`: Util functions, constants and classes.

**Evaluation**
- `evaluation`:
    - `logs`: Stores the evaluation logs generated when running the evaluation scripts in AWS.
    - `results`: Stores the plots and other results of the analysis of the logs.
    - `aws-scripts`: Holds useful scripts for running each component of the prototype with docker and to generate the logs.
    - ` data_analysis`: A python package to analyze the logs generated during the evaluation and generate the necessary plots and tables.
    - `README.md`: Describes the tests performed during the evaluation and includes the necessary commands to replicate them.

### Dependencies
**To test locally**:
- [LocalStack CLI](https://docs.localstack.cloud/getting-started/installation/) and [AWS Command Line Interface](https://docs.localstack.cloud/user-guide/integrations/aws-cli/).
- OpenJDK
- Maven

**To test in AWS**:
- EC2 instances must have docker, expose port 8080 and have access to S3 buckets
- S3 buckets for each partition

### Execution Instructions
#### LocalStack (locally)
The project can be tested locally using LocalStack. To do so, follow the instructions below:

**Set up the buckets and the read and write nodes**:
1. Open a terminal and start LocalStack: `localstack start` 
2. Open a new terminal in the root folder
3. Create buckets: `make createBuckets`. This command creates the following buckets:
    - bucket `p1-us-east-1-reference-architecture`;
    - bucket `p2-us-east-1-reference-architecture`;
    - bucket `clock-reference-architecture`, which is used to persist the clock values.
4. `make`
5. Start the Read Nodes:
    - `make readNode` (reads from partition 1 and partition 2)
6. Start the Write Nodes, one on each terminal:
    - `make writeNode1` (writes in partition 1)
    - `make writeNode2` (writes in partition 2)

**Issue read and write requests**:
Using the command-line interface:
1. Start the desired number of clients: `make client`
2. Issue the desired ROT and write requests:
    - ROT example: `R x y`
    - Write example: `W x 3`

## Candidate Reference Architecture

![Candidate Reference Architecture](images/reference-architecture.png)

**Clock**: 
Logical clock or Hybrid Logical Clock

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
