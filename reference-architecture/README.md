## Reference Architecture

## Tasks
- Simulate storage push in writeNode and pull in readNode
    - Change unsafe conversion (maybe convert to json)
    - Do pull part
- Client with operation parameters
- Server parameters (to support a server per partition)
- Change key name in data store to represent the partition
- Have a map of remoteInterfaces identified by partition  for read interfaces and write interface
- Have a mapping between keys and partition
- Client cache
- Think if it makes sense to separate read and write storage

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.
