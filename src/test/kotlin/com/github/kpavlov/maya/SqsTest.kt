package com.github.kpavlov.maya

import com.github.kpavlov.maya.sqs.LocalSqs
import com.github.kpavlov.maya.sqs.SqsMessageSender
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI
import java.util.function.Function.identity

private const val QUEUE_NAME = "test-queue"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqsTest {

    private val sqs = LocalSqs(configPath = "sqs-queues.conf")
    private lateinit var sqsClient: SqsAsyncClient
    private lateinit var sender: SqsMessageSender<String>

    @BeforeAll
    fun beforeAll() {
        sqs.start()

        sqsClient = SqsAsyncClient.builder()
            .endpointOverride(URI.create(sqs.endpointUrl()))
            .region(Region.US_EAST_1)
            .build()

        sender = SqsMessageSender<String>(sqsClient, QUEUE_NAME, identity())
    }

    @AfterAll
    fun afterAll() {
        sqs.stop()
    }

    @Test
    fun `should run SQS`() = runBlocking {
        println("Running SQS. Listening to ${sqs.endpointUrl()}")
        val messageId = sender.sendMessage("Hello, World!")
        assertThat(messageId).isNotBlank
    }
}
