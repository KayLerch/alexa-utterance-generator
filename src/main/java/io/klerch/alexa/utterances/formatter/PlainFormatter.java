package io.klerch.alexa.utterances.formatter;

import io.klerch.alexa.utterances.model.Generation;
import io.klerch.alexa.utterances.output.OutputWriter;
import org.apache.commons.lang3.StringUtils;

import static io.klerch.alexa.utterances.formatter.PlainFormatter.EditorialFormatterBuilder.INTENT_DISPLAY_OPTION.ONCE_ON_TOP;
import static io.klerch.alexa.utterances.formatter.PlainFormatter.EditorialFormatterBuilder.INTENT_DISPLAY_OPTION.ONCE_PER_LINE;
import static io.klerch.alexa.utterances.formatter.PlainFormatter.EditorialFormatterBuilder.SLOT_TYPE_DISPLAY_OPTION.*;

public class PlainFormatter implements Formatter {
    private final EditorialFormatterBuilder.INTENT_DISPLAY_OPTION intentDisplay;
    private final EditorialFormatterBuilder.SLOT_TYPE_DISPLAY_OPTION slotTypeDisplay;
    private final boolean displayInvocationName;
    private final OutputWriter writer;

    @Override
    public void print(final Generation generation) {
        final StringBuilder sb = new StringBuilder();

        if (displayInvocationName) {
            sb.append("Invocation: ").append(generation.getModel().getInvocationName()).append("\n----------------------\n\n");
        }

        generation.getModel().getModel().getIntents().forEach(intent -> {
            if (intentDisplay.equals(ONCE_ON_TOP)) sb.append(intent.getName()).append("\n----------------------\n");
            intent.getSamples().forEach(sample -> {
                if (intentDisplay.equals(ONCE_PER_LINE)) sb.append(intent.getName()).append(": ");
                sb.append(sample).append("\n");
            });
            sb.append("\n");
        });

        if (!slotTypeDisplay.equals(NONE) && generation.getModel().getModel().getSlotTypes().size() > 0) {
            generation.getModel().getModel().getSlotTypes().forEach(slotType -> {
                sb.append(slotType.getName()).append("\n----------------------\n");
                slotType.getValues().forEach(value -> {
                    if (!slotTypeDisplay.equals(VALUES_ONLY)) sb.append(value.getId()).append(": ");
                    if (!slotTypeDisplay.equals(ID_ONLY)) {
                        sb.append(value.getSlotName().getValue()).append(", ");
                        sb.append(StringUtils.join(value.getSlotName().getSynonyms(), ", "));
                    }
                    sb.append("\n");
                });
                sb.append("\n");
            });
        }
        writer.print(sb.toString(), generation);
    }

    private PlainFormatter(final EditorialFormatterBuilder builder) {
        this.intentDisplay = builder.intentDisplay;
        this.slotTypeDisplay = builder.slotTypeDisplay;
        this.displayInvocationName = builder.displayInvocationName;
        this.writer = builder.writer;
    }

    public static EditorialFormatterBuilder create(final OutputWriter writer) {
        return new EditorialFormatterBuilder(writer);
    }

    public static class EditorialFormatterBuilder {
        public enum INTENT_DISPLAY_OPTION {
            ONCE_ON_TOP, ONCE_PER_LINE, NONE
        }

        public enum SLOT_TYPE_DISPLAY_OPTION {
            ID_AND_VALUES, ID_ONLY, VALUES_ONLY, NONE
        }

        private INTENT_DISPLAY_OPTION intentDisplay = INTENT_DISPLAY_OPTION.NONE;
        private SLOT_TYPE_DISPLAY_OPTION slotTypeDisplay = NONE;
        private boolean displayInvocationName = false;
        private OutputWriter writer;

        public EditorialFormatterBuilder(final OutputWriter writer) {
            this.writer = writer;
        }

        public EditorialFormatterBuilder displayIntent(final INTENT_DISPLAY_OPTION option) {
            this.intentDisplay = option;
            return this;
        }

        public EditorialFormatterBuilder displaySlotType(final SLOT_TYPE_DISPLAY_OPTION option) {
            this.slotTypeDisplay = option;
            return this;
        }

        public EditorialFormatterBuilder displayInvocationName(final boolean displayYes) {
            this.displayInvocationName = displayYes;
            return this;
        }

        public PlainFormatter build() {
            return new PlainFormatter(this);
        }
    }
}
