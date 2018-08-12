package io.klerch.alexa.utterances.lambda;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.klerch.alexa.utterances.formatter.Formatter;
import io.klerch.alexa.utterances.formatter.JsonFormatter;
import io.klerch.alexa.utterances.output.MemoryOutputWriter;
import io.klerch.alexa.utterances.processor.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Lambda request handler for hosting the Generator as a serverless function on AWS
 */
public class Handler implements RequestHandler<Request, Response> {
    /**
     * Handles the incoming request
     * @param request request object
     * @param context execution context in Lambda
     * @return response object with generation results
     */
    @Override
    public Response handleRequest(final Request request, final Context context) {
        final MemoryOutputWriter outputWriter = new MemoryOutputWriter();
        final Formatter formatter = JsonFormatter.create(outputWriter).build();
        final List<String> errors = new ArrayList<>();
        Optional.ofNullable(request.getLines()).ifPresent(lines -> {
            try {
                Generator.create().withFormatter(formatter).build().generate(Arrays.asList(lines));
            } catch (Throwable ex) {
                errors.add(ex.getMessage());
            }
        });
        if (!errors.isEmpty()) {
            final StringBuilder errorPayload = new StringBuilder("{ 'errors' : [");
            errorPayload.append("'" + errors.get(0) + "'");
            errorPayload.append("]}");
            return new Response(500).withBody(errorPayload.toString());
        }
        return new Response(200)
                .withBody(outputWriter.getOutput())
                .withHeader("BuiltinIntents", outputWriter.getGeneration().getNumberOfBuiltinIntents())
                .withHeader("CustomIntents", outputWriter.getGeneration().getNumberOfCustomIntents())
                .withHeader("Intents", outputWriter.getGeneration().getNumberOfIntents())
                .withHeader("Slots", outputWriter.getGeneration().getNumberOfSlots())
                .withHeader("SlotTypes", outputWriter.getGeneration().getNumberOfSlotTypes())
                .withHeader("SlotValues", outputWriter.getGeneration().getNumberOfSlotValues())
                .withHeader("SlotValuesWithSynonyms", outputWriter.getGeneration().getNumberOfSlotValuesWithSynonyms())
                .withHeader("Utterances", outputWriter.getGeneration().getNumberOfUtterances());
    }
}
