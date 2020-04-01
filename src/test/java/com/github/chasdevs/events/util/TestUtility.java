package com.github.chasdevs.events.util;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Small IO utility class to help with reading any configuration or fixture data across the project.
 */
public class TestUtility {

    //todo - move this to a generic IO utility in src/main/resources, write simple test
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtility.class);

    private static final String GENERATED_SCHEMA_AVSC = "src/test/resources/avsc/v1/ClickEvent.avsc";
    private static final String GENERATED_SCHEMA_NEW_AVSC = "src/test/resources/avsc/v2/ClickEvent.avsc";

    public static Optional<Schema> getSchemaFixtureData() {
        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(new File(GENERATED_SCHEMA_AVSC));
        } catch (IOException e) {
            LOGGER.error("Could not read test schema", e);
        }
        return Optional.of(schema);
    }

    public static Optional<Schema> getUpdatedSchemaFixtureData() {
        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(new File(GENERATED_SCHEMA_NEW_AVSC));
        } catch (IOException e) {
            LOGGER.error("Could not read test schema", e);
        }
        return Optional.of(schema);
    }
}
