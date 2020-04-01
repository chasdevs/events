package com.github.chasdevs.events.util;

import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.idl.ParseException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Simple utility class for parsing schemas and protocols from specified file paths.
 */
public class AvroParser {

    public static Protocol protocolFromIdl(Path pathToIdl) throws IOException, ParseException {
        Idl idl = new Idl(pathToIdl.toFile());
        Protocol protocol = idl.CompilationUnit();
        return protocol;
    }

    public static Schema schemaFromIdl(Path pathToIdl, String type) throws IOException, ParseException {
        return protocolFromIdl(pathToIdl).getTypes().stream().filter(t -> t.getName().equals(type)).findFirst().orElse(null);
    }
}