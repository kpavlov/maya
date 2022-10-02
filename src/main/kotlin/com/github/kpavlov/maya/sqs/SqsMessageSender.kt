package com.github.kpavlov.maya.sqs

import kotlinx.coroutines.future.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class SqsMessageSender<T> internal constructor(
    private val sqsClient: SqsAsyncClient,
    private val queueName: String,
    private val encoder: Function<T, String>
) {
    private val logger: Logger = LoggerFactory.getLogger(
        SqsMessageSender::class.java.simpleName + "[" + queueName + "]"
    )

    suspend fun sendMessage(message: T): String {
        val messageBody = encoder.apply(message)
        return sqsClient.getQueueUrl { request: GetQueueUrlRequest.Builder ->
            request.queueName(
                queueName
            )
        }
            .thenApply { getQueueUrlResponse: GetQueueUrlResponse ->
                SendMessageRequest.builder()
                    .queueUrl(getQueueUrlResponse.queueUrl())
                    .messageBody(messageBody)
                    .build()
            }
            .thenCompose { sendMessageRequest ->
                sqsClient.sendMessage(sendMessageRequest)
            }
            .thenApply { sendMessageResponse ->
                val messageId = sendMessageResponse.messageId()
                logger.info("Message sent: messageId={}", messageId)
                messageId
            }.exceptionallyCompose { throwable: Throwable ->
                logger.error(
                    "Failed to publish a message {} to SQS queue \"{}\"", message,
                    queueName
                )
                CompletableFuture.failedStage(throwable)
            }.await()
    }
}
