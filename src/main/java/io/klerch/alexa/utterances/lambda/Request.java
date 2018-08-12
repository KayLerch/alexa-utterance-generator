package io.klerch.alexa.utterances.lambda;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expected request format of Lambda call
 */
public class Request {
    @JsonProperty
    private String body;
    @JsonProperty
    private String[] lines;
    @JsonProperty
    private LinkedHashMap<String, Object> queryStringParameters;

    /**
     * New Request
     */
    public Request() {
    }

    /**
     * get grammar specification line by line
     * @return grammar specification line by line
     */
    public String[] getLines() {
        return lines;
    }

    /**
     * set grammar specification line by line
     * @param lines grammar specification line by line
     */
    public void setLines(final String[] lines) {
        this.lines = lines;
    }
}
