# Goodput Tests - Eventually Consistent S3

## Test 1
- Read Delay = 50ms
- Read for 20s
- 1 key per read
- 3 iterations

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v3.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v3.0.0 1 50 20000 a

---
## Test 2
- Write Delay = 100ms
- Read for 20s
- 1 key per read
- 3 iterations

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v3.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v3.0.0 1 100 20000 a

---
## Test 3
- Write Delay = 200ms
- Read for 20s
- 1 key per read
- 3 iterations

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v3.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v3.0.0 1 200 20000 a
