#!/bin/bash
set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
KAFBIN="$HOME/data/software/kafka/bin"
GATEWAY_API_VERSION="v1.2.1"
GATEWAY_NODE_PORT=30800

echo "==> Gateway API CRDs"
kubectl apply -f "https://github.com/kubernetes-sigs/gateway-api/releases/download/${GATEWAY_API_VERSION}/standard-install.yaml"

echo "==> NGINX Gateway Fabric"
helm upgrade --install ngf oci://ghcr.io/nginxinc/charts/nginx-gateway-fabric \
  --namespace nginx-gateway --create-namespace \
  --set service.type=NodePort \
  --wait --timeout=120s

echo "==> Gateway resources"
kubectl get namespace risingwavepoc &>/dev/null || kubectl create namespace risingwavepoc
helm upgrade --install risingwavepoc-gateway "$REPO_ROOT/helm/gateway" -n risingwavepoc

echo "  Waiting for nginx-gateway service..."
until kubectl get svc ngf-nginx-gateway-fabric -n nginx-gateway &>/dev/null; do sleep 3; done
kubectl patch svc ngf-nginx-gateway-fabric -n nginx-gateway --type='json' \
  -p="[{\"op\":\"add\",\"path\":\"/spec/ports/0/nodePort\",\"value\":${GATEWAY_NODE_PORT}}]"
echo "  Gateway: http://${NODE_IP}:${GATEWAY_NODE_PORT}"

echo "==> Kafka namespace"
kubectl get namespace risingwavepoc-kafka &>/dev/null || kubectl create namespace risingwavepoc-kafka
helm upgrade --install kafka "$REPO_ROOT/helm/kafka" -n risingwavepoc-kafka --set nodeIP="$NODE_IP"
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
kubectl get namespace risingwavepoc &>/dev/null || kubectl create namespace risingwavepoc
helm upgrade --install risingwave risingwavelabs/risingwave -n risingwavepoc -f "$REPO_ROOT/helm/risingwave-values.yaml"
kubectl rollout status deployment/risingwave-frontend -n risingwavepoc --timeout=180s
kubectl rollout status statefulset/risingwave-compute -n risingwavepoc --timeout=180s

echo "==> Deploying rw-loader"
helm upgrade --install risingwavepoc-rw-loader "$REPO_ROOT/helm/rw-loader" -n risingwavepoc
kubectl rollout status deployment/risingwavepoc-rw-loader -n risingwavepoc --timeout=120s

echo "==> Deploying rw-webui-api"
helm upgrade --install risingwavepoc-rw-webui-api "$REPO_ROOT/helm/rw-webui-api" -n risingwavepoc
kubectl rollout status deployment/risingwavepoc-rw-webui-api -n risingwavepoc --timeout=120s

echo "==> Deploying rw-webui"
helm upgrade --install risingwavepoc-rw-webui "$REPO_ROOT/helm/rw-webui" -n risingwavepoc
kubectl rollout status deployment/risingwavepoc-rw-webui -n risingwavepoc --timeout=120s

echo "==> Done"
