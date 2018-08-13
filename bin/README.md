# Alexa Skill Utterance and Schema Generator (V2.0)

[![Join the chat at https://gitter.im/alexa-utterance-generator/Lobby](https://badges.gitter.im/alexa-utterance-generator/Lobby.svg)](https://gitter.im/alexa-utterance-generator/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven central](https://img.shields.io/badge/maven%20central-v2.0.0-orange.svg)](https://search.maven.org/artifact/io.klerch/alexa.utterances/2.0.0/jar)

## **How to use this JAR file in your Command-line**

The easiest way to do it is to use the command-line interface (CLI) by running the _alexa-generate.jar_ file. Simply build the project or
download the JAR file of this folder. In your command-line you can now run:

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