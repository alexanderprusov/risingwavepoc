# # Kubernetes Tips

# ## Build

# Set Docker env to minikube's daemon, then build with Jib:
export DOCKER_TLS_VERIFY="1"
export DOCKER_HOST="tcp://192.168.49.2:2376"
export DOCKER_CERT_PATH="$HOME/.minikube/certs"

./gradlew :app:jibDockerBuild

# `dockerClient.executable` in `app/build.gradle` points to `/home/alex/data/software/docker/docker` (not on PATH).

# ## Deploy

kubectl create namespace risingwavepoc
helm install risingwave risingwavelabs/risingwave -n risingwavepoc -f helm/risingwave-values.yaml
helm install risingwavepoc-app helm/app -n risingwavepoc

# ## Redeploy App

./gradlew :app:jibDockerBuild
kubectl rollout restart deployment/risingwavepoc-app -n risingwavepoc

# ## Verify

kubectl get pods -n risingwavepoc
kubectl logs -n risingwavepoc deployment/risingwavepoc-app

# ## Disk Usage

# RisingWave state is stored in MinIO (Hummock). Check usage:
kubectl exec -n risingwavepoc deployment/risingwave-minio -- du -sh /bitnami/minio/data
