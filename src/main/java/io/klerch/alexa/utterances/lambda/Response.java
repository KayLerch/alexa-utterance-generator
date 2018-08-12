package io.klerch.alexa.utterances.lambda;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Response object for Lambda call
 */
public class Response {
    private final Integer statusCode;
    private String body;
    private final Map<String, Object> headers = new HashMap<>();

    /**
     * New success response
     */
    public Response() {
        this(200);
    }

    /**
     * New response with defined HTTP status code
     * @param statusCode HTTP status code
     */
    public Response(final Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * set body (JSON schema)
     * @param body body (JSON schema)
     * @return response object
     */
    @JsonIgnore
    public Response withBody(final String body) {
        this.body = body;
        return this;
    }

    /**
     * Add some metadata to header
     * @param key metadata key
     * @param value metadata value
     * @return response object
     */
    @JsonIgnore
    public Response withHeader(final String key, final Object value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Get HTTP response code
     * @return HTTP response code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Get body (JSON schema)
     * @return body (JSON schema)
     */
    public String getBody() {
        return body;
    }

    /**
     * Get key value map of metadata
     * @return key value map of metadata
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }
}
