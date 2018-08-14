package io.klerch.alexa.utterances.processor;

import io.klerch.alexa.utterances.formatter.JsonFormatter;
import io.klerch.alexa.utterances.model.*;
import io.klerch.alexa.utterances.output.ConsoleOutputWriter;
import io.klerch.alexa.utterances.util.Resolver;
import io.klerch.alexa.utterances.util.ResourceReader;
import io.klerch.alexa.utterances.formatter.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Takes file and path references to *.grammar and *.values files or inputs as a list of strings and converts the grammar to an
 * Alexa skill interaction model you can use to deploy your skill right away
 */
public class Generator {
    /**
     * Default invocation name used in case none is specified in the grammar nor set on initialization of Generator class
     */
    private String DEFAULT_INVOCATION_NAME = "my skill";
    /**
     * Default intent name used if sample utterances found in the grammar not assigned to any intent
     */
    private String DEFAULT_INTENT_NAME = "MyIntent";
    /**
     * Before conversion to JSON schema the grammar specification will be broken down to entities represented as "scopes"
     */
    private final Map<GeneratorScope, Map<String, List<String>>> sourceMap = new HashMap<>();

    /**
     * Formatter encapsulates logic to format output string
     */
    private final Formatter formatter;
    /**
     * Holds invocation name given during initialization of Generator class and potentially overwrites an invocation name
     * specified in the grammar.
     */
    private String invocationName;
    /**
     * JSON object holding the resulting Alexa skill interaction schema generated from the grammar
     */
    private InteractionModel model;
    /**
     * Reference to file object of grammar
     */
    private final File grammarFile;
    /**
     * Reference to path containing values files
     */
    private final Path valuesFilePath;

    private boolean skipValidation = false;
    private boolean skipCleanup = false;

    /**
     * Generator converts grammar specification with sample utterances, intent mappings and slot values to
     * an Alexa skill interaction model you can use to deploy your skill right away
     * @param builder Generator builder
     */
    private Generator(final GeneratorBuilder builder) {
        formatter = builder.formatter;
        grammarFile = builder.grammarFile;
        valuesFilePath = builder.valuesFilePath;
        invocationName = builder.invocationName;
        skipCleanup = builder.skipCleanup;
        skipValidation = builder.skipValidation;
        model = new InteractionModel(Optional.ofNullable(builder.invocationName).orElse(DEFAULT_INVOCATION_NAME));
    }

    /**
     * Generates grammar from referenced file to an Alexa interaction model schema. If you did not assign a grammar file
     * reference to the Generator this method will throw an exception. Consider to use generate(List of strings) in case you'd like
     * to provide your grammar specification as a list of strings rather than letting Generator read it from file.
     */
    public void generate() {
        Validate.notNull(grammarFile, "Calling generate() without parameters is only supported when initializing Generator class with reference to an existing *.grammar file");
        generate(ResourceReader.getLines(grammarFile));
    }

    /**
     * Generates grammar from list of strings to an Alexa interaction model schema.
     * @param lines grammar specification line by line as it appears in a typical *.grammar file. Please notice that
     * in grammar specification line-breaks matter. That being said, you must not include multiple specification (e.g. sample utterances)
     * in one line.
     */
    public Generation generate(final List<String> lines) {
        // process and fill model
        resolve(lines);
        // post processing on model content
        if (!skipCleanup) model.cleanUp();
        if (!skipValidation) model.validate();
        // wrap model in result object and send to formatter for output
        final Generation output = new Generation(model);
        formatter.print(new Generation(model));
        return output;
    }

    /**
     * Does the actual job of converting the grammar to schema
     * @param lines grammar specification line by line as it appears in a typical *.grammar file
     */
    private void resolve(final List<String> lines) {
        preprocess(lines);
        // introduce a new entity which holds all the resolved placeholders in utterances
        sourceMap.putIfAbsent(GeneratorScope.VARIANT_PHRASES, new HashMap<>());
        // get invocation name from file or use default
        Optional.ofNullable(sourceMap.get(GeneratorScope.INVOCATION)).ifPresent(invocation -> {
            Optional.ofNullable(invocation.get("Invocation")).filter(i -> !i.isEmpty() && StringUtils.isNotBlank(i.get(0))).ifPresent(name -> {
                model.setInvocationName(name.get(0));
            });
        });
        // go through all placeholder definitions
        sourceMap.get(GeneratorScope.PLACEHOLDER).forEach((placeholderName, values) -> {
            // resolve from one to many values in case they are grouped in brackets
            final List<String> resolvedValues = Resolver.resolveSlotValues(values);
            sourceMap.get(GeneratorScope.PLACEHOLDER).get(placeholderName).clear();
            sourceMap.get(GeneratorScope.PLACEHOLDER).get(placeholderName).addAll(resolvedValues);
        });
        sourceMap.get(GeneratorScope.INTENT).forEach((intentName, utterances) -> {
            // go from one to many utterances by resolving placeholders (slots, alternate phrases)
            final Intent intent = model.addIntent(intentName);
            final List<String> resolvedUtterances = new ArrayList<>();
            final List<Slot> slotsOfIntent = new ArrayList<>();
            utterances.forEach(utteranceLine -> {
                final StringBuffer utteranceResolvedSlotsBuffer = new StringBuffer();
                // extract {{slot}} placeholders
                final Matcher slotPlaceholders = Resolver.slotInUtteranceDefinition.matcher(utteranceLine);
                final List<Slot> slotsInUtterance = new ArrayList<>();
                while (slotPlaceholders.find()) {
                    final List<Slot> slotsInGroup = new ArrayList<>();
                    final String placeholder = slotPlaceholders.group(0).replaceAll("[{}]", "");
                    // substitute slot placeholder with a random id which is associated to the collection of slots
                    // hold slot key in case it is defined in order to apply it to all slot type references in the placeholder
                    final AtomicReference<String> slotKey = new AtomicReference<>("");
                    final AtomicBoolean hasWhitespace = new AtomicBoolean(false);
                    // slot placeholder can hold variants as well, split and go through them
                    Arrays.stream(placeholder.split("[|,;]", -1)).distinct().map(String::trim).forEach(slotRef -> {
                        if (StringUtils.isNotBlank(slotRef)) {
                            final String[] keyValue = slotRef.split(":");
                            final String slotType = keyValue.length > 1 ? keyValue[1] : slotRef;
                            // slot name as defined otherwise key as defined for preceding value otherwise key is value itself
                            final String slotName = keyValue.length > 1 && StringUtils.isNotBlank(keyValue[0]) ? keyValue[0] :
                                    StringUtils.isNotBlank(slotKey.get()) && (!slotKey.get().startsWith("AMAZON_")) ? slotKey.get() : slotRef.replaceAll("[:.]", "_");
                            // ensure unique slot name within utterance
                            slotKey.set(slotName);
                            final String slotNameUnique = Resolver.resolveToUniqueSlotName(slotsInUtterance, slotName);
                            // keep track of slot in utterance to add a reference for the intent later on
                            final Slot slot = new Slot(slotNameUnique, slotType);
                            slotsInGroup.add(slot);
                            slotsInUtterance.add(slot);
                        } else {
                            hasWhitespace.set(true);
                        }
                    });
                    // since slot alias could have changed we need the hash key for the new name/type pairs
                    final String updatedPlaceholder = StringUtils.join(slotsInGroup.stream().map(s -> s.getName() + ":" + s.getType()), "|");
                    final String slotPlaceholderId = Integer.toString(updatedPlaceholder.hashCode());

                    if (!sourceMap.get(GeneratorScope.VARIANT_PHRASES).containsKey(slotPlaceholderId)) {
                        final List<String> slots = new ArrayList<>();
                        // if this placeholder got a leading or trailing separator keep in mind that whitespace is an option
                        if (hasWhitespace.get()) slots.add("");
                        slotsInGroup.forEach(slot -> {
                            slots.add(slot == null ? "" : "{" + slot.getName() + "}");
                        });
                        sourceMap.get(GeneratorScope.VARIANT_PHRASES).put(slotPlaceholderId, slots);
                    }
                    // substitute slot collection with its hash key
                    slotPlaceholders.appendReplacement(utteranceResolvedSlotsBuffer, Matcher.quoteReplacement("{!" + slotPlaceholderId + "}"));
                }
                slotsInUtterance.forEach(slotInUtterance -> {
                    if (slotsOfIntent.stream().noneMatch(s -> s.getName().equals(slotInUtterance.getName()))) {
                        slotsOfIntent.add(slotInUtterance);
                    }
                    final String slotTypeName = slotInUtterance.getType();
                    // looking for slot values not defined in grammar file but sitting in values files
                    if (!sourceMap.get(GeneratorScope.PLACEHOLDER).containsKey(slotTypeName)) {
                        ResourceReader.getPlaceholderValueList(valuesFilePath, slotTypeName).map(Resolver::resolveSlotValues).ifPresent(values -> {
                            sourceMap.get(GeneratorScope.PLACEHOLDER).put(slotTypeName, values);
                        });
                    }
                });
                slotPlaceholders.appendTail(utteranceResolvedSlotsBuffer);
                // get new utterance resolved by slot definitions which are now represented by {!UUID}
                final String utteranceResolvedSlots = utteranceResolvedSlotsBuffer.toString();
                final StringBuffer utteranceResolvedPlaceholdersBuffer = new StringBuffer();
                // extract all remaining placeholders while ignoring the already resolved {!UUID} slot placeholders
                final Matcher variantPlaceholders = Resolver.placeholderInUtteranceExcludingResolved.matcher(utteranceResolvedSlots);
                while (variantPlaceholders.find()) {
                    final String placeholder = variantPlaceholders.group(0).replaceAll("[{}]", "");
                    final String variantPlaceholderId = Integer.toString(placeholder.hashCode());
                    // skip resolving values if it has already been done for the same string in a previous placeholder
                    if (!sourceMap.get(GeneratorScope.VARIANT_PHRASES).containsKey(variantPlaceholderId)) {
                        final List<String> values = new ArrayList<>();
                        // is not an alternate phrasing (e.g. {bookingItem} and not {my,alternate,phrases})
                        final boolean alternate = placeholder.matches(".*[|,;].*");
                        if (!placeholder.matches(".*[|,;].*")) {
                            // first check if this variant is referencing a placeholder
                            if (sourceMap.get(GeneratorScope.PLACEHOLDER).containsKey(placeholder)) {
                                // if yes, resolve it by adding all values as variant phrasings
                                values.addAll(sourceMap.get(GeneratorScope.PLACEHOLDER).get(placeholder).stream().map(String::trim).map(value -> value.contains(":") ? value.split(":", -1)[1] : value).collect(Collectors.toList()));
                            }
                            // just in case this is
                            else {
                                // try get placeholder values from file if path to values files set
                                // if it could not be resolved (no values file with placeholder as file key) the placeholder itself will be the resulting string
                                final List<String> valuesList = ResourceReader.getPlaceholderValueList(valuesFilePath, placeholder).map(Resolver::resolveSlotValues).orElse(Collections.singletonList(placeholder));
                                // store in source map to only read values from file once
                                sourceMap.get(GeneratorScope.PLACEHOLDER).put(placeholder, valuesList);
                                values.addAll(valuesList.stream().map(value -> value.split(":", -1)[0]).collect(Collectors.toList()));
                            }
                        }
                        else {
                            // otherwise add all variant phrases to the new entity
                            values.addAll(Arrays.stream(placeholder.split("[|,;]", -1)).map(String::trim).collect(Collectors.toList()));
                        }
                        sourceMap.get(GeneratorScope.VARIANT_PHRASES).put(variantPlaceholderId, values);
                    }
                    variantPlaceholders.appendReplacement(utteranceResolvedPlaceholdersBuffer, Matcher.quoteReplacement("{!" + variantPlaceholderId + "}"));
                }
                variantPlaceholders.appendTail(utteranceResolvedPlaceholdersBuffer);
                // finally add utterance to list whose placeholders were entirely resolved
                resolvedUtterances.add(utteranceResolvedPlaceholdersBuffer.toString());
            });
            resolvedUtterances.forEach(resolvedUtterance -> {
                // list of pairs holds all placeholder value collections relevant for this utterance
                final List<Pair<String, List<String>>> resolvedPlaceholdersForUtterance = new ArrayList<>();
                // now get all the resolved placeholders currently existing in the utterances as {!Hashkey}
                final Matcher resolvedPlaceholders = Resolver.resolvedPlaceholders.matcher(resolvedUtterance);
                while(resolvedPlaceholders.find()) {
                    final String placeholder = resolvedPlaceholders.group(0);
                    // extract id from the placeholder to look up the corresponding value collection in the source map
                    final String placeholderId = placeholder.replaceAll("[{}!]", "");
                    resolvedPlaceholdersForUtterance.add(new ImmutablePair<>(placeholder, sourceMap.get(GeneratorScope.VARIANT_PHRASES).get(placeholderId)));
                }
                // recursively substitute placeholders in utterances which generates and adds all permutations to the intent
                generatePermutations(resolvedPlaceholdersForUtterance, 0, resolvedUtterance, intent);

            });
            // add all the slots referenced in the utterances of this intent
            slotsOfIntent.forEach(slot -> {
                // add slot with reference to current intent
                intent.addSlot(slot);
                // check if referenced type got a placeholder definition entity
                if (sourceMap.get(GeneratorScope.PLACEHOLDER).containsKey(slot.getType())) {
                    // only add new slot type to the model if not already done with a previous reference
                    if (!model.hasSlotType(slot)) {
                        final SlotType slotType = Resolver.resolveSlotType(slot.getType(), sourceMap.get(GeneratorScope.PLACEHOLDER).get(slot.getType()));
                        model.addSlotType(slotType);
                    }
                } else {
                    // if not this is only valid for builtins - otherwise throw an exception
                    Validate.isTrue(slot.getType().startsWith("AMAZON."), "Slot type " + slot.getType() + " was not defined and has no values.");
                }
            });
        });
    }

    /**
     * Recursively resolves placeholders and variant phrasing from 1 to N utterances
     * @param lists list of placeholders and their actual substitutions
     * @param depth depth
     * @param utterance currently processed utterance (goes down the path until all placeholders have been resolved)
     * @param intent intent of the currently processed utterance
     */
    private void generatePermutations(final List<Pair<String, List<String>>> lists, final int depth, final String utterance, final Intent intent)
    {
        if(depth == lists.size()) {
            intent.addSample(utterance.trim().replaceAll("\\s+"," "));
            return;
        }

        for(int i = 0; i < lists.get(depth).getValue().size(); ++i) {
            final Pair<String, List<String>> placeholder = lists.get(depth);
            final String placeholderValue = placeholder.getValue().get(i);
            final String placeholderName = placeholder.getKey();
            generatePermutations(lists, depth + 1, utterance.replaceFirst(Pattern.quote(placeholderName), placeholderValue), intent);
        }
    }

    /**
     * Before conversion to JSON schema the grammar specification will be broken down to entities represented as "scopes" and
     * put into the source map. This method is called by one of the generate methods.
     * @param lines
     */
    @SuppressWarnings("unchecked")
    private void preprocess(final List<String> lines) {
        sourceMap.put(GeneratorScope.PLACEHOLDER, new HashMap<>());
        sourceMap.put(GeneratorScope.INVOCATION, new HashMap<>());
        sourceMap.put(GeneratorScope.INTENT, new HashMap<>());

        // keep track of last declared scope as not each line has it (default to dummy intent MyIntent
        final AtomicReference<ImmutablePair<GeneratorScope, String>> currentScope = new AtomicReference<>(new ImmutablePair<>(GeneratorScope.INTENT, DEFAULT_INTENT_NAME));

        lines.stream()
                .map(String::trim) // eliminate leading and trailing spaces
                .filter(line -> !line.startsWith("//")) // ignore comment lines
                .map(line -> line.split("//")[0].trim()) // eliminate inline comments
                .filter(StringUtils::isNotBlank) // ignore blank lines
                .forEach(line -> {
                    // check for any new definition in the current line
                    final ImmutableTriple<GeneratorScope, String, String> resolution = Resolver.resolveInvocationDefinition(line).map(l -> new ImmutableTriple(GeneratorScope.INVOCATION, l.left, l.right)).orElse(
                            Resolver.resolveIntentDefinition(line).map(l -> new ImmutableTriple(GeneratorScope.INTENT, l.left, l.right)).orElse(
                                    Resolver.resolveSlotTypeDefinition(line).map(l -> new ImmutableTriple(GeneratorScope.PLACEHOLDER, l.left, l.right))
                                            .orElse(null)));
                    Optional.ofNullable(resolution).ifPresent(res -> {
                        final GeneratorScope newScope = res.left;
                        final String definitionKey = res.middle;
                        final String definitionVal = res.right;
                        // ensure entity is registered within scope (e.g. MyIntent in scope INTENT)
                        sourceMap.get(newScope).putIfAbsent(definitionKey, new ArrayList<>());
                        // add value to entity in scope if not blank (e.g. utterance for MyIntent in scope INTENT
                        if (StringUtils.isNotBlank(definitionVal)) sourceMap.get(newScope).get(definitionKey).add(definitionVal);
                        // remember this scope for upcoming lines
                        currentScope.set(new ImmutablePair<>(newScope, definitionKey));
                    });
                    // if line does not have a definition it belongs to the last processed definition (e.g. an INTENT)
                    if (resolution == null) {
                        // do not follow invocation scope in case invocation name has already been set
                        if (currentScope.get().left.equals(GeneratorScope.INVOCATION) &&
                                !sourceMap.get(GeneratorScope.INVOCATION).get(currentScope.get().right).isEmpty() &&
                                StringUtils.isNotBlank(sourceMap.get(GeneratorScope.INVOCATION).get(currentScope.get().right).get(0))) {
                            // instead continue with default intent
                            currentScope.set(new ImmutablePair<>(GeneratorScope.INTENT, DEFAULT_INTENT_NAME));
                        }
                        sourceMap.get(currentScope.get().left).putIfAbsent(currentScope.get().right, new ArrayList<>());
                        sourceMap.get(currentScope.get().left).get(currentScope.get().right).add(line);
                    }
                });
    }

    /**
     * Create a new Generator object which converts grammar and values specification to an Alexa skill interaction model schema
     * @return generator builder taking all the inputs like reference to your grammar specification
     */
    public static GeneratorBuilder create() {
        return new GeneratorBuilder();
    }

    /**
     * Generator builder taking all the inputs like reference to your grammar specification
     */
    public static class GeneratorBuilder {
        private Formatter formatter = JsonFormatter.create(new ConsoleOutputWriter()).build();
        private String invocationName;
        private File grammarFile;
        private Path valuesFilePath;
        private boolean skipValidation = false;
        private boolean skipCleanup = false;

        /**
         * New generator builder
         */
        GeneratorBuilder() {
        }

        /**
         * Formatter encapusaltes logic of formatting output string and delegates to an outputwriter to print it out
         * @param formatter instance of a Formatter
         * @return generator builder
         */
        public GeneratorBuilder withFormatter(final Formatter formatter) {
            this.formatter = formatter;
            return this;
        }

        /**
         * Sets the invocation name for the skill whose model is generated. If may overwrite the invocation name
         * specified in the grammar
         * @param invocationName invocation name for your Alexa skill
         * @return generator builder
         */
        public GeneratorBuilder withInvocationName(final String invocationName) {
            this.invocationName = invocationName;
            return this;
        }

        /**
         * File reference to your grammar specification. If you don't hold your specification in a file and you
         * do not set a reference make sure you call generate(List of strings) instead of generate() in the Generator
         * @param grammarFile file reference to your grammar. Make sure this is an absolute path
         * @return generator builder
         */
        public GeneratorBuilder withGrammarFile(final File grammarFile) {
            this.grammarFile = grammarFile;
            return this;
        }

        /**
         * Path reference to folder location where all your values files are stored. This is optional as you may not
         * have used any placeholders in your grammar or specified values within your grammar already. If not set the Generator
         * will look up values files according to the name of unresolved placeholders in your grammar specification in the folder
         * your grammar file is in.
         * @param valuesFilePath path reference to folder location where all your values files can be found. Should be an absolute path.
         * @return generator builder
         */
        public GeneratorBuilder withValuesFilePath(final Path valuesFilePath) {
            this.valuesFilePath = valuesFilePath;
            return this;
        }

        /**
         * Disables validation of output which may throw errors in case of e.g. duplicate sample utterances across multiple intents
         * @param skipValidation true to disable validation
         * @return generator builder
         */
        public GeneratorBuilder disableValidation(final boolean skipValidation) {
            this.skipValidation = skipValidation;
            return this;
        }

        /**
         * Disables cleanup of output e.g. deduplication and sorting of sample utterances
         * @param skipCleanup true to disable clean up
         * @return generator builder
         */
        public GeneratorBuilder disableCleanup(final boolean skipCleanup) {
            this.skipCleanup = skipCleanup;
            return this;
        }

        /**
         * builds the Generator object
         * @return generator object
         */
        public Generator build() {
            Validate.noNullElements(Collections.singletonList(formatter), "Generator needs a Formatter instance to process.");
            Validate.isTrue(grammarFile == null || grammarFile.canRead(), "Could not obtain read access to grammar file.");
            return new Generator(this);
        }
    }
}
