# Visibility Tests - CC prototype
- Write Delay = 50ms
- Push/Pull Rate = 5ms
- 500 writes per client
- Read for 90s
- 12 keys per partition
- 2 keys per read
- <!-- TODO: specify client machine -->
- <!-- TODO: specify server machine -->

## Test
<!-- TODO: replace <R> an <W> placeholders with the number of read and write clients -->

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-WEST-1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 500 12 <W>
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 90000 2 12 <R>

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-EAST-1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 90000 2 12 <R>

<!-- TODO: spread write nodes across both regions and repeat one of the tests for 1, 2, 3, 4, 6 partitions  -->