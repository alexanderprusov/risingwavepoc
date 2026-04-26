# risingwavepoc

## Build

Uses the [Jib Gradle plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin) to build a container image without a Dockerfile, directly into minikube's Docker daemon.

See [README/tips-k8s.md](README/tips-k8s.md) for build and deploy commands.

## SQL Access

See [README/tips-sql.md](README/tips-sql.md) for connection commands and quick selects.

## API

Base URL (fixed NodePort): `http://192.168.49.2:30080`

See [README/tips-api.md](README/tips-api.md) for endpoint examples.
