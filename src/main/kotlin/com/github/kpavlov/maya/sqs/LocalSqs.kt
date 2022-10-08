package com.github.kpavlov.maya.sqs

import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/**
 * https://github.com/softwaremill/elasticmq
 * https://www.testcontainers.org/features/creating_container/
 */
public class LocalSqs(
    configPath: String = "sqs-queues.conf"
) {

    private val logger = LoggerFactory.getLogger(LocalSqs::class.java)

    @Suppress("SpellCheckingInspection")
    private val container = GenericContainer(
        DockerImageName.parse("softwaremill/elasticmq-native")
    )
        .withExposedPorts(9324)
        .waitingFor(Wait.forLogMessage(".*Queues successfully registered.*\\n", 1))
        .withLogConsumer(Slf4jLogConsumer(logger))
        .withFileSystemBind(
            Thread.currentThread().contextClassLoader.getResource(configPath)?.path,
            "/opt/elasticmq.conf",
            BindMode.READ_ONLY
        )

    public fun start() {
        logger.info("Starting SQS Server...")
        container.start()
        logger.info("SQS Server started.")
    }

    @Suppress("HttpUrlsUsage")
    public fun endpointUrl(): String {
        return "http://${container.host}:${container.getMappedPort(9324)}"
    }

    public fun stop() {
        logger.info("Stopping SQS Server...")
        container.stop()
        logger.info("SQS Server stopped.")
    }
}
