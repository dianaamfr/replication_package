## Reference Architecture

## Tasks
- Make write node perist periodically in the abscence of updates to ensure progress
- Client cache
- Support multiple writers (send lastWriteTimestamp)
- ReadNode send stableTime back for client to prune cache and decide which version to show

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.
