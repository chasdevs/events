package com.github.chasdevs.events.clients.registry;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;

import java.util.Objects;

public class SchemaMetaData {
    private final int id;
    private final int version;
    private final String schema;

    public SchemaMetaData(final int id, final int version, final String schema) {
        this.id = id;
        this.version = version;
        this.schema = schema;
    }

    public static SchemaMetaData fromConfluentMetadata(SchemaMetadata confluentMetadata) {
        return new SchemaMetaData(confluentMetadata.getId(), confluentMetadata.getVersion(), confluentMetadata.getSchema());
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaMetaData metaData = (SchemaMetaData) o;
        return id == metaData.id &&
                version == metaData.version &&
                Objects.equals(schema, metaData.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, schema);
    }

    @Override
    public String toString() {
        return "SchemaMetaData{" +
                "id=" + id +
                ", version=" + version +
                ", schema='" + schema + '\'' +
                '}';
    }
}
