#!/bin/bash
set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
KAFBIN="$HOME/data/software/kafka/bin"

echo "==> Kafka namespace"
kubectl create namespace risingwavepoc-kafka
helm install kafka "$REPO_ROOT/helm/kafka" -n risingwavepoc-kafka --set nodeIP="$NODE_IP"
kubectl rollout status statefulset/kafka -n risingwavepoc-kafka --timeout=120s
until $KAFBIN/kafka-topics.sh --bootstrap-server "${NODE_IP}:30092" --list &>/dev/null; do sleep 3; done

echo "==> Creating Kafka topics"
for f in "$REPO_ROOT/kafka/topics/"*.yaml; do
  name=$(grep '^name:'               "$f" | awk '{print $2}')
  parts=$(grep '^partitions:'        "$f" | awk '{print $2}')
  rf=$(grep '^replication-factor:'   "$f" | awk '{print $2}')
  $KAFBIN/kafka-topics.sh --bootstrap-server "${NODE_IP}:30092" \
    --create --if-not-exists --topic "$name" --partitions "$parts" --replication-factor "$rf" \
    && echo "  $name"
done

echo "==> RisingWave namespace"
kubectl create namespace risingwavepoc
helm install risingwave risingwavelabs/risingwave -n risingwavepoc -f "$REPO_ROOT/helm/risingwave-values.yaml"
kubectl rollout status deployment/risingwave-frontend -n risingwavepoc --timeout=180s
kubectl rollout status statefulset/risingwave-compute -n risingwavepoc --timeout=180s

echo "==> Building and deploying app"
export DOCKER_TLS_VERIFY="1"
export DOCKER_HOST="tcp://${NODE_IP}:2376"
export DOCKER_CERT_PATH="$HOME/.minikube/certs"
"$REPO_ROOT/gradlew" -p "$REPO_ROOT" :app:jibDockerBuild
helm install risingwavepoc-app "$REPO_ROOT/helm/app" -n risingwavepoc
kubectl rollout status deployment/risingwavepoc-app -n risingwavepoc --timeout=120s

echo "==> Done"
