# Alexa Skill Utterance and Schema Generator (V2.0)

[![Join the chat at https://gitter.im/alexa-utterance-generator/Lobby](https://badges.gitter.im/alexa-utterance-generator/Lobby.svg)](https://gitter.im/alexa-utterance-generator/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven central](https://img.shields.io/badge/maven%20central-v2.0.0-orange.svg)](https://search.maven.org/artifact/io.klerch/alexa.utterances/2.0.0/jar)

This is a handy tool many developers already use to **create better interaction models** for their custom Alexa skills. The tool introduces
an **easy to read grammar for generating hundreds and thousands of variant sample utterances** and slot values with just a few written lines.
The resulting JSON file can be used to **upload the model to your Alexa skill right away** either via your web browser into Alexa skill builder interface or with help of [SMAPI](https://developer.amazon.com/de/alexa-skills-kit/smapi).

* [What's the point?](#0)
* [Quick start example](#1)
* [Run it](#2)
    * [CLI](#21)
    * [IDE](#22)
    * [Java code](#23)
    * [AWS Lambda function](#24)
    * [Web Browser](#25)
* [Reference](#3)
  * [**.grammar syntax** for utterance definitions](#31)
    * [Invocation name](#311)
    * [Alternate phrasing (inline)](#312)
    * [Alternate phrasing by reference](#313)
    * [Slots](#314)
      * [Builtin Slots](#315)
      * [Named slots](#316)
      * [Alternate slots](#317)
      * [Duplicate slots](#318)
    * [Duplicate sample utterances](#319)
  * [Defining value collections](#32)
    * [Slot types](#321)
    * [Synonyms and Slot Ids](#322)
  * [Comments](#33)

<a name="0"></a>
## **0. What's the point?**

Using an utterance generator is a best practice. Better consistency and wider coverage of sample utterances improve the natural language understanding of Alexa
and it reduces the risk of incorrect intent mapping and slot filling in your Alexa skills. It is almost impossible to achieve the same with manually writing down utterances line by line.
Secondly, rather than defining interaction models in JSON, grammar files provide an easy to understand syntax. Just think of outsourcing the interface design to another team or external agency - you'll give creative minds an option to contribute to your skills without knowing JSON or Alexa skill-specific elements. Needless to say it will simplify localization of your interaction model where you would want to include a non-tech translator.

* Provide more consistency and variety in your sample utterance collection by defining their grammar rather than writing down one by one
* Don't care about duplicates and overlaps in utterances and slot values as this tool eliminates them
* Avoid common pitfalls during certification of Alexa skills caused by an incompliant interaction schema
* Maintain all assets of your skill interaction model in human-readable files in your source repository
* Auto generate interaction models
* Reuse value lists and utterance collections

<a name="1"></a>
## **1. Quick start example**

You can find a full example in the [_resources_](/src/main/resources) folder of this project. The following is an excerpt and
introduces just the basic concepts in order to get started quickly.

```xml
Invocation: travel booking
BookingIntent: {get|book|order} {me|us} {|a} {{bookingItem}} {at|on|for} {{date:AMAZON.DATE|time:AMAZON.TIME}}

{bookingItem}:
car,ride,taxi,cab
hotel,room
table,restaurant,dinner
```

results in 3 * 2 * 2 * 1 * 3 * 2 = 72 sample utterances (_get me a {bookingItem} for {date}_, _order us {bookingItem} for {time}_ etc.)
At the same time a slot type _bookingItem_ is created with three values (+synonyms) and is referenced in the _BookingIntent_.

You will put all the above grammar, slot value lists and intent mappings into one _*.grammar_ text file (see above
or [here](/src/main/resources/utterances/booking.grammar)) and can further create _*.values_ text files
(see below or [here](/src/main/resources/slots/bookingItem.values)) to externalize slot value listings for better reuse if you want.

```xml
// this format is necessary if you want custom slot ids
{car01:car|ride|taxi|cab}
// also possible with easier syntax but no option to set custom slot id (hotel will get id and first value)
hotel, room
{table|restaurant|dinner}
{bike01:bike}
cinema
```

Write slot values down line by line and optionally assign synonyms (e.g. _room_ for _hotel_) and a custom identifier (e.g. _car01_). Running _alexa-generate.jar_ from your CLI or use _UtteranceGenerator_ console application from within your Java IDE results in the following
(see below or [here](/src/main/resources/output/travel_booking.json)):

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
              "name":"bookingItem",
              "type":"bookingItem"
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
              "id": hotel,
              "name": {
                "value":"hotel",
                "synonyms":[ "room" ]
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

As a working example is already in place just go to the _UtteranceGenerator_ class in your Java IDE and execute.
The generator will pick up the referenced _booking.grammar_ file and associated _*.values_ files and generates the interaction
schema which you will then find in the _[/src/main/resources/output/](/src/main/resources/output/)_ folder.

<a name="2"></a>
## **2. Run it**

Generating Alexa skill interaction schemas from your self-created _*.grammar_ files (and optionally _*.values_ files) is possible in different ways.

<a name="21"></a>
### **2.1 CLI**

The easiest way to do it is to use the command-line interface (CLI) by running the _alexa-generate.jar_ file. Simply build the project or
[download the JAR file](/bin/alexa-generate.jar). In your command-line you can now run:

```bash
java -jar alexa-generate.jar path/to/my.grammar [path/to/output.json] [-v|--values path/to/values] [-d|--dry-run] [-p|--plain] [-r|--repl]
```
__-h, --help__ to get details and instructions.

__-d, --dry-run__ will just print the output to the console rather than writing it to a file.

__-p, --plain__ won't print the output as a JSON skill schema but rather chooses an easy to read format for validating the generated samples.

__-v, --values__ followed by a PATH to the values files location. If not set the values files will be looked up in the folder of the references *.grammar file.

__-r, --repl__ enters the _[REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop)_ inline mode. You can now enter grammar specification line by line in your console. Complete your input by typing _generate_ (see also below)

Start with _java -jar alexa-generate.jar booking.grammar_ that will pick up the referenced grammar file and it generates and stores the resulting
interaction schema as a JSON file in the same folder as the grammar file. Without even giving this command a path to values-files the generator
will look up _*.values_ files in the folder of _booking.grammar_ in case it cannot resolve a placeholder from what is specified in the grammar file.
You can change the folder location where the generator looks up those values files simply by giving it a path with the -v option. Also customize
the location and file name of the resulting JSON interaction model if you want.

**REPL**

Yeah, you are not actually required to create a *.grammar file in order to validate some schema outout. Simply append _-r_ or __--repl__ to
your command and you will enter REPL mode - which stands for _[read-eval-print loop](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop)_.
It lets you put in grammar specification line by line from your command. Complete by typing _generate!_ (with exclamation mark!)
and you will be given the resulting output in the console. As this mode will only take input from console it will ignore
any *.grammar file references. However, you can still use _-p, --plain_ and _-v, --values_ so you can make use
of your existing values definitions inside *.values files in your inline grammar specification. There is no need to explicitly set _-d, --dry-mode_
as in REPL mode it automatically prints output to the console anyway. It will also let you save the output to file after reviewing the output in the console.

**Organize your projects**

We recommend to structure your skill project folders as follows:

```
/my-alexa-skills/
│   alexa-generate.jar
│
└───/booking-skill/
│   └───/models/
│       │   en-US.grammar
│       │   en-US.json
│       │   ...
│       └───/slots/
│       │   │   bookingItem.values
│       │   │   ...
│
└───/another-skill/
    │   ...
```

Navigate to your alexa-skills folder and run

```bash
java -jar alexa-generate.jar booking-skill/models/en-US.grammar booking-skill/models/en-US.json -v booking-skill/models/slots
```

The folder structure equals to what the Alexa Skills Kit SDKs set up for you. After storing the generated model in the _models_ folder
you can use _[ASK CLI](https://developer.amazon.com/docs/smapi/ask-cli-command-reference.html)_ to deploy your Alexa skills with an updated model right away.

<a name="22"></a>
### **2.2 IDE**

Use a Java IDE like Eclipse or IntelliJ Idea to open this project right after you pulled it from Github. You need to store your _*.grammar_ and _*.values_ files
in their respective folders under _[/src/main/resources](/src/main/resources/)_. The JSON schema will be saved in the _[/src/main/resources/output](/src/main/resources/output)_ folder.

1) In your IDE open _[UtteranceGenerator.java](/src/main/java/io/klerch/alexa/utterances/UtteranceGenerator.java)_
2) Put your _*.grammar_ file in the _[utterances](/src/main/resources/utterances)_ folder and if you have your _*.values_ files in the _[slots](/src/main/resources/slots)_ folder
3) Set the _GRAMMAR_FILE_KEY_IN_UTTERANCES_FOLDER_ variable to the file key of the targeted grammar file.
4) Optionally change the configuration variables. Code comments give all the instructions.
5) Run or debug _UtteranceGenerator_ from your IDE

<a name="23"></a>
### **2.3 Java Code**

The _main_ method in _[UtteranceGenerator.java](/src/main/java/io/klerch/alexa/utterances/UtteranceGenerator.java)_ demonstrates how to initialize a _Generator_ object and set it up before calling the magical _generate()_ method. This project is available
in [Maven central](https://search.maven.org/artifact/io.klerch/alexa.utterances/2.0.0/jar) as well and can be added as a Maven dependency to the _pom.xml_ of your own Java project.

[![Maven central](https://img.shields.io/badge/maven%20central-v2.0.0-orange.svg)](https://search.maven.org/artifact/io.klerch/alexa.utterances/2.0.0/jar)
```xml
<dependency>
  <groupId>io.klerch</groupId>
  <artifactId>alexa.utterances</artifactId>
  <version>2.0.0</version>
</dependency>
```

In the _[docs](/docs)_ folder you can find the API docs for more information.

<a name="24"></a>
### **2.4 AWS Lambda function**

If you´d like to host this project as an AWS lambda function, no problem. Use _[lambda/Handler.java](src/main/java/io/klerch/alexa/utterances/lambda/Handler.java)_ and
hand in grammar specification as an array of strings (JSON field in the request should be _lines_.

1) Create a new Lambda function in AWS developer console (Runtime: Java8)
2) Upload [download the JAR file](/bin/alexa-generate.jar) and set the handler to _io.klerch.alexa.utterances.lambda.Handler_. Increase timeout setting.
3) Call this Lambda function by giving it your grammar specification line by line as a JSON payload. Try it out by creating a test event in AWS console. as follows

```json
{ "lines": [
    "Invocation: travel booking",
    "AMAZON.StopIntent:",
    "RainForecastIntent: will {it|there be} rain {|today}"
  ]
}
```

<a name="25"></a>
### **2.5 Web browser**

A web interface it currently in the works and will be releases soon. It will be the most convenient way of writing grammar specification
and generating interaction models right in your web browser. Moreover, there will be an option to save your grammar online and share with others
to collaborate on it easily.


<a name="3"></a>
## **3. Reference**

<a name="31"></a>
### **3.1 .grammar syntax for sample utterance definitions**

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

<a name="311"></a>
### 3.1.1 Invocation name

Optionally, you can set the invocation name for your Alexa skill in the grammar file as well. It is part of the generated interaction schema and is required unless you give it as constructor value to the _SMAPIFormatter_ in code (see below). Defining the invocation is easy and works the same as with intents. _Invocation_ is a reserved word in grammar files and is not processed as an intent definition.

```xml
Invocation: weather info
RainForecastIntent: will {it|there be} rain {|today}
```

<a name="312"></a>
### 3.1.2 Alternate phrasing (inline)

We´ve seen this already in above examples and it´s one of the biggest strengths of grammar definition. Inline you can define different wording for one and the same thing, surrounded by **single curly brackets** and **separated by pipes (|) symbols**. A trailing or leading pipe within the curly brackets also adds a blank value as an option.

```xml
RainForecastIntent: will {it|there be} rain {|today}
```

This results in _"will it rain"_, _"will there be rain"_, _"will it rain today"_ and _"will there be rain today"_. Pretty simple, right?

<a name="313"></a>
### 3.1.3 Alternate phrasing by reference

If you got very long enumerations of alternate phrasings (like a long list of synonym verbs) and those repeat in many lines you may not want to have it inline in your grammar utterances. Therefore, you can store these values in a .values file, store it in the [/src/main/resources/slots/](/src/main/resources/slots/) folder and refer to it by its file key within curly brackets. Assume you have a file [bookingAction.values](/src/main/resources/slots/bookingAction.values) that contains three lines with _"book"_, _"get"_ and _"order"_ you can now do the following:

```xml
BookHotelIntent: please {bookingAction} me a room
```

The generator will resolve this placeholder whenever it sees a file in the slots folder having the same name (e.g. _bookingAction.values_) as the placeholder reference (e.g. _bookingAction_). The above example results in _"please book me a room"_, _"please get me a room"_, _"please order me a room"_.

<a name="314"></a>
### 3.1.4 Slots

If you are familiar with slots in Alexa skills you know there is a requirement to leave the placeholder within a sample utterance in order to extract certain information in your skill to process it. In order to prevend this generator from resolving the placeholder you need to surround it by **double curly brackets**.

```xml
BookHotelIntent: please {bookingAction} me a {{bookingItem}}
```

This still requires a _*.values_ file called _bookingItem.values_ in the _slots_ folder but now the generator will leave it in the resulting sample utterances as a placeholder (slot). The result from above example now is: _"please book me a {bookingItem}"_, _"please get me a {bookingItem}"_, _"please order me a {bookingItem}"_. At the same time the generator will create a slot type called _bookingItem_ in your schema and adds all the values it found in the _bookingItem.values_ file.

<a name="315"></a>
### 3.1.5 Builtin Slots

The same works with AMAZON-builtin slot types with one exception: the generator will not create a custom slot type for it in your schema as this is not required in Alexa skills. The generator will slightly rename the slot name as dots are not allowed in slot names. Please note: you can still extend builtin slot types with your own values by creating and storing a file in the _slots_ folders which is named as the builtin slot type (e.g. _[AMAZON.US_CITY.values](/src/main/resources/slots/AMAZON.US_CITY.values)_).

```xml
BookHotelIntent: please {bookingAction} me a {{bookingItem}} in {{AMAZON.US_CITY}}
```

The result from above example now is: _"please book me a {bookingItem} in {AMAZON_US_CITY}"_, _"please get me a {bookingItem} in {AMAZON_US_CITY}"_, _"please order me a {bookingItem} in {AMAZON_US_CITY}"_.

<a name="316"></a>
### 3.1.6 Named slots

If you don't want to have the file key be your slot name in the sample utterances you can also define your own names by preceding it to the slot type reference (file key) and separate with a colon.

```xml
BookHotelIntent: please {bookingAction} me a {{item:bookingItem}} in {{city:AMAZON.US_CITY}}
```

results in _"please book me a {item} in {city}"_, _"please get me a {item} in {city}"_, _"please order me a {item} in {city}"_.

<a name="317"></a>
### 3.1.7 Alternate slots

You can apply the concept of alternate phrasing to slot placeholders as well. Making a slot optional in you sample utterance is done by adding a preceding or trailing pipe symbol to the reference. You can even list more than just one slot type reference (file key + optionally custom slot name).

```xml
BookHotelIntent: please {bookingAction} me a {{|item:bookingItem}} in {{cityUS:AMAZON.US_CITY|cityEU:AMAZON.EUROPE_CITY}}
```

will also result in things like _"please book me a in {cityUS}"_, _"please get me a {item} in {cityUS}"_, _"please order me a in {cityEU}"_ and _"please order me a {item} in {cityEU}"_.

<a name="318"></a>
### 3.1.8 Duplicate slots

In case you have more than one occurance of one and the same slot type reference in one sample utterance and you did not assign individual custom slot names to them (like above _cityUS_ and _cityEU_) the generator will take care of it. It's not allowed to have more than one slot with the same name in one utterance. The generator will recognize this pattern and will rename the second, third, ... occurance of the same name by adding suffix A, B, C ... to them.

```xml
BookHotelIntent: please {bookingAction} me a {{|item:bookingItem}} in {{city:AMAZON.US_CITY|city:AMAZON.EUROPE_CITY}}
```

In this example _US_CITY_ and _EU_CITY_ got the same slot name _city_. The generator will leave the first occurance as is (_city_) while renaming the second occurance for _EU_CITY_ to _cityA_.

This results in things like _"please book me a {item} in {city}"_, _"please get me a {item} in {cityA}"_.

<a name="319"></a>
### 3.1.9 Duplicate sample utterances

With grammar definitions you will very likely create overlaps and duplicate sample utterances. The generator will take care of it and removes duplicate sample utterances within one and the same intent. Just in case you got duplicate overlaps spanning over different intents the generator will throw an error. The tool cannot decide on your behalf which one is to remove and you need to resolve yourself.

<a name="32"></a>
### **3.2 Defining value collections**

<a name="321"></a>
### 3.2.1 Slot types

When you're using _{{slots}}_ and _{placeholders}_ in your grammar definition there needs to be place where to define what's in there. You can either do it within your *.grammar files or use separate *.values files listing all the values.

***Specified in .grammar file**

First let's look at an example where slot and placeholder values are specified in grammar files.

```xml
Invocation: travel booking
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} in {{city:AMAZON.US_CITY}}

{bookingAction}: get, book, order

{bookingItem}:
car
hotel
table

{AMAZON.US_CITY}: big apple
```

_bookingAction_ is a placeholder for alternate phrasing which is further defined below. There's a simplified CSV format for defining alternative phrasing. "_get, book, order_" is equal to _{get|book|order}. You can add as many lines as you want to resolve the _{bookingAction}_ in your utterances even with more than just one alternate phrasing.

_bookingItem_ is a slot and values are defined the same. Indeed, a values definition starting with {placeholder:} can be used as a {placeholder} and {{slot}} in sample utterances. Please note that for slots individual values ending up in a slot type in your schema need to be separated by linebreak. Assume you would use {{bookingAction}} as a slot in your utterance. The generator treats separated values in one line as alternatives - for slots this means getting a new slot type called _bookingAction_ having only one slot value _get_ with _book_ and _get_ as synonyms added to it.

***Specified in .values files**

Slot value collections and alternate phrasing can also be stored in separate _*.values_ files. Those files need to be named like the placeholder name (e.g. _bookingAction.values_) and for slots the slot type name (e.g. _bookingItem.values_ or _AMAZON.US_CITY.values_). Syntax for defining values in these files is the same as described above.

Here's a very simple example for the [bookingAction.values](/src/main/resources/slots/bookingAction.values), [bookingItem.values](/src/main/resources/slots/bookingItem.values) and [AMAZON.US_CITY.values](/src/main/resources/slots/AMAZON.US_CITY.values)

The generator resolves the placeholder _{bookingAction}_ by generating all permutations with _"book"_, _"get"_ and _"order"_ (e.g. _"please book me a {item} in {city}"_). In case the placeholder is representing a slot (in double curly brackets) the generator will take the values and adds it to a custom slot type in the output schema.

Needless to say you can do both at the same time: define values in separated files and within your *.grammar file. If you define _{bookingAction}: get, book, order_ in your grammar the generator won't consider _bookingAction.values_ anymore as it always prioritizes the first.

<a name="322"></a>
### 3.2.2 Synonyms and Slot ids

If you would like to make use of [synonyms](https://developer.amazon.com/de/docs/custom-skills/define-synonyms-and-ids-for-slot-type-values-entity-resolution.html) in slots you can also use alternate phrasing syntax already introduced for the sample utterance. Here's the [bookingItems.values](/src/main/resources/slots/bookingAction.values) file for the above example. Please note, you can do the same within your grammar specification where you would prepend {bookingItems}: to the below in your grammar-file.

```xml
{car01:car|ride|taxi|cab}
hotel, room
{table|restaurant|dinner}
{bike01:bike}
cinema
```

We see several things here which all work side by side. First of all we created synonyms for slot value _car_ (ride, taxi, cab), _hotel_ (room) and _table_ (restaurant, dinner), again by using alternate phrasing syntax. Secondly, for car and bike we defined custom slot ids (curly brackets get mandatory here, so don't forget them). In particular, this is important for slot values having synonyms as you need to check for just this id in your code to handle all the synonyms. Note that if you do not assign a custom slot id the generator will set the first value as the id (e.g. _hotel_, _table_ and _cinema_).

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
          "id" : cinema,
          "name" : {
            "value" : "cinema",
            "synonyms" : [ ]
          }
        } ]
      }, {
        "name" : "AMAZON.US_CITY",
        "values" : [ {
          "id" : "new york",
          "name" : {
            "value" : "new york",
            "synonyms" : [ "big apple" ]
          }
        } ]
      } ],
      "invocationName" : "travel booking"
    }
  }
}
```

The above example also shows an extension to a builtin slot type for _AMAZON.US_CITY_ which is coming from an additional _*.values_ file called _[AMAZON.US_CITY.values](/src/main/resources/slots/AMAZON.US_CITY.values)_

<a name="33"></a>
### 3.3 Comments

It is often useful to leave comments in your artifacts to document and explain what was defined as an intent, sample utterance or slot value. You can make use of comments in _*.grammar_ and _*.values_ file by prepending a line with double forward slashes (_//_). It both works inline and in new line.

```xml
// invocation name
Invocation: travel booking    // comment can also be inline

// custom intents
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} {at|on|for} {{date:AMAZON.DATE}}
BookHotelIntent: {|please} {bookingAction} me a {{item:bookingItem}} in {{city:AMAZON.US_CITY}}
```

<a name="4"></a>
### 4. Feature roadmap

The next big thing I am currently working on is also supporting dialog intents and slots in the grammar specification. It is currently the missing piece of building complete interaction models.
I am also busy setting up a web interface so you can type grammar specification and review, save and share the output in your web browser. Stay tuned!

I am always happy to receive pull requests or any kind of constructive feedback and feature requests.
You can reach me on
[![Join the chat at https://gitter.im/alexa-utterance-generator/Lobby](https://badges.gitter.im/alexa-utterance-generator/Lobby.svg)](https://gitter.im/alexa-utterance-generator/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)