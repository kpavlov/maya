# Maya

[![Maven Central](https://img.shields.io/maven-central/v/com.github.kpavlov.maya/maya.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.kpavlov.maya%22%20AND%20a:%22maya%22)

_Maya_ (/ˈmɑːjə/, माया), literally "illusion" or "magic". 
In Vedic texts it connotes a "magic show, an illusion where things
appear to be present, but are not what they seem".

This project provides classes for easier integration testing, 
simulating external environment.

## features

* SQS emulator
  * Standard and FIFO queues support
  * [SqsMessageSender](src/main/kotlin/com/github/kpavlov/maya/sqs/SqsMessageSender.kt) for publishing messages from tests.

## Installation

Add a dependency to your project.

Gradle:
```kotlin
dependencies {
    testImplementation("com.github.kpavlov.maya:maya:${latestVersion}")
}
```
      
Maven:
```xml pom.xml
<dependency>
  <groupId>com.github.kpavlov.maya</groupId>
  <artifactId>maya</artifactId>
  <version>${latestVersion}</version>
</dependency>
```

## Local SQS Simulator

[LocalSqs](src/main/kotlin/com/github/kpavlov/maya/sqs/LocalSqs.kt)
starts local SQS server using [ElasticMQ](https://github.com/softwaremill/elasticmq) 
Docker [container](https://hub.docker.com/r/softwaremill/elasticmq-native/).
   
Make sure that docker daemon is running on your machine.

It can be customized by creating configuration file and storing 
it in the test classpath.
Configuration parameters are described [here](https://github.com/softwaremill/elasticmq#installation-stand-alone).

```kotlin
// Create and start local SQS Server
val sqs = LocalSqs(configPath="sqs-queues.conf")
sqs.start()

// Create SQS client connected to local SQS server
val sqsClient = SqsAsyncClient.builder()
    .endpointOverride(URI.create(sqs.endpointUrl()))
    .region(Region.US_EAST_1)
    .build()

// Create SqsMessageSender for Standard SQS Queue
val sender = SqsMessageSender<String>(
    sqsClient, 
    queueName = "test-queue",
    messageEncoder = identity()
)

// Send message to Standard queue
val messageId = sender.sendMessageAsync("Hello, World!").await()

// Create SqsMessageSender for Standard SQS Queue
val fifoSender = SqsMessageSender<String>(
    sqsClient,
    queueName = "test-queue.fifo",
    messageEncoder = Function.identity() ,
    messageGroupIdExtractor = { it.substring(0, 5) },
    messageDeduplicationIdExtractor = { it.hashCode().toString() }
)

// Send message to FIFO queue
val messageId = fifoSender.sendMessageAsync("Hello, FIFO World!").await()

// run some tests...

sqs.stop()
```

It is recommended to start SQS server only once before running all tests.
With JUnit5 you should run it in java static initializer, to make sure it is started before all tests in all classes.
