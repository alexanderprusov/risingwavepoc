# # SQL Tips

# ## Connect

# Via psql-client pod (env vars pre-configured):
kubectl exec -it -n risingwavepoc deployment/psql-client -- psql

# Via port-forward:
kubectl port-forward -n risingwavepoc svc/risingwave 4567:4567
psql -h localhost -p 4567 -U root -d dev

# ## Quick Selects

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

-- count filtered join
SELECT count(1) FROM (
  SELECT *
  FROM entity_alpha a
  JOIN alpha_beta_ref r ON r.alpha_id = a.id
  JOIN entity_beta   b ON b.id = r.beta_id
  WHERE 1=1
    AND a.alpha_name LIKE '%a%'
    --AND b.beta_title LIKE '%<betaTitle>%'
  ORDER BY a.id, b.id
);

# ## Notes

# `pg_size_pretty` and related pg_catalog size functions are not supported in RisingWave.
# Real disk usage is in MinIO — see tips-k8s.md.
