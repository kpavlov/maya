package com.github.kpavlov.maya

import com.github.kpavlov.maya.sqs.LocalSqs
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI

object TestEnvironment {
    private val sqs = LocalSqs(configPath = "sqs-queues.conf")
    val sqsClient: SqsAsyncClient

    init {
        sqs.start()
        println("Running SQS. Listening to ${sqs.endpointUrl()}")

        sqsClient = SqsAsyncClient.builder()
            .endpointOverride(URI.create(sqs.endpointUrl()))
            .region(Region.US_EAST_1)
            .build()
    }
}
