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
  --data "{\"schema\":$(jq -Rs . < OrdersToCancel.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/orders-to-cancel-value/versions

# Register PlaceOrder
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < NewOrders.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/new-orders-value/versions

# Register ProcessCancellation
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < CancelledOrders.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/cancelled-orders-value/versions

# Register ProcessOrder
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data "{\"schema\":$(jq -Rs . < WipOrders.avsc)}" \
  ${SCHEMA_REGISTRY_URL}/subjects/wip-orders-value/versions

echo "Schemas registered."