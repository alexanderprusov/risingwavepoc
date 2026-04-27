# # Streaming Tips

# ## Entities Source

# Check how many messages RisingWave has consumed from the entities Kafka topic:
kubectl exec -n risingwavepoc deployment/psql-client -- psql -h risingwave -U root -d dev -c "SELECT COUNT(*) FROM entities;"

# Sample recent messages:
kubectl exec -n risingwavepoc deployment/psql-client -- psql -h risingwave -U root -d dev -c "SELECT * FROM entities LIMIT 10;"

# ## Kafka Topics

# Total message count per topic (sum across all partitions):
$KAFBIN/kafka-get-offsets.sh $KAFKA --topic entities | awk -F: '{s+=$3} END {print s}'
$KAFBIN/kafka-get-offsets.sh $KAFKA --topic alpha | awk -F: '{s+=$3} END {print s}'
$KAFBIN/kafka-get-offsets.sh $KAFKA --topic beta | awk -F: '{s+=$3} END {print s}'
$KAFBIN/kafka-get-offsets.sh $KAFKA --topic refs | awk -F: '{s+=$3} END {print s}'

# Summed totals for all topics:
for topic in entities alpha beta refs; do
  echo -n "$topic: "
  $KAFBIN/kafka-get-offsets.sh $KAFKA --topic $topic | awk -F: '{s+=$3} END {print s}'
done
