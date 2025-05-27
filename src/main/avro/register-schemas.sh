#!/bin/sh
set -e

SCHEMA_REGISTRY_URL="http://schema-registry:8085"

# Wait for Schema Registry to be up
echo "Waiting for Schema Registry to be ready..."
until curl -s ${SCHEMA_REGISTRY_URL}/subjects > /dev/null; do
  sleep 2
done
echo "Schema Registry is up."

cd /usr/src/app/schemas

# Register CancelOrder
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < CancelOrder.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/cancel-order-value/versions

# Register PlaceOrder
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < PlaceOrder.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/place-order-value/versions

# Register ProcessCancellation
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < ProcessCancellation.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/process-cancellation-value/versions

# Register ProcessOrder
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < ProcessOrder.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/process-order-value/versions

echo "Schemas registered."