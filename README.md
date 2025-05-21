
// TODO - Update the commands so that those can be run without using Docker Desktop but CLI.
# Register place-order schema:

```shell
kafka-avro-console-producer \
--broker-list broker:9093 \
--topic place-order \
--property schema.registry.url=http://localhost:8085 \
--property value.schema.file=/usr/src/app/schemas/PlaceOrder.avsc
```

# Register cancel-order schema:

```shell
kafka-avro-console-producer \
--broker-list broker:9093 \
--topic cancel-order \
--property schema.registry.url=http://localhost:8085 \
--property value.schema.file=/usr/src/app/schemas/CancelOrder.avsc
```

# Read message from a topic

```shell
kafka-avro-console-consumer \
   --bootstrap-server broker:9093 \
   --topic process-cancellation \
   --from-beginning \
   --property schema.registry.url=http://localhost:8085
```