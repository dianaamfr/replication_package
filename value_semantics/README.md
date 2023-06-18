# Value Semantics Experiment
In order to verify how the system enables value semantics, this experiment allows users to retrieve the value of a set of keys at a specific point in time or the history of a key's values over time. Versions can be indexed by their timestamp or by wall-clock time (passing a date/time as input to the program).

## Configuration
- 1 region: eu-west-1
- 1 read node and 2 partitions (2 write nodes)
- 2 clients issuing writes with 3s inter-write delay
- t4g.small/ubuntu/arm instances instances for each server node and for each client
- Push/Pull Rate of 5ms
- 4 keys (a,b,c,d)
- 2 keys per partition
- 100 writes per client

## AWS execution instructions
> Note: follow the execution instructions on the [main README](../README.md) to create and setup the instances and S3 buckets.

**Write Node 1**: ./writeNode.sh v15.0.0 2 8080 1 8080 <readIp>
**Write Node 2**: ./writeNode.sh v15.0.0 2 8080 2 8080 <readIp>
**Write Client 1**: ./constantWriteGenerator.sh v15.0.0 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 3000 100 a b c d
**Write Client 2**: ./constantWriteGenerator.sh v15.0.0 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 3000 100 a b c d

## Get logs
1. Go to each S3 bucket.
2. Extract the logs to the appropriate subdirectory of the [logs directory](./logs/).

## Run value semantics experiment
1. run `python3 .` inside the current `value_semantics` directory.
2. Use the CMD to:
    - **Option 1**: retrieve the value of a set of keys by date.
    - **Option 2**: retrieve the history of a key up until a given date.
    - **Option 3**: retrieve the value of a set of keys by timestamp.
    - **Option 4**: retrieve the history of a key up until a given timestamp.
