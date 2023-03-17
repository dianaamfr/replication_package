## Reference Architecture

## Tasks
- Change key name in data store to represent the partition
- Make write node write state only when it has something
- Make write node persist the log per partion
- Make write node perist periodically in the abscence of updates to ensure progress
- Client cache

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.
