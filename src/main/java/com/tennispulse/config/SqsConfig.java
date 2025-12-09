package com.tennispulse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.net.URI;

@Configuration
public class SqsConfig {

    private static final Logger log = LoggerFactory.getLogger(SqsConfig.class);

    @Value("${aws.sqs.endpoint}")
    private String endpoint;

    @Value("${aws.sqs.region}")
    private String region;

    @Value("${tennispulse.sqs.match-completed-queue-name}")
    private String matchCompletedQueueName;

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }

    @Bean
    public String matchCompletedQueueUrl(SqsClient sqsClient) {
        sqsClient.createQueue(
                CreateQueueRequest.builder()
                        .queueName(matchCompletedQueueName)
                        .build()
        );

        String url = sqsClient.getQueueUrl(
                GetQueueUrlRequest.builder()
                        .queueName(matchCompletedQueueName)
                        .build()
        ).queueUrl();

        log.info("MatchCompleted SQS queue URL: {}", url);
        return url;
    }
}

