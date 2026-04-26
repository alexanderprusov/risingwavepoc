# # API Tips

# Base URL (fixed NodePort): `http://192.168.49.2:30080`

# Swagger UI: `http://192.168.49.2:30080/swagger-ui.html`

# ## Generate Data

# # generate 100 rows in entity_alpha, entity_beta, alpha_beta_ref
curl -X POST "http://192.168.49.2:30080/api/generate?count=100"

# # generate 100 events
curl -X POST "http://192.168.49.2:30080/api/generate/events?count=100"

# ## Search

# # search joined alpha-beta (both params optional, substring match)
curl "http://192.168.49.2:30080/api/alpha-beta/search?alphaName=alpha&betaTitle=mike"

# # same query, returns elapsed_ms and row_count only
curl "http://192.168.49.2:30080/api/alpha-beta/search-timed?alphaName=alpha&betaTitle=mike"
