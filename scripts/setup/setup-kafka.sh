#!/bin/bash
set -e

# Install Kafka binaries locally to ~/data/software/kafka/

VERSION="3.9.2"
INSTALL_DIR="$HOME/data/software/kafka"

mkdir -p "$INSTALL_DIR"
curl -Lo /tmp/kafka.tgz "https://dlcdn.apache.org/kafka/${VERSION}/kafka_2.13-${VERSION}.tgz"
tar -xzf /tmp/kafka.tgz -C /tmp
mv /tmp/kafka_2.13-${VERSION}/* "$INSTALL_DIR/"
rm -rf /tmp/kafka.tgz /tmp/kafka_2.13-${VERSION}

echo "Kafka binaries installed to $INSTALL_DIR/bin"

# Deploy Kafka into the risingwavepoc-kafka namespace using the dedicated helm chart.
# Uses apache/kafka image in KRaft mode (2 brokers, KRaft, no auth).

NS=risingwavepoc-kafka
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
kubectl get namespace $NS &>/dev/null || kubectl create namespace $NS
helm upgrade --install kafka helm/kafka -n $NS --set nodeIP=$NODE_IP

# Bootstrap server (in-cluster): kafka.risingwavepoc-kafka.svc.cluster.local:9092
# Bootstrap server (external):   <node-ip>:30092  (see KAFKA env var in ~/.bashrc.kube)
#
# Test with a temporary client pod:
# kubectl run kafka-client --restart=Never --image=apache/kafka:3.9.0 -n risingwavepoc-kafka --command -- sleep infinity
# kubectl exec -it kafka-client -n risingwavepoc-kafka -- /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka.risingwavepoc-kafka.svc.cluster.local:9092 --topic test
# kubectl exec -it kafka-client -n risingwavepoc-kafka -- /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka.risingwavepoc-kafka.svc.cluster.local:9092 --topic test --from-beginning
