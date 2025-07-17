package com.example.order

import order.CancelOrderRequest
import order.CancellationReference
import order.CancellationStatus
import order.OrderRequest
import order.OrderStatus
import order.OrderToProcess
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val wipOrdersKafkaTemplate: KafkaTemplate<String, OrderToProcess>,
    private val ordersToCancelKafkaTemplate: KafkaTemplate<String, CancellationReference>
) {
    companion object {
        private const val NEW_ORDERS_TOPIC = "new-orders"
        private const val WIP_ORDERS_TOPIC = "wip-orders"
        private const val ORDERS_TO_CANCEL_TOPIC = "orders-to-cancel"
        private const val CANCELLED_ORDERS_TOPIC = "cancelled-orders"
    }

    private val serviceName = this::class.simpleName

    init {
        println("$serviceName started running..")
    }

    @KafkaListener(topics = [NEW_ORDERS_TOPIC])
    fun placeOrder(record: ConsumerRecord<String, OrderRequest>) {
        val orderRequest = record.value()
        println("[$serviceName] Received message on topic $NEW_ORDERS_TOPIC - $orderRequest")

        val orderToProcess = OrderToProcess.newBuilder()
            .setId(orderRequest.id)
            .setStatus(OrderStatus.PROCESSING)
            .build()
        wipOrdersKafkaTemplate.send(WIP_ORDERS_TOPIC, orderToProcess)
        println("[$serviceName] Sent message to topic $WIP_ORDERS_TOPIC - $orderToProcess")
    }

    @KafkaListener(topics = [ORDERS_TO_CANCEL_TOPIC])
    fun cancelOrder(record: ConsumerRecord<String, CancelOrderRequest>) {
        val cancelOrderRequest = record.value()

        val cancellationReference = CancellationReference.newBuilder()
            .setReference(cancelOrderRequest.id)
            .setStatus(CancellationStatus.COMPLETED)
            .build()

        ordersToCancelKafkaTemplate.send(CANCELLED_ORDERS_TOPIC, cancellationReference)
        println("[$serviceName] Sent message to topic $CANCELLED_ORDERS_TOPIC - $cancellationReference")
    }
}