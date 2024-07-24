# Search for OpenAPI 3.x.x specifications
spec_files=$(find "${1}" -type f \( -name "*.yaml" -o -name "*.yml" -o -name "*.json" \) -print0 | xargs -0 grep -l -e "openapi: 3" -e "\"openapi\": 3")

# Search for Swagger 2.0 specifications
spec_files+=$(find "${1}" -type f \( -name "*.yaml" -o -name "*.yml" -o -name "*.json" \) -print0 | xargs -0 grep -l -e "swagger: \"2.0\"" -e "\"swagger\": \"2.0\"" )
echo "$spec_files"
