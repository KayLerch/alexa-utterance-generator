# Alexa Utterance Generator
Use this tool if you'd like to generate hundreds and thousands of variant utterances
for your Alexa skills.

## How to

Create a new txt-file in _/src/main/resources/utterances_ and start writing down generic utterances.
The magic comes with using placeholders in curly brackets. You need to set the chosen filename as a key
in the UtteranceGenerator code-file or hand it in as a parameter when running this application from cli.

### Value placeholders
```xml
help me {get|book} a {taxi|room|table}
```

results in six utterances (_help me get a taxi_, _help me book a room_ etc.)

### Resource placeholders

If you have a lot of values for your placeholders it's worth maintaining them in
a file. Create one to many new txt-files in _src/main/resources/placeholders_ and use
the chosen file-name as the placeholder-name. If the name cannot be resolved to
a placeholder-file the script treats it as a value placeholder.

Assume you created a file named "city.txt" that contains
```xml
Boston
Berlin
Bristol
```

an utterance like 

```xml
help me {get|book} a {taxi|room|table} in {city}
```

results in 18 variants (_help me get a taxi in Boston_, _help me book a room in Berlin_ etc.)

### Value ranges

The generator is also able to resolve numeric range defined in placeholders.

```xml
help me {get|book} a {taxi|room|table} in {city} at {1-12}:00 {AM|PM}
``` 

results in 432 variants (_help me get a taxi in Boston at 1:00 AM_, _help me book a table in Bristol at 12:00 PM_ etc.)

### Escape placeholders

Placeholders follow the same syntax as slots in regular utterances. You might want to keep a {slot} in 
generated utterances without having it resolved by this script. 

```xml
help me {get|book} a {taxi|room|table} in {{city}} at {1-12}:00 {AM|PM}
``` 

returns utterances like "_help me get a taxi in {city} at 1:00 AM_".