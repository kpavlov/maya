package com.github.kpavlov.maya

import com.github.kpavlov.maya.sqs.LocalSqs
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqsTest {

    private val sqs = LocalSqs()

    @BeforeAll
    fun beforeAll() {
        sqs.start()
    }

    @AfterAll
    fun afterAll() {
        sqs.stop()
    }

    @Test
    fun `should run SQS`() {
        println("Running SQS. Listening to ${sqs.endpointUrl()}")
    }
}
