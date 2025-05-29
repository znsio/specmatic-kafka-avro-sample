package com.example.order

import io.specmatic.kafka.SpecmaticKafkaContractTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import java.io.File
import java.time.Duration

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContractTest: SpecmaticKafkaContractTest {
    @Value("\${spring.kafka.properties.schema.registry.url}")
    lateinit var schemaRegistryUrl: String

    private val schemaRegistry = schemaRegistry()

    @BeforeAll
    fun setup() {
        schemaRegistry.start()
        System.setProperty("SCHEMA_REGISTRY_URL", schemaRegistryUrl)
        System.setProperty("CONSUMER_GROUP_ID", "order-consumer-group-id")
        System.setProperty("EXAMPLES_DIR", "src/test/resources")
    }

    @AfterAll
    fun tearDown() {
        schemaRegistry.stop()
    }

    private fun schemaRegistry(): DockerComposeContainer<*> {
        return DockerComposeContainer(
            File("src/test/resources/docker-compose.yaml")
        ).withLocalCompose(true).waitingFor(
            "register-schemas",
            LogMessageWaitStrategy()
                .withRegEx(".*(?i)Schemas registered.*")
                .withStartupTimeout(Duration.ofSeconds(60))
        )
    }

}