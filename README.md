# Dissertation Work

## Reference architecture with S3 emulation using localstack

**Requirements**
*(TODO: setup up docker-compose)*
- [Localstack](https://docs.localstack.cloud/getting-started/installation/)
- OpenJDK
- Maven

**Run**
1. Open a terminal and start localstack: `localstack start` 
2. Open a new terminal in the root folder
3. `cd referencearchitecture`
4. Create buckets: `make createBuckets`. This command creates buckets in:
    - us-east-1:
        - bucket "partition1": keys "x" and "y"
        - bucket "partition2": key "z"
    - use-west-1:
        - bucket "partition3": key "p"
5. `make`
6. Start the Read Compute Nodes, one on each terminal:
    - `make readNodeWest`
    - `make readNodeEast`
7. Start the Write Compute Nodes, one on each terminal:
    - `make writeNode1` (partition1)
    - `make writeNode2` (partition2)
    - `make writeNode3` (partition3)
8. Start the desired number of Clients:
    - `make clientWest` to access buckets in "us-east-1"
    - `make clientEast` to access buckets in "us-west-1"
9. Issue the desired ROT and write requests:
    - ROT example: `R x y` (keys must be available in the region)
    - Write example: `W x 3` (the value must be an integer)