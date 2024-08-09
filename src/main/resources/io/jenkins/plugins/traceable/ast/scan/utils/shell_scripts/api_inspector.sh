#!/bin/bash

GRAPHQL_ENDPOINT="https://$1/graphql"
API_ENDPOINT="https://$1/rest/v2/upload/documentation/openapi"
PLATFORM_TOKEN="$2"
SPEC_PATH="$3"
FILE_NAME=$(basename "$SPEC_PATH")
GRAPHQL_QUERY='mutation {
  createApiSpec(
    input: {
      name: \"'$FILE_NAME'\"
      apiNamingEnabled: false
      apiDiscoveryEnabled: false
      apiInspectorDisabled: false
    }
  ) {
    id
  }
}'
echo "Creating API spec config with name $FILE_NAME"
RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: $PLATFORM_TOKEN" \
  -d "{\"query\": \"$GRAPHQL_QUERY\"}" \
  "$GRAPHQL_ENDPOINT")
if [ "$RESPONSE" = "unauthorized" ]; then
  echo "token $PLATFORM_TOKEN is invalid"
else
  # Extract the ID from the response
  ID=$(echo "$RESPONSE" | jq -r '.data.createApiSpec.id')
  echo "API spec config created with ID: $ID"

  # Step 2: Upload the spec file using the REST API
  echo "Uploading spec file $SPEC_PATH"
  UPLOAD_RESPONSE=$(curl -s -X POST "$API_ENDPOINT" \
  -H "Authorization: $PLATFORM_TOKEN" \
  -F "$ID=@$SPEC_PATH")
  echo "Uploaded Open Api Spec."
fi