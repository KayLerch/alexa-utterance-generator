package io.klerch.alexa.utterances.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.klerch.alexa.utterances.model.LanguageModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class SMAPIFormatter implements Formatter {
    private LanguageModel model;

    public SMAPIFormatter(final String[] args) {
        if (args != null) {
            int index = ArrayUtils.indexOf(args, "-in");
            if (index < args.length - 1) {
                final String invocationName = args[index + 1];
                Validate.notBlank(invocationName, "Please provide an invocation-name.");
                Validate.isTrue(!invocationName.startsWith("-"), "Please provide a valid invocation-name.");
                this.model = new LanguageModel(invocationName);
            }
        }
    }

    public SMAPIFormatter(final String invocatioName) {
        this(new String[] { "-in", invocatioName});
    }

    @Override
    public void before() { }

    @Override
    public boolean write(final String sample) {
        model.addSample(sample);
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
