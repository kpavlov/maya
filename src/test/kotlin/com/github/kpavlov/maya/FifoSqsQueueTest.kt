package com.github.kpavlov.maya

import com.github.kpavlov.maya.TestEnvironment.sqsClient
import com.github.kpavlov.maya.sqs.SqsMessageSender
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.function.Function

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FifoSqsQueueTest {

    private lateinit var sender: SqsMessageSender<String>

    private val messageGroupIdExtractor: (t: String) -> String = { it.substring(0, 5) }
    private val messageDeduplicationIdExtractor: (t: String) -> String = { it.hashCode().toString() }

    @BeforeAll
    fun beforeAll() {
        sender = SqsMessageSender<String>(
            sqsClient = sqsClient,
            queueName = "test-queue.fifo",
            messageEncoder = Function.identity(),
            messageGroupIdExtractor = messageGroupIdExtractor,
            messageDeduplicationIdExtractor = messageDeduplicationIdExtractor
        )
    }

    @Test
    fun `should send one message`(): Unit = runBlocking {
        val messageId = sender.sendMessageAsync(
            message = "Hello, World!",
            messageGroupIdExtractor = messageGroupIdExtractor,
            messageDeduplicationIdExtractor = messageDeduplicationIdExtractor
        ).await()
        Assertions.assertThat(messageId).isNotBlank
    }

    @Test
    fun `should deduplicate message`(): Unit = runBlocking {
        val message = "Hello, World!"
        val messageId1 = sender.sendMessageAsync(
            message = message
        ).await()
        val messageId2 = sender.sendMessageAsync(
            message = message,
            messageGroupIdExtractor = messageGroupIdExtractor,
            messageDeduplicationIdExtractor = messageDeduplicationIdExtractor
        ).await()
        Assertions.assertThat(messageId2).isEqualTo(messageId1)
    }
}
