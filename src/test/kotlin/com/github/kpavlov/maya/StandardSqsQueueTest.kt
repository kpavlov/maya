package com.github.kpavlov.maya

import com.github.kpavlov.maya.sqs.SqsMessageSender
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.function.Function.identity

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StandardSqsQueueTest {

    private lateinit var sender: SqsMessageSender<String>

    @BeforeAll
    fun beforeAll() {
        sender = SqsMessageSender<String>(TestEnvironment.sqsClient, "test-queue", identity())
    }

    @Test
    fun `should run SQS`(): Unit = runBlocking {
        val messageId = sender.sendMessageAsync("Hello, World!").await()
        assertThat(messageId).isNotBlank
    }
}
