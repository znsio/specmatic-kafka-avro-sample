package com.example.order

import io.specmatic.async.core.constants.AVAILABLE_SERVERS
import io.specmatic.async.core.constants.EXAMPLES_DIR
import io.specmatic.async.core.constants.SCHEMA_REGISTRY_KIND
import io.specmatic.async.core.constants.SCHEMA_REGISTRY_URL
import io.specmatic.async.core.constants.SchemaRegistryKind
import io.specmatic.kafka.test.SpecmaticKafkaContractTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.ComposeContainer
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
        System.setProperty(SCHEMA_REGISTRY_URL, schemaRegistryUrl)
        System.setProperty(SCHEMA_REGISTRY_KIND, SchemaRegistryKind.CONFLUENT.name)
        System.setProperty(AVAILABLE_SERVERS, "localhost:9092")
    }

    @AfterAll
    fun tearDown() {
        schemaRegistry.stop()
    }

    private fun schemaRegistry(): ComposeContainer {
        return ComposeContainer(
            File("docker-compose.yaml")
        ).withLocalCompose(true).waitingFor(
            "register-schemas",
            LogMessageWaitStrategy()
                .withRegEx(".*(?i)Schemas registered.*")
                .withStartupTimeout(Duration.ofSeconds(60))
        )
    }

}