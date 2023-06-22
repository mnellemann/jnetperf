# jPerf

Small utility to measure network performance.

## Requirements

You need Java (JRE) version 11 or later to run jperf.

## Usage Instructions

- Install the jperf package (*.deb*, *.rpm* or *.jar*) from [downloads](https://bitbucket.org/mnellemann/jperf/downloads/) or compile from source.
- Run **/opt/jperf/bin/jperf**, if installed from package
- Or as **java -jar /path/to/jperf.jar**

To change the temporary directory where disk-load files are written to, use the *-Djava.io.tmpdir=/mytempdir* option.

```shell
Usage: ...
```

## Development Information

You need Java (JDK) version 11 or later to build jperf.

### Build & Test

Use the gradle build tool, which will download all required dependencies:

```shell
./gradlew clean build run
```
