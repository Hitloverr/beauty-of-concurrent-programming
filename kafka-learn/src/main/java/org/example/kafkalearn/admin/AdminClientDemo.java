package org.example.kafkalearn.admin;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AdminClientDemo {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        AdminClient admin = AdminClient.create(props);
// TODO: 用AdminClient做一些有用的事情
        admin.close(Duration.ofSeconds(30));
    }

    private void listTopics(AdminClient client) throws ExecutionException, InterruptedException {
        ListTopicsResult listTopicsResult = client.listTopics();
        listTopicsResult.names().get().forEach(System.out::println);
    }

    private void describeTopics(AdminClient client) throws ExecutionException, InterruptedException {
        DescribeTopicsResult demoTopic = client.describeTopics(List.of("topic1"));
        TopicDescription topicDescription = null;
        try {
            topicDescription = demoTopic.values().get("topicName").get();
            if (topicDescription.partitions().size() != 3) {
                System.out.println("topic has wrong number of partitions");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof UnknownTopicOrPartitionException)) {
                e.printStackTrace();
                throw e;
            }
            // 创建新topic
            CreateTopicsResult newTopic = client.createTopics(List.of(new NewTopic("topic1", 3, (short) 1)));
            if (newTopic.numPartitions("topic1").get() != 3) {
                System.out.println("topic has wrong number of partitions");
            }
        }

    }

    private void deleteTopics(AdminClient client) throws ExecutionException, InterruptedException {
        client.deleteTopics(List.of("topic1")).all().get();
    }

    private void kafkaFuture(AdminClient admin) {
        DescribeTopicsResult demoTopic = admin.describeTopics(
                Collections.singletonList("topic"),
                new DescribeTopicsOptions().timeoutMs(1000));
        demoTopic.values().get("topic").whenComplete(new KafkaFuture.BiConsumer<TopicDescription, Throwable>() {
            @Override
            public void accept(TopicDescription topicDescription, Throwable throwable) {
                if (throwable != null) {

                } else {
                }
            }
        });

    }

    private void listConsumers(AdminClient admin) throws ExecutionException, InterruptedException {
        admin.listConsumerGroups().valid().get().forEach(System.out::println);

        // 获取消费者组所有分区最新的offset
        Map<TopicPartition, OffsetAndMetadata> offsets = admin.listConsumerGroupOffsets("group1").partitionsToOffsetAndMetadata().get();
        Map<TopicPartition, OffsetSpec> requestLastestOffset = new HashMap<>();
        for (TopicPartition tp : offsets.keySet()) {
            requestLastestOffset.put(tp, OffsetSpec.latest());
        }
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> lastestOffsets = admin.listOffsets(requestLastestOffset).all().get();
        for (Map.Entry<TopicPartition, OffsetAndMetadata> e : offsets.entrySet()) {
            String topic = e.getKey().topic();
            int partition = e.getKey().partition();
            long committedOffset = e.getValue().offset();
            long lastestOffset = lastestOffsets.get(e.getKey()).offset();
        }

    }
}
