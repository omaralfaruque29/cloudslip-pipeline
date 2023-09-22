/*
package com.cloudslip.pipeline.config;

import com.cloudslip.pipeline.config.properties.SSLProperties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.HashMap;
import java.util.Map;

public final class KafkaSslConfig {

    private KafkaSslConfig() {
    }

    public static Map<String, Object> mapToKafkaProperties(SSLProperties sslProperties) {
        if (!sslProperties.isEnabled()) {
            return new HashMap<>(0);
        }

        Map<String, Object> sslConfig = new HashMap<>();
        try {
            if (!sslProperties.isEnabled()) {
                return sslConfig;
            }
            sslConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, sslProperties.getSecurityProtocol());
            sslConfig.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslProperties.getTruststoreLocation());
            sslConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslProperties.getTruststorePassword());
            sslConfig.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslProperties.getKeystoreLocation());
            sslConfig.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslProperties.getKeystorePassword());
            sslConfig.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslProperties.getKeyPassword());
            sslConfig.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, sslProperties.getProtocols());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sslConfig;
    }

}
*/
