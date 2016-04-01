## Synopsis

A tool to monitor the change of a companies postal address. This tool is using an expandable list of URLs of website credits to run a daily lookup process.
Once a company address has been changed, an e-mail is to be sent to a configured e-mail address. All changes detected are to be saved in a data base.
Instead of overwriting existing address-entries, changes are to be stored separately together with the date of detection.

## Build
mvn package

## How do I run it?

Build the application with 
mvn package

Then get the *-SNAPSHOT-jar-with-dependencies.jar and copy it to your running directory. Make sure you have also copied the samples/config.properties
on the same directory.

Run the jar file from the console issuginthe following command:
java -jar address-comprehension-utility-VERSION-jar-with-dependencies.jar

Requirements

The tool is built on Java 8. You'll also need maven to build it.

## License

The address comperehension tool is made available under the terms of the Berkeley Software Distribution (BSD) license. This allow you complete freedom to use and distribute the code in source and/or binary form as long as you respect the original copyright.
Please see the LICENCE file for exact terms.
