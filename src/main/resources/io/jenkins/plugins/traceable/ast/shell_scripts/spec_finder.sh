# Search for OpenAPI 3.x.x specifications
spec_files=$(find "${1}" -type f \( -name "*.yaml" -o -name "*.yml" -o -name "*.json" \) -print0 | xargs -0 grep -l -E '^\s*("openapi"|openapi)?\s*:\s*("3\.[0-9]+\.[0-9]+"|3\.[0-9]+\.[0-9]+)\s*')

spec_files="$spec_files"$'\n'
# Search for Swagger 2.0 specifications
spec_files+=$(find "${1}" -type f \( -name "*.yaml" -o -name "*.yml" -o -name "*.json" \) -print0 | xargs -0 grep -l -E '^\s*("swagger"|swagger)?\s*:\s*("2\.0"|2\.0)\s*')

echo "$spec_files"