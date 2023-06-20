# Value Semantics Experiment
In order to verify how the system enables value semantics, this experiment allows users to retrieve the value of a set of keys at a specific point in time or the history of a key's values over time. Versions can be indexed by their timestamp or by wall-clock time (passing a date/time as input to the program).

## Configuration
To generate the current logs we used the following configuration:
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

**Write Node 1**: `./writeNode.sh final 2 8080 1 8080 <readIp>`  
**Write Node 2**: `./writeNode.sh final 2 8080 2 8080 <readIp>`  
**Write Client 1**: `./constantWriteGenerator.sh final 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 3000 100 a b c d`  
**Write Client 2**: `./constantWriteGenerator.sh final 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 3000 100 a b c d`  

## Get logs
Use S3's interface or CLI to extract the logs to the appropriate subdirectory of the [logs directory](./logs/) (e.g. if the logs correspond to partition 1, store them in `logs/p1`).

## Run value semantics experiment
Run `python3 .` inside the current `value_semantics` directory specifying the necessary arguments to perform one of these operations:
- Get info about the time range of the logs with the `-i` flag
    > e.g. `python3 . -i`

- Retrieve the value of a set of keys by date, specifying the `-k` (keys), `-d` (date) and `-v`(get version) flags 
    > e.g. `python3 . -v -k a,b -d "2023-06-18 13:25:00.0"`

- Retrieve the history of a key up until a given date, specifying the `-k` (a single key), `-d` (date) and `-hist`(get history) flags 
    > e.g. `python3 . -hist -k a -d "2023-06-18 13:25:00.0"`

- Retrieve the value of a set of keys by timestamp, specifying the `-k` (keys), `-t` (timestamp) and `-v`(get version) flags 
    > e.g. `python3 . -v -k a,b -t 00000001687091029107-00000000000000000000`

- Retrieve the history of a key up until a given timestamp, specifying the `-k` (a single key), `-t` (timestamp) and `-hist`(get history) flags 
    > e.g. `python3 . -hist -k a -t 00000001687091029107-00000000000000000000`

> **Note**: Use the optional `-c` flag if the logs were obtained with checkpointing enabled.
