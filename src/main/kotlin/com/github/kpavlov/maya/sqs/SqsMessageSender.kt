package com.github.kpavlov.maya.sqs

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Function

public open class SqsMessageSender<T> constructor(
    private val sqsClient: SqsAsyncClient,
    private val queueName: String,
    private val messageEncoder: Function<T, String>,
    private val messageGroupIdExtractor: Function<T, String>? = null,
    private val messageDeduplicationIdExtractor: Function<T, String>? = null,
) {
    private val logger: Logger = LoggerFactory.getLogger(
        SqsMessageSender::class.java.simpleName + "[" + queueName + "]"
    )

    public open fun sendMessageAsync(
        message: T
    ): CompletionStage<String> = sendMessageAsync(
        message = message,
        messageGroupIdExtractor = messageGroupIdExtractor,
        messageDeduplicationIdExtractor = messageDeduplicationIdExtractor
    )

    public open fun sendMessageAsync(
        message: T,
        messageGroupIdExtractor: Function<T, String>? = null,
        messageDeduplicationIdExtractor: Function<T, String>? = null,
    ): CompletionStage<String> {
        val messageBody = messageEncoder.apply(message)
        return sqsClient.getQueueUrl { request: GetQueueUrlRequest.Builder ->
            request.queueName(
                queueName
            )
        }
            .thenApply { getQueueUrlResponse: GetQueueUrlResponse ->
                val requestBuilder = SendMessageRequest.builder()
                    .queueUrl(getQueueUrlResponse.queueUrl())
                    .messageBody(messageBody)

                messageGroupIdExtractor?.let {
                    requestBuilder.messageGroupId(it.apply(message))
                }

                messageDeduplicationIdExtractor?.let {
                    requestBuilder.messageDeduplicationId(it.apply(message))
                }

                requestBuilder.build()
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
            }
    }
}
