package io.klerch.alexa.utterances.formatter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.klerch.alexa.utterances.model.Generation;
import io.klerch.alexa.utterances.output.OutputWriter;

import java.io.IOException;
import java.io.Writer;

public class JsonFormatter implements Formatter {
    private final OutputWriter writer;

    private JsonFormatter(final JsonFormatterBuilder builder) {
        this.writer = builder.writer;
    }

    @Override
    public void print(final Generation generation) {
        try {
            final String schema = new ObjectMapper(new Factory())
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(SerializationFeature.WRAP_ROOT_VALUE)
                    .writeValueAsString(generation.getModel());
            writer.print(schema, generation);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonFormatterBuilder create(final OutputWriter writer) {
        return new JsonFormatterBuilder(writer);
    }

    public static class JsonFormatterBuilder {
        private OutputWriter writer;

        JsonFormatterBuilder(final OutputWriter writer) {
            this.writer = writer;
        }

        public JsonFormatter build() {
            return new JsonFormatter(this);
        }
    }

    /**
     * Custom pretty printer for JSON to print array items in separated lines
     */
    private static class PrettyPrinter extends DefaultPrettyPrinter {
        public static final PrettyPrinter instance = new PrettyPrinter();

        PrettyPrinter() {
            _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        }
    }

    /**
     * Custom JsonFactory to set custom pretty printer
     */
    private static class Factory extends JsonFactory {
        @Override
        protected JsonGenerator _createGenerator(final Writer out, final IOContext ctxt) throws IOException {
            return super._createGenerator(out, ctxt).setPrettyPrinter(PrettyPrinter.instance);
        }
    }
}
