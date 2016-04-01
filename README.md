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
name | is optional | default value | example
--- | --- | --- | --- | ---|
logsDir | yes | . | logDir
mail.smtp.host | yes | smtp.gmail.com | smtp.gmail.com
mail.smtp.port | yes | 587 | 587
mail.subject | yes | Address Discovery | Address Discovery [testing]
mail.to | no | | recipient@gmail.com
mail.user | no | | some_gmail@gmail.com
mail.pass | no | | some_password123
engine.threads | yes | <= vm.availableProcessors | 2
db.name | no | | AppDB
run.time | yes | now() | 23:57 or 23:57:34
run.period_seconds | yes | 86400 (1 day) | 3600
download.directory | yes | . | download
companies | no |  | company1, company2, company3, company4
companies.company1.name | yes | | company name GmbH
companies.company1.url | no | | http://www.company.de/impressum/
companies.company1.parsers | no | com.jojos.home.addresscomprehension.parse.DefaultParser | com.jojos.home.addresscomprehension.parse.MockParser, com.jojos.home.addresscomprehension.parse.DefaultParser
companies.company1.retain_download_data | yes | false | true


## a comma-separated list of company aliases to be used further down.
companies=company1, company2, company3, company4


## License

The address comperehension tool is made available under the terms of the Berkeley Software Distribution (BSD) license. This allow you complete freedom to use and distribute the code in source and/or binary form as long as you respect the original copyright.
Please see the LICENCE file for exact terms.
