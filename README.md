# risingwavepoc

## Local Java App on Minikube

### Build

Uses the [Jib Gradle plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin) to build a container image without a Dockerfile, directly into minikube's Docker daemon:

```bash
export DOCKER_TLS_VERIFY="1"
export DOCKER_HOST="tcp://192.168.49.2:2376"
export DOCKER_CERT_PATH="$HOME/.minikube/certs"

./gradlew :app:jibDockerBuild
```

The `dockerClient.executable` in `app/build.gradle` is set to `/home/alex/data/software/docker/docker` since the docker binary is not on the default PATH.

### Deploy

```bash
# Create namespace
kubectl create namespace risingwavepoc

# Run pod (imagePullPolicy=Never uses the locally built image)
kubectl run risingwavepoc-app --image=risingwavepoc/app:latest --image-pull-policy=Never --restart=Never --namespace=risingwavepoc
```

### Verify

```bash
kubectl get pod risingwavepoc-app -n risingwavepoc
kubectl logs risingwavepoc-app -n risingwavepoc
# Hello World!
```

## SQL Access

Connect via the bundled psql-client pod (env vars pre-configured):

```bash
kubectl exec -it -n risingwavepoc deployment/psql-client -- psql
```

Or port-forward and connect locally:

```bash
kubectl port-forward -n risingwavepoc svc/risingwave 4567:4567
psql -h localhost -p 4567 -U root -d dev
```

### Quick Selects

```sql
-- row counts
SELECT
  (SELECT COUNT(*) FROM entity_alpha)   AS alphas,
  (SELECT COUNT(*) FROM entity_beta)    AS betas,
  (SELECT COUNT(*) FROM alpha_beta_ref) AS refs,
  (SELECT COUNT(*) FROM events)         AS events;

-- latest alphas
SELECT * FROM entity_alpha ORDER BY alpha_created_at DESC LIMIT 10;

-- latest betas
SELECT * FROM entity_beta ORDER BY beta_created_at DESC LIMIT 10;

-- joined alpha-beta via refs
SELECT
  a.id AS alpha_id, a.alpha_name,
  b.id AS beta_id,  b.beta_title,
  r.created_at AS linked_at
FROM entity_alpha a
JOIN alpha_beta_ref r ON r.alpha_id = a.id
JOIN entity_beta   b ON b.id = r.beta_id
ORDER BY a.id, b.id
LIMIT 20;

-- refs per alpha
SELECT a.alpha_name, COUNT(*) AS beta_count
FROM entity_alpha a
JOIN alpha_beta_ref r ON r.alpha_id = a.id
GROUP BY a.alpha_name
ORDER BY beta_count DESC
LIMIT 10;

-- latest events
SELECT * FROM events ORDER BY created_at DESC LIMIT 10;
```

### Disk Usage

RisingWave uses MinIO (Hummock) as the actual state store, so pg_catalog size functions may not be available. Check both:

`pg_size_pretty` and related functions are not supported in RisingWave. Real disk usage is in MinIO:

```bash
kubectl exec -n risingwavepoc deployment/risingwave-minio -- du -sh /bitnami/minio/data
```

## API

Base URL (fixed NodePort): `http://<minikube-ip>:30080`

```bash
# generate 100 rows in all tables
curl -X POST "http://192.168.49.2:30080/api/generate?count=100"

# search joined alpha-beta (both params optional, substring match)
curl "http://192.168.49.2:30080/api/alpha-beta/search?alphaName=alpha&betaTitle=mike"

# Swagger UI
open http://192.168.49.2:30080/swagger-ui.html
```







### SQL

select count(1) from (
  SELECT                                                                                                                                      *                                                                                                                                           
  FROM entity_alpha a                                       
  JOIN alpha_beta_ref r ON r.alpha_id = a.id
  JOIN entity_beta   b ON b.id = r.beta_id                                                                                                                                                   
  WHERE 1=1
    AND a.alpha_name LIKE '%a%'                                                                                      
    --AND b.beta_title LIKE '%<betaTitle>%'                                                                                      
  ORDER BY a.id, b.id
  );

