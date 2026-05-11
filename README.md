# risingwavepoc

## Build

Uses the [Jib Gradle plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin) to build a container image without a Dockerfile, directly into minikube's Docker daemon.

See [README/tips-k8s.md](README/tips-k8s.md) for build and deploy commands.

## SQL Access

See [README/tips-sql.md](README/tips-sql.md) for connection commands and quick selects.

## Endpoints

All traffic is routed through the NGINX Gateway Fabric on port `30800`.

| URL | Service | Description |
|-----|---------|-------------|
| `http://alex-ubuntu:30800/rw-webui` | `risingwavepoc-rw-webui` | Angular UI |
| `http://alex-ubuntu:30800/rw-webui-api` | `risingwavepoc-rw-webui-api` | WebSocket API |
| `http://alex-ubuntu:30800/rw-loader` | `risingwavepoc-rw-loader` | Loader REST API |

The path prefix is stripped before forwarding to the backend. Services are also reachable directly via NodePorts `30088`, `30082`, `30080` on `192.168.49.2`.

## API

See [README/tips-api.md](README/tips-api.md) for endpoint examples.
