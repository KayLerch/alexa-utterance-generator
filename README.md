# Alexa Utterance and Schema Generator
Use this tool if you'd like to generate hundreds and thousands of variant utterances
for your Alexa skills. The tool spits out the list of utterances or a full-fledged interaction
model you can drag and drop right into Skill Builder in the Amazon developer console or upload via Skill management API (SMAPI).

## Latest updates
* Define custom slot names in sample utterances like "MyIntent: book a flight to {{city:AMAZON.US_CITY}}"
* For slot values you can now assign custom slot ids like "MySlotId: {value|synonym1|synonym2|synonym3}"

## Benefits
* Maintain all assets of your skill interaction model in human-readable files in your sources
* Auto-generate the interaction model from your sources
* Provide more consistency and variety in your sample utterance collection by defining their grammar rather than writing them down one by one
* Don't care about duplicates and overlaps in utterances and slot values as this tool eliminates them
* Avoid common pitfalls during certification of skills regarding schema compliance of the interaction model

This approach is a best practice. The quality and quantity of your sample utterances impacts the NLU of Alexa
and avoids friction and false positives in your Alexa skills.

## How to

### 1) Define your sample-utterance grammar file

Create a new .grammar file in the _/src/main/resources/utterances_ folder with file ending _*.grammar_ i.e. _booking.grammar_.
Start writing down sample utterances all starting with the intent-name they should be part of. The main difference from
doing this in the developer console is that you can make use of grammar-like syntax to provide more variety while reducing the
number of lines you need to write down.

```xml
BookingIntent: {|please} help me {get|book|order} a {taxi|room|table} for {1-12} people
```

The tool will start generating permutations i.e. _BookingIntent help me get a taxi for 2 people_ or _BookingIntent please help me book a room for 4 people_.
The above example results in 2 * 3 * 3 * 12 = 216 sample utterances.

If you're separating the first string with a colon from the rest of the line, the tool treats it 
as the intent name. You don't have to place it in every line. Following lines are applied to the
last defined intent name before there's a new intent definition.

```xml
BookingIntent:
{|please} help me {get|book|order} a {taxi|room|table}
{|please} help me {get|book|order} a {taxi|room|table} for {1-12} people

FlightBooking:
{|please} {i|we} {want|need|like} to {get|book|order} a flight
 ```

### 2) Define your slot value files and refer to them in your grammar

This is optional but super useful. If you don't want to have all your slot values defined inline like above you
create a new .values file in the _/src/main/resources/slots_ folder with file-ending _*.values_ i.e. _bookingType.values_.
List the slot values separated with a line-break.

```xml
taxi
room
table
```
and refer to it in your grammar with the file key.

```xml
BookingIntent: {|please} help me {get|book|order} a {bookingType} for {1-12} people
```

This example has the same effect as the above. 

#### Escape slots to prevent auto resolution

By now, we only used the slots in our grammar as a convenient option to reduce the number of sample utterances 
to define at design time. Of course, you'd like to keep some of the slots in the resulting set of utterances as you
want to catch they values at runtime and process them in your skill-code. Escape from resoltuons with the following:

```xml
BookingIntent: {|please} help me {get|book|order} a {{bookingType}} for {1-12} people
```

results in 2 * 3 * 1 * 12 = 72 sample utterances (_BookingIntent help me order a {bookingType} for 3 people_ etc.).

### 3) Choose an output strategy

Go to the _UtteranceGenerator_ entry class and set the according _OUTPUT_WRITER_ to either _ConsoleOutputWriter_ (prints results to console)
or _FileOutputWriter_ (writes to a new file it stores in the _/src/main/resources/output_ folder).

### 4) Choose a formatter

Go to the _UtteranceGenerator_ entry class and set the according _FORMATTER_ to either _UtteranceListFormatter_ (prints results as a string list)
or _SkillBuilderFormatter_ (generates a full interaction model you can drag / paste in the Skill-Builder code-section).
You can also choose _SMAPIFormatter_ and give it an invocation name to generate SMAPI-compliant schema you can upload over the CLI.

Side note: SkillBuilderFormatter is deprecated as its output format aligned to SMAPIFormat in the Alexa console. From now on, please always use the SMAPIFormatter.

When you escaped the slot from resolution (see above) but still have a values-file in place, the formatter uses this
file to populate the contained slot values to a new custom slot type in the schema. That being said, you can even make use
of all the builtin slot types provided by Amazon in your grammar.

```xml
BookingIntent: {|please} help me {get|book|order} a {{booking:bookingType}} for {1-12} people in {{city:AMAZON.US_CITY}} at {{date:AMAZON.DATE}}
```

Optionally, you can now define your own slot names (booking, city, date) which do not need to be equal to the slot type name anymore.

### 5) Run and done

Run the _UtteranceGenerator_ from your commandline and watch out for a new file created in the _/src/main/resources/output_ folder.
If you decided for the _SkillBuilderFormatter_ take that json-file and paste / drag it to the code-section in the Skill-Builder. Done!

## Advanced

### Deduplication

This is more of a side-note as you don't have to set up anything to let the following happen:
When defining sample utterances in grammar-style, you likely gonna create overlapping utterances that
you'd like to avoid. The tool will automatically eliminate them. More than that, it throws an error
if those overlaps occurs across multiple intents (this is not allowed for skills in certification).
The same applies for slots and their synonyms.

### Integrate and extend builtin-intents of Amazon

If you'd like to add the builtin-intents you don't have to (but still can) provide any sample utterances.
Just add those intents with their exact name (see developer docs) to the grammar-file without giving it a sample utterance.

```xml
AMAZON.HelpIntent:
AMAZON.CancelIntent: Get me out of here
AMAZON.StopIntent: 
BookingIntent: {get|book|order} a {{booking:bookingType}}
```

### Leverage synonyms in slots

Synonyms for slots were introduced by Amazon most recently. It lets you create a set of synonyms all resolving to one
slot value. This reduces lots of complexity in your backend as you are released from handling a bunch of slot values all
meaning the same to your business logic (i.e. taxi, cab, car, ride -> car).

To leverage synonyms with this tool you simply apply the same syntax of placeholders from the grammar in your
slot-values-files. The tool then auto-populates all the values it finds in one line as synonyms to a new custom slot-type
and uses the first value per line as the value the synonyms resolve to.

Now you can also assign a custom id to each of your slot values and synonym collections (see below example where custom ids added for car and bike values).

Assume you have values named _bookingType.values_ file of the following:
```xml
car01: {car|ride|taxi|cab}
{hotel|room}
{table|restaurant|dinner}
bike01: bike
cinema
```

and a grammar-file like the above you end up with the following interaction-model:

```json
{
  "intents" : [ {
    "name" : "AMAZON.CancelIntent",
    "samples" : [ "Get me out of here" ],
    "slots" : [ ]
  }, {
    "name" : "AMAZON.HelpIntent",
    "samples" : [ ],
    "slots" : [ ]
  }, {
    "name" : "AMAZON.StopIntent",
    "samples" : [ ],
    "slots" : [ ]
  }, {
    "name" : "BookingIntent",
    "samples" : [ "book a {bookingType}", "get a {bookingType}", "order a {bookingType}" ],
    "slots" : [ {
      "name" : "bookingType",
      "type" : "bookingType",
      "samples" : [ ]
    } ]
  } ],
  "types" : [ {
    "name" : "bookingType",
    "values" : [ {
      "id" : "car01",
      "name" : {
        "value" : "car",
        "synonyms" : [ "ride", "taxi", "cab" ]
      }
    }, {
      "id" : null,
      "name" : {
        "value" : "hotel",
        "synonyms" : [ "room" ]
      }
    }, {
      "id" : null,
      "name" : {
        "value" : "table",
        "synonyms" : [ "restaurant", "dinner" ]
      }
    }, {
      "id" : "bike01",
      "name" : {
        "value" : "bike",
        "synonyms" : [ ]
      }
    }, {
      "id" : null,
      "name" : {
        "value" : "cinema",
        "synonyms" : [ ]
       }
    } ]
  } ]
}
```

### Use a slot in one sample utterance multiple times

Sounds obvious, and of course is also supported, the use of one and the same slot within one
sample utterance. The tool will just take that and generates all permutations - or in case it is escaped
from resolution - will ensure unique slot-names in your interaction model (this actually is a requirement for skill in certification) 

If you got this

```xml
BookingIntent: {bookingType} and {bookingType}
```

it results in N*N sample utterances where N is the number of values contained in _bookingType.values_ file.
However, if you do the following:

```xml
BookingIntent: {{bookingType}} and {{bookingType}}
```

as you learned - the slots will be resolved as slot types in the schema but not in the utterances themselves.
In your interaction model it will look like the following:

```json
{
  "intents" : [ ..., {
    "name" : "BookingIntent",
    "samples" : [ "{bookingType} and {bookingType_A}" ],
    "slots" : [ {
      "name" : "bookingType",
      "type" : "bookingType",
      "samples" : [ ]
    },{
      "name" : "bookingType_A",
      "type" : "bookingType",
      "samples" : [ ]
    }]
  } ],
  "types" : [ {
    "name" : "bookingType",
    "values" : [ ... ]
  } ]
}
```

You can also take care of unique slot names by assigning custom slot names.

```xml
BookingIntent: {{bookingA:bookingType}} and {{bookingB:bookingType}}
```