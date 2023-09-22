package com.cloudslip.pipeline.updated.config;

import com.cloudslip.pipeline.updated.config.properties.KafkaCloudslipProducerProperties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {


    @Bean
    public ProducerFactory<String, String> kafkaCloudslipProducerFactory(KafkaCloudslipProducerProperties kafkaCloudslipProducerProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCloudslipProducerProperties.getBootstrapServers());
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaCloudslipProducerProperties.getClientId());
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaCloudslipProducerProperties.getAcks());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaCloudslipProducerProperties.getBufferMemory());
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaCloudslipProducerProperties.getMaxRequestSize());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaCloudslipProducerProperties.getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, kafkaCloudslipProducerProperties.getLingerInMs());
        configProps.put(ProducerConfig.RETRIES_CONFIG, kafkaCloudslipProducerProperties.getRetries());
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaCloudslipProducerProperties.getCompressionType());
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaCloudslipProducerProperties.getRequestTimeoutMs());
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(@Qualifier("kafkaCloudslipProducerFactory")
                                                               ProducerFactory<String, String> kafkaCloudslipProducerFactory) {
        return new KafkaTemplate<>(kafkaCloudslipProducerFactory);
    }
}
