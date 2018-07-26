# Alexa Skill Utterance and Schema Generator (Improved)

This is a handy tool many developers already use to **create better interaction models** for their custom Alexa skills. The tool introduces
an **easy to read grammar for generating hundreds and thousands of variant sample utterances** and slot values with just a few written lines.
The resulting JSON file can be used to **upload the model to your Alexa skill right away** either via your web browser into Alexa skill builder interface or with help of [SMAPI](https://developer.amazon.com/de/alexa-skills-kit/smapi).

Using an utterance generator is a best practice. Better consistency and wider coverage of sample utterances improves the natural language understanding of Alexa
and it avoids potential friction and false positives in your Alexa skills. It is almost impossive to achieve the same with manually writing down utterances line by line.
Secondly, rather than defining interaction models in JSON, grammar files provide an easy to understand syntax. Just think of outsourcing the interface design to another team or external agency - you'll give creative minds an option to contribute to your skills without knowing JSON or Alexa skill-specific elements. Needless to say it will simplify localization of your interaction model where you would want to include a non-tech translator.

* [Quick start example](#1)
* [Benefits](#2)
* [Reference](#3)
  * [**Execute](#31)
  * [**.grammar syntax** for utterance definitions](#32)
    * [Invocation name](#321)
    * [Alternate phrasing (inline)](#322)
    * [Alternate phrasing by reference](#323)
    * [Slots](#324)
      * [Builtin Slots](#325)
      * [Named slots](#326)
      * [Alternate slots](#327)
      * [Duplicate slots](#328)
    * [Duplicate sample utterances](#329)
  * [**.values syntax** for slot definitions](#33)
    * [Slot types](#331)
    * [Synonyms and Slot Ids](#332)
  * [Comments](#34)


## **1. Quick start example** <a name="1"></a>

You can find a full example in the [_resources_](/src/main/resources) folder of this project. The following is an excerpt and introduces just the basic concepts in order to get started quickly.

```xml
Invocation: travel booking
BookingIntent: {get|book|order} {me|us} {|a} {{bookingItem}} {at|on|for} {{date:AMAZON.DATE|time:AMAZON.TIME}}
```

results in 3 * 2 * 2 * 1 * 3 * 2 = 72 sample utterances (_get me a {bookingItem} for {date}_, _order us {bookingItem} for {time}_ etc.)

You will put all your sample utterance grammar and intent mappings into one _*.grammar_ text file (see above or [here](/src/main/resources/utterances/booking.grammar)) and can further creage _*.values_ text files (see below or [here](/src/main/resources/slots/bookingItem.values)) to list your slot values like for _{bookingItem}_ in above example.

```xml
car01: {car|ride|taxi|cab}
{hotel|room}
{table|restaurant|dinner}
bike01: bike
cinema
```

Write slot values down line by line and optionally assign synonyms (e.g. _room_ for _hotel_) and a custom identifier (e.g. _car01_). Running _UtteranceGenerator_ console application from within your IDE or command line results in the following (see below or [here](/src/main/resources/output/SMAPIFormatter_booking.json)):

```xml
{
  "interactionModel":{
    "languageModel":{
      "intents":[ {
          "name":"BookingIntent",
          "samples":[
            "get me {bookingItem} for {date}",
            "get me {bookingItem} for {time}",
            "get me a {bookingItem} for {date}",
            "get me a {bookingItem} for {time}",
            "book me {bookingItem} for {date}",
            ...
          ],
          "slots":[ {
              "name":"bookingType",
              "type":"bookingType"
            },
            {
              "name":"date",
              "type":"AMAZON.DATE"
            },
            {
              "name":"time",
              "type":"AMAZON.TIME"
            } ]
        }
      ],
      "types":[
        {
          "name":"bookingItem",
          "values":[
            {
              "id": "car01",
              "name":{
                "value":"car",
                "synonyms":[ "ride", "taxi", "cab" ]
              }
            },
            ...
            {
              "id": null,
              "name": {
                "value":"cinema",
                "synonyms":[]
              }
            }
          ]
        }
      ],
      "invocationName":"travel booking"
    }
  }
}
```

As a working example is already in place just go to the _UtteranceGenerator_ class in your Java IDE and execute. The generator will pick up the referenced booking.grammar file and associated *.values files and generates the interaction schema which you will then find in the [/src/main/resources/output/](/src/main/resources/output/) folder. For refence, this is how it should look like: [SMAPIFormatter_booking.json](/src/main/resources/output/SMAPIFormatter_booking.json).

## **2. Benefits** <a name="2"></a>
* Provide more consistency and variety in your sample utterance collection by defining their grammar rather than writing down one by one
* Don't care about duplicates and overlaps in utterances and slot values as this tool eliminates them
* Avoid common pitfalls during certification of Alexa skills caused by an incompliant interaction schema
* Maintain all assets of your skill interaction model in human-readable files in your source repository
* Auto generate interaction models
* Reuse value lists and utterance collections

## **3. Reference** <a name="3"></a>

### **3.1 Execute** <a name="31"></a>

**From your IDE** Once you created your grammar file and optionally values files and stored it in the respective resource folders (you`ll will learn in the next chapters) open the _UtteranceGenerator_ class in your Java IDE.
- set the _utteranceFileKey_ variable to the file key of your grammar file. If you got _booking.grammar_ the value of this variable should be _booking_
- choose one of the two _OutputWriter_. The generator either writes the resulting schema to the output folder as a file or to the console
- choose one of the two _Formatters_. Most likely you need the _SMAPIFormatter_ which spits out a full-fledged interaction model for your Alexa skill. For reviewing purpose it may be helpful to use _UtteranceListFormatter_ which simply writes down the generated sample utterances line by line.
- run or debug the _UtteranceGenerator_ class from within your IDE and watch out for a new JSON file appearing in the [/src/main/resources/output/](/src/main/resources/output/) folder. This is your new interaction model.

You can drag and drop the JSON file into the _JSON Editor_ in the _Build_ section of Alexa skill developer console or you can store the file in your skill project to deploy it along with your skill code via Amazon´s [Skill Management API](https://developer.amazon.com/de/alexa-skills-kit/smapi) aka SMAPI.

**From command line** : there is also a CLI interface for the generator so you could also run it from your command line rather than using an IDE. I did not really test it so please use it with caution and patience :) Once this is working properly and reliably I will follow up with documentation.

### **3.2 .grammar syntax for sample utterance definitions** <a name="32"></a>

All your sample utterances will be defined in one text files with file ending _*.grammar_ which needs to be stored in the [/src/main/resources/utterances/](/src/main/resources/utterances/) folder in this project. The format of these files is very easy to read also for non-techies like Designers who likely own user experience in your project. You are basically defining all intents for your Alexa skill (including the[AMAZON-builtin intents](https://developer.amazon.com/de/docs/custom-skills/standard-built-in-intents.html)) followed by a colon and assigned sample utterances in (optionally) grammar style. You only need to reference the intent name once as all following lines up to the next intent definition are assigned to that last defined intent.

```xml
AMAZON.StopIntent:
AMAZON.CancelIntent:
AMAZON.HelpIntent: please guide {me|us}
WeatherForecastIntent: what is the weather
tell me the weather
RainForecastIntent: will {it|there be} rain {|today}
```

From above example you can see that builtin intents do not require a sample utterance as they Amazon covered that part. However, you can still extend with your own samples. _WeatherForecastIntent_ got two sample utterances not using any grammar whereas _RainForecastIntent_ got one grammar-style sample utterance resulting in 2 (it, there be) * 2 (blank, today) = 4 permutations.

### 3.2.1 Invocation name <a name="321"></a>

Optionally, you can set the invocation name for your Alexa skill in the grammar file as well. It is part of the generated interaction schema and is required unless you give it as constructor value to the _SMAPIFormatter_ in code (see below). Defining the invocation is easy and works the same as with intents. _Invocation_ is a reserved word in grammar files and is not processed as an intent definition.

```xml
Invocation: weather info
RainForecastIntent: will {it|there be} rain {|today}
```

### 3.2.2 Alternate phrasing (inline) <a name="322"></a>

We´ve seen this already in above examples and it´s one of the biggest strengths of grammar definition. Inline you can define different wording for one and the same thing, surrounded by **single curly brackets** and **separated by pipes (|) symbols**. A trailing or leading pipe within the curly brackets also adds a blank value as an option.

```xml
RainForecastIntent: will {it|there be} rain {|today}
```

This results in _"will it rain"_, _"will there be rain"_, _"will it rain today"_ and _"will there be rain today"_. Pretty simple, right?

### 3.2.3 Alternate phrasing by reference <a name="323"></a>

If you got very long enumerations of alternate phrasings (like a long list of synonym verbs) and those repeat in many lines you may not want to have it inline in your grammar utterances. Therefore, you can store these values in a .values file, store it in the [/src/main/resources/slots/](/src/main/resources/slots/) folder and refer to it by its file key within curly brackets. Assume you have a file [bookingAction.values](/src/main/resources/slots/bookingAction.values) that contains three lines with _"book"_, _"get"_ and _"order"_ you can now do the following:

```xml
BookHotelIntent: please {bookingAction} me a room
```

The generator will resolve this placeholder whenever it sees a file in the slots folder having the same name (e.g. _bookingAction.values_) as the placeholder reference (e.g. _bookingAction_). The above example results in _"please book me a room"_, _"please get me a room"_, _"please order me a room"_.

### 3.2.4 Slots <a name="324"></a>

If you are familiar with slots in Alexa skills you know there is a requirement to leave the placeholder within a sample utterance in order to extract certain information in your skill to process it. In order to prevend this generator from resolving the placeholder you need to surround it by **double curly brackets**.

```xml
BookHotelIntent: please {bookingAction} me a {{bookingItem}}
```

This still requires a _*.values_ file called _bookingItem.values_ in the _slots_ folder but now the generator will leave it in the resulting sample utterances as a placeholder (slot). The result from above example now is: _"please book me a {bookingItem}"_, _"please get me a {bookingItem}"_, _"please order me a {bookingItem}"_. At the same time the generator will create a slot type called _bookingItem_ in your schema and adds all the values it found in the _bookingItem.values_ file.

### 3.2.5 Builtin Slots <a name="325"></a>

The same works with AMAZON-builtin slot types with one exception: the generator will not create a custom slot type for it in your schema as this is not required in Alexa skills. The generator will slightly rename the slot name as dots are not allowed in slot names. Please note: you can still extend builtin slot types with your own values by creating and storing a file in the _slots_ folders which is named as the builtin slot type (e.g. _[AMAZON.US_CITY.values](/src/main/resources/slots/AMAZON.US_CITY.values)_).

```xml
BookHotelIntent: please {bookingAction} me a {{bookingItem}} in {{AMAZON.US_CITY}}
```

The result from above example now is: _"please book me a {bookingItem} in {AMAZON_US_CITY}"_, _"please get me a {bookingItem} in {AMAZON_US_CITY}"_, _"please order me a {bookingItem} in {AMAZON_US_CITY}"_.

### 3.2.6 Named slots <a name="326"></a>

If you don't want to have the file key be your slot name in the sample utterances you can also define your own names by preceding it to the slot type reference (file key) and separate with a colon.

```xml
BookHotelIntent: please {bookingAction} me a {{item:bookingItem}} in {{city:AMAZON.US_CITY}}
```

results in _"please book me a {item} in {city}"_, _"please get me a {item} in {city}"_, _"please order me a {item} in {city}"_.

### 3.2.7 Alternate slots <a name="327"></a>

You can apply the concept of alternate phrasing to slot placeholders as well. Making a slot optional in you sample utterance is done by adding a preceding or trailing pipe symbol to the reference. You can even list more than just one slot type reference (file key + optionally custom slot name).

```xml
BookHotelIntent: please {bookingAction} me a {{|item:bookingItem}} in {{cityUS:AMAZON.US_CITY|cityEU:AMAZON.EUROPE_CITY}}
```

will also result in things like _"please book me a in {cityUS}"_, _"please get me a {item} in {cityUS}"_, _"please order me a in {cityEU}"_ and _"please order me a {item} in {cityEU}"_.

### 3.2.8 Duplicate slots <a name="328"></a>

In case you have more than one occurance of one and the same slot type reference in one sample utterance and you did not assign individual custom slot names to them (like above _cityUS_ and _cityEU_) the generator will take care of it. It's not allowed to have more than one slot with the same name in one utterance. The generator will recognize this pattern and will rename the second, third, ... occurance of the same name by adding suffix A, B, C ... to them.

```xml
BookHotelIntent: please {bookingAction} me a {{|item:bookingItem}} in {{city:AMAZON.US_CITY|city:AMAZON.EUROPE_CITY}}
```

In this example _US_CITY_ and _EU_CITY_ got the same slot name _city_. The generator will leave the first occurance as is (_city_) while renaming the second occurance for _EU_CITY_ to _cityA_.

This results in things like _"please book me a {item} in {city}"_, _"please get me a {item} in {cityA}"_.

### 3.2.9 Duplicate sample utterances <a name="329"></a>

With grammar definitions you will very likely create overlaps and duplicate sample utterances. The generator will take care of it and removes duplicate sample utterances within one and the same intent. Just in case you got duplicate overlaps spanning over different intents the generator will throw an error. The tool cannot decide on your behalf which one is to remove and you need to resolve yourself.

### **3.3 .values syntax** for slot definitions** <a name="33"></a>

### 3.3.1 Slot types <a name="331"></a>

Slot value collections and optionally also alternate phrasing is stored in separate _*.values_ files in [/src/main/resources/slots/](/src/main/resources/slots/) folder of this project. They will be referenced by using they file names as placeholders within your sample utterances in the _*.grammar_ files. (e.g. _{{mySlot}}_ resolves to values stored in _mySlot.values_ file).

```xml
Invocation: travel booking
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} {at|on|for} {{date:AMAZON.DATE}}
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} in {{city:AMAZON.US_CITY}}
```

Slot values in these files are listed line by line. Here's a very simple example for the [bookingAction.values](/src/main/resources/slots/bookingAction.values) used in

```xml
book
get
order
```

The generator resolves the placeholder _{bookingAction}_ by generating all permutations with _"book"_, _"get"_ and _"order"_ (e.g. _"please book me a {item} in {city}"_). In case the placeholder is representing a slot (in double curly brackets) the generator will take the values and adds it to a custom slot type in the output schema.

### 3.3.2 Synonyms and Slot ids <a name="332"></a>

If you would like to make use of [synonyms](https://developer.amazon.com/de/docs/custom-skills/define-synonyms-and-ids-for-slot-type-values-entity-resolution.html) in slots you can also use alternate phrasing syntax already introduced for the sample utterance. Here's the [bookingItems.values](/src/main/resources/slots/bookingAction.values) file for the above example.

```xml
car01: {car|ride|taxi|cab}
{hotel|room}
{table|restaurant|dinner}
bike01: bike
cinema
```

We see several things here which all work side by side. First of all we are created synonyms for slot value _car_ (ride, taxi, cab), _hotel_ (room) and _table_ (restaurant, dinner), again by using alternate phrasing syntax with curly brackets and values separated by pipe symbols. Secondly, for car and bike we defined custom slot ids. In particular, this is important for slot values having synonyms as you need to check for just this id in your code to handle all the synonyms. Custom slot ids are assigned in the same way you assigned intent names to your sample utterances by using a colon.

The generator will now take these and creates a custom slot type in your interaction schema.

```xml
{
  "interactionModel" : {
    "languageModel" : {
      "intents" : [ {
        "name" : "BookingIntent",
        "samples" : [
          "book a {item} at {date}",
          "book a {item} in {city}",
          ...
        ],
        "slots" : [ {
          "name" : "item",
          "type" : "bookingItem",
          "samples" : [ ]
        }, {
          "name" : "date",
          "type" : "AMAZON.DATE",
          "samples" : [ ]
        }, {
          "name" : "city",
          "type" : "AMAZON.US_CITY",
          "samples" : [ ]
        }
        ...
        ]
      }
      } ],
      "types" : [ {
        "name" : "bookingItem",
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
      }, {
        "name" : "AMAZON.US_CITY",
        "values" : [ {
          "id" : null,
          "name" : {
            "value" : "big apple",
            "synonyms" : [ ]
          }
        } ]
      } ],
      "invocationName" : "travel booking"
    }
  }
}
```

The above example also shows an extension to a builtin slot type for _AMAZON.US_CITY_ which is coming from an additional _*.values_ file called _[AMAZON.US_CITY.values](/src/main/resources/slots/AMAZON.US_CITY.values)_

### 3.4 Comments <a name="34"></a>

It is often useful to leave comments in your artifacts to document and explain what was defined as an intent, sample utterance or slot value. You can make use of comments in _*.grammar_ and _*.values_ file by prepending a line with double forward slashes (_//_). It both works inline and in new line.

```xml
// invocation name
Invocation: travel booking    // comment can also be inline

// custom intents
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} {at|on|for} {{date:AMAZON.DATE}}
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} in {{city:AMAZON.US_CITY}}
```