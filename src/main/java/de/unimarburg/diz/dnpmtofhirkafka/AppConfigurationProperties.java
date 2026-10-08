package de.unimarburg.diz.dnpmtofhirkafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppConfigurationProperties {
    String inputTopic;
    String outputTopic;
}
