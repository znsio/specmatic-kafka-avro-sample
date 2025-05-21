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
    private val processOrderKafkaTemplate: KafkaTemplate<String, OrderToProcess>,
    private val cancelOrderKafkaTemplate: KafkaTemplate<String, CancellationReference>
) {
    companion object {
        private const val PLACE_ORDER_TOPIC = "place-order"
        private const val PROCESS_ORDER_TOPIC = "process-order"
        private const val CANCEL_ORDER_TOPIC = "cancel-order"
        private const val PROCESS_CANCELLATION_TOPIC = "process-cancellation"
    }

    private val serviceName = this::class.simpleName

    init {
        println("$serviceName started running..")
    }

    @KafkaListener(topics = [PLACE_ORDER_TOPIC])
    fun placeOrder(record: ConsumerRecord<String, OrderRequest>) {
        val orderRequest = record.value()
        println("[$serviceName] Received message on topic $PLACE_ORDER_TOPIC - $orderRequest")

        val orderToProcess = OrderToProcess.newBuilder()
            .setId(orderRequest.id)
            .setStatus(OrderStatus.PROCESSING)
            .build()
        processOrderKafkaTemplate.send(PROCESS_ORDER_TOPIC, orderToProcess)
        println("[$serviceName] Sent message to topic $PROCESS_ORDER_TOPIC - $orderToProcess")
    }

    @KafkaListener(topics = [CANCEL_ORDER_TOPIC])
    fun cancelOrder(record: ConsumerRecord<String, CancelOrderRequest>) {
        val cancelOrderRequest = record.value()

        val cancellationReference = CancellationReference.newBuilder()
            .setReference(cancelOrderRequest.id)
            .setStatus(CancellationStatus.COMPLETED)
            .build()

        cancelOrderKafkaTemplate.send(PROCESS_CANCELLATION_TOPIC, cancellationReference)
        println("[$serviceName] Sent message to topic $PROCESS_CANCELLATION_TOPIC - $cancellationReference")
    }
}