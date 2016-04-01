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

Run the jar file from the console issuing the following command (config.properties should be on the same folder):
java -jar address-comprehension-utility-VERSION-jar-with-dependencies.jar

Alternatively you can run it with a specific properties file
java -jar address-comprehension-utility-VERSION-jar-with-dependencies.jar -init patch_to_properties

## Requirements

The tool is built on Java 8. You'll also need maven to build it.

## Properties

A short explanation regarding the properties
* logsDir - Directory where the logs shall be written. Default to LOGS
* mail.smtp.host - The smtp host. Default is smtp.gmail.com
* mail.smtp.port - The smtp port. Default is 587
* mail.subject - The subject of the email report. Default is Address Discovery
* mail.to - The email recipien TODO: make it a comma separated of email addresses
* mail.user - Your email username
* mail.pass - Your email password
* engine.threads - number of threads used only for downloading and parsing. The rest of application will still run on the main thread. If that number is greater than the available processors then the latter shall be used.
* db.name - Name of the db
* run.time - Optional run time that can take one of the two formats: HH:mm:ss or HH:mm. Comment it if you want the tool to start downloading/parsing now.
* run.period_seconds - The period between successive executions in seconds. if omitted the default is one day or 24*60*60 = 86400 seconds
* download.directory - The optional download directory. If not defined the current dir is used
* companies - Comma-separated list of company aliases to be used as part of the next properties
* companies.company1.name - Defines an optional name for a specific alias. The alias must be defined in the 'companies' property
* companies.company1.url - Mandatory URL to be used for downloading and extracting the address(es)
* companies.company1.parsers - Comma separated list of parsers defined with their fully qualified class names. The com.jojos.home.addresscomprehension.parse.DefaultParser will always be used as a fallback if everything else fails. You might defined it although it doesn't make any difference if you don't
* companies.company1.retain_download_data - Optional boolean property that retains the files downloaded for each company alias. Default value is false (delete download file after done parsing it).

## License

The address comprehension tool is made available under the terms of the Berkeley Software Distribution (BSD) license. This allow you complete freedom to use and distribute the code in source and/or binary form as long as you respect the original copyright.
Please see the LICENCE file for exact terms.
