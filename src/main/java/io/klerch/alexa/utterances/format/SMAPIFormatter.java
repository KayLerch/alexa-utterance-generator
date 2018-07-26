package io.klerch.alexa.utterances.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.klerch.alexa.utterances.model.SMAPIModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

public class SMAPIFormatter implements Formatter {
    private SMAPIModel model;

    public SMAPIFormatter(final String[] args) {
        if (args != null) {
            int index = ArrayUtils.indexOf(args, "-in");
            if (index < args.length - 1) {
                final String invocationName = args[index + 1];
                Validate.isTrue(!invocationName.startsWith("-"), "Please provide a valid invocation name.");
                this.model = new SMAPIModel(invocationName);
            }
        }
    }

    public SMAPIFormatter() {
        this(new String[] { "-in", "" });
    }

    public SMAPIFormatter(final String invocationName) {
        this(new String[] { "-in", invocationName});
    }

    @Override
    public void before() { }

    @Override
    public boolean write(final String sample) {
        model.getModel().addSample(sample);
        return true;
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public String generateSchema() {
        try {
            return new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(SerializationFeature.WRAP_ROOT_VALUE)
                    .writeValueAsString(model);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
