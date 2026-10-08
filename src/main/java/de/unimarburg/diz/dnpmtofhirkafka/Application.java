package de.unimarburg.diz.dnpmtofhirkafka;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import dev.pcvolkmer.mv64e.model.Converter;
import dev.pcvolkmer.onco.datamapper.fhir.PatientRecordMapper;
import dev.pcvolkmer.onco.datamapper.fhir.builders.DizUniMrReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.filter.AtcCodeFilter;
import dev.pcvolkmer.onco.datamapper.fhir.filter.Filter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;

@SpringBootApplication
@EnableConfigurationProperties(AppConfigurationProperties.class)
public class Application {
    static Logger log = LoggerFactory.getLogger(Application.class);

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private final AppConfigurationProperties appConfigurationProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PatientRecordMapper mapper;
    private final IParser fhirJsonParser;

    public Application(
            final AppConfigurationProperties appConfigurationProperties,
            final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.appConfigurationProperties = appConfigurationProperties;
        this.kafkaTemplate = kafkaTemplate;
        this.fhirJsonParser = FhirContext.forR4().newJsonParser();

        // Build patient record mapper for DIZ Uni-Marburg
        final var filters = new ArrayList<Filter<?>>();
        filters.add(new AtcCodeFilter());
        this.mapper = PatientRecordMapper.customInstance(new DizUniMrReferenceBuilder(), filters);
    }

    @KafkaListener(topics = "${app.input-topic}")
    public void handleRecord(ConsumerRecord<String, String> record) {
        try {
            final var patientRecord = Converter.fromJsonString(record.value());
            final var bundle = mapper.mapToBundle(patientRecord);
            final var bundleJson = fhirJsonParser.encodeResourceToString(bundle);
            kafkaTemplate.send(appConfigurationProperties.outputTopic, bundleJson);
        } catch (Exception e) {
            log.error("Error handling record for '{}'", record.key(), e);
        }
    }
}
