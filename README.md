# MyUW Exporter
Export your class schedule from MyUW to and .ics file so that one can import it to their calendar app such as Outlook or
Google calendar.

## Use
### Directly use
[Download the .jar file](https://github.com/eyhc1/ScheduleMyClasses/releases) from release to use the program directly. Due to log4j2 security issue, it is highly recommended to download release version 3 and up.
### Develop
Clone this repository into your computer, then open command prompt and direct it to the folder where all the files are
located. Then type `gradlew build` to build the project. If you are using an IDE such as Intellij, it might going to
build it automatically once you opened the project into it. After completing building it you can go to `src` folder to
edit the programs.
### Compile and jar
Type `gradlew jar` in the command prompt which has been directed into the project folder to create a standalone program.
The program .jar file should be created in build > libs directory.

## Depends
- Java 8
- jsoup 1.10.3
- gson 2.8.7
- log4j 2.14.1 (very old versions only)

## Old version
This repository was updated from a version that was written in Python. You can check the archived repository [here](https://github.com/eyhc1/visual-schedule-to-ics).

## Issues
- Sometimes the error popup shows nothing
- Some popup message not showing anything messages
- Program crashed when attempting to parse on web in Windows 11
- <s>Not working for 2 Factor Authendication (2FA)</s>

## Todo:
- [ ] Fix Issues
- [ ] Convert final exam schedule as well
- [x] Disable log4j for security
