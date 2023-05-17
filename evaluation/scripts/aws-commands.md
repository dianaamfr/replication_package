# BUILD:
**Latency**: docker buildx build --platform linux/amd64,linux/arm64 -t dianaamfreitas/dissertation:v13.0.0-latency --build-arg visibility_logs=false --build-arg goodput_logs=false --push . 
**Goodput**: docker buildx build --platform linux/amd64,linux/arm64 -t dianaamfreitas/dissertation:v13.0.0-goodput --build-arg visibility_logs=false --build-arg goodput_logs=true --push .
**Visibility**: docker buildx build --platform linux/amd64,linux/arm64 -t dianaamfreitas/dissertation:v13.0.0-visibility --build-arg visibility_logs=true --build-arg goodput_logs=false --push .

**Eventual**:
docker buildx build --platform linux/amd64,linux/arm64 -t dianaamfreitas/dissertation-eventual:v2.0.0 --push .

# Docker clean
**On every machine**
docker rm -f $(docker ps -a -q)
docker rmi $(docker images -a -q)