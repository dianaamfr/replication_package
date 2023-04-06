# Dissertation Work

A prototype implementation of a cloud-native causally consistent system. 

## Description

This repository holds a prototype implementation of a candidate reference architecture for a cloud-native causally consistent read-heavy system that is being designed to prove the dissertation's hypothesis. With this prototype, we aim to study the feasibility of the candidate reference architecture and identify any impediments and possible improvements at an early stage.

For a more detailed description of the reference architecture please refer to [Reference Architecture](#candidate-reference-architecture).


## Prototype Features
### Current Features
- **ECDS**: LocalStack is being used to emulate AWS S3 (without any replication configuration).
- **Compute Layer**: Provides ROTs and Writes to the Client through gRPC and uses AWS S3 for persistance.
- **Client Layer**: Connects with the Compute Layer through gRPC.
- **Clock**: Hybrid Logical Clock.
- **Consistency**: Stable time computation, read-you-writes for multiple writers through client cache and last write timestamp for monotonic writes.
- **Clock Synchronization**: Each *Write Compute Node* asynchronously persists his clock value in an S3 bucket and fetches the last clock value that has been stored. If the fetched clock value is higher than its own, it advances its clock.

## Getting Started

### Structure 
This repository holds a Maven project with the following structure:
- `src/main/java` comprised the application sources, namely:
    - `referencearchitecture`: Comprises the classes that implement the candidate reference architecture.
        - `client`: Contains the `Client` class, which can be used to issue ROTs and write operations. It connects with the `ReadNode` of its region and with the `WriteNodes` of its region's partitions through gRPC. It keeps a "cache" with its unstable writes and his last write timestamp.
        - `compute`: Contains the `ReadNode` and `WriteNode`, respectively responsible for handling ROTs of a region and writes of a partition. Also contains the `storage` package, which comprises the classes used to store the log in-memory and to pull and push the log to the data store. Furthermore, it stores the classes related to the implementation of the Hybrid Logical Clock in the `clock` package.
        - `exceptions`: Custom exceptions used throughout the source code.
        - `s3`: Provide the necessary functions to perform put and get operations in AWS S3.
    - `utils`: Util functions, constants and classes.
    - `validation`: Comprises classes that can be used to test the prototype, namely:
        - `ClientInterface`: To test the prototype through a command-line interface.
        - `WriteGenerator`: *(To be refactored)* To generate random write load. The delay between client writes, number of clients per partition, number of writes per partition and number of bytes per object can be customized. 
        - `ReadGenerator`: *(To be refactored)* To generate random read load. The delay between client ROTs, number of clients per region, number of ROTs per region and number of keys per ROT can be customized.
    - `proto`: Holds the `.proto` file that defines the services provided by read and write nodes.

### Dependencies
- OpenJDK
- Maven
- [LocalStack CLI](https://docs.localstack.cloud/getting-started/installation/) can be used to test the project locally.

### Execution Instructions
**User-friendly CLI**:
1. Open a terminal and start LocalStack: `localstack start` 
2. Open a new terminal in the root folder
3. Create buckets: `make createBuckets`. This command creates the following buckets:
    - bucket `reference-architecture-partition1`;
    - bucket `reference-architecture-partition2`;
    - bucket `reference-architecture-clock`, which is used to persist the clock values.
4. `make`
5. Start a Read Compute Node:
    - `make readNode`
6. Start the Write Compute Nodes, one on each terminal:
    - `make writeNode1` (partition1)
    - `make writeNode2` (partition2)
7. To test using the command-line interface:
    1. Start the desired number of Clients with the following command:
        - `make client`
    2. Issue the desired ROT and write requests:
        - ROT example: `R x y`
        - Write example: `W x 3`

**Load Generators**:
1. Open a terminal and start LocalStack: `localstack start` 
2. Open a new terminal in the root folder
3. Create buckets: `make createBuckets`. This command creates the following buckets:
    - bucket `reference-architecture-partition1`;
    - bucket `reference-architecture-partition2`;
    - bucket `reference-architecture-clock`, which is used to persist the clock values.
4. `make`
5. Start the Write Generator: `make writeGenerator`
6. Start the Read Generator: `make readGenerator`

## Candidate Reference Architecture

![Candidate Reference Architecture](images/reference-architecture.png)

**Clock**: 
Logical clock or Hybrid Logical Clock

**Assumptions**: 
- The Eventually consistent Data Store (ECDS) allows writing to a specific region/partition.
- The ECDS allows reading by region, partition and timestamp.
- A *Read Compute Node* in region R must only respond to read requests for partitions stored in region R.
- A *Write Compute Node P* must only perform write requests in partition P. 
- Partitions are disjoint.
- Clients are sticky to a *Read Compute Node* of their region.

**Overview**:
- The Client Layer forwards writes and ROTs to the Compute Layer through a client library. Writes are forwarded to the *Write Compute Node* of the partition that is responsible for the data item that is written. ROTs are forwarded to the *Read Compute Node* of the nearest region.
- The client stores his writes in cache until he knows that they are stable. 
- There is a *Write Compute Node* per partition that orders the writes to that partition, updates the partition’s log and persists it to the ECDS.
- Replication is handled by the ECDS, so the *Write Compute Node* just needs to write each log in one region and the ECDS will replicate it to the others.
- *Read Compute Nodes* read asynchronously and periodically from the ECDS. They must keep track of the *stableTime* (a time below which the compute node knows all writes).
- *Read Compute Nodes* must determine the *stableTime* from which ROTs can be performed. The client must store the timestamp of his last write (*lastWriteTimestamp*) so that it ensures monotonic writes. That timestamp must be sent in the next write request so that the server may update his clock and ensure that the second write is time stamped with a higher version than the first.
- Given that there are multiple writers, clocks must be synchronized to ensure writes become visible.
- When a *Read Compute Node* receives a ROT request, it reads from the *stableTime* and sends back the *stableTime* together with the requested values so that the client may prune his cache and determine which values to return. After pruning the cache, if the client has a version in his cache of one of the requested items, he must return the value in his cache to ensure read-your-writes.

**Clock Synchronization**

![Clock Synchronization](images/clock-sync.png)
