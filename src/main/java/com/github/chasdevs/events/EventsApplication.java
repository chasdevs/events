package com.github.chasdevs.events;

import com.github.chasdevs.events.config.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
public class EventsApplication {

	@Bean
	CachedSchemaRegistryClient theirRegistryClient(SchemaRegistryConfig config) {
		return new CachedSchemaRegistryClient(config.getUrl(), config.getIdentityMapLimit(), config.getAuthConfigs());
	}

	public static void main(String[] args) {
		SpringApplication.run(EventsApplication.class, args);
	}

}
