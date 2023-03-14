## Reference Architecture

## Tasks
- Server storage (map of key values)
- Write key in storage
- Request key from storage
- Have a map of remoteInterfaces identified by partition  for read interfaces and write interface
- Have a mapping between keys and partition
- Client cache

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.
