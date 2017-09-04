package io.klerch.alexa.utterances.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resolver {
    public static List<String> resolveSlotValue(final String placeHolderName) {
        final List<String> values = new ArrayList<>();

        Arrays.stream(placeHolderName.split("\\|")).forEach(value -> {
            final String[] ranges = value.split("-");
            if (ranges.length == 2 && NumberUtils.isParsable(ranges[0]) && NumberUtils.isParsable(ranges[1])) {
                final int r1 = Integer.parseInt(ranges[0]);
                final int r2 = Integer.parseInt(ranges[1]);
                final int min = Integer.min(r1, r2);
                final int max = Integer.max(r1, r2);

                for (Integer i = min; i <= max; i++) {
                    values.add(i.toString());
                }
            } else {
                values.add(value);
            }
        });
        return values;
    }
}
