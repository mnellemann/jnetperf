# jnetperf

Small utility to measure (single threaded) network performance between two hosts.

## Requirements

You need Java (JRE) version 8 or later to run jnetperf.

## Usage Instructions

- Install the jnetperf package (*.deb*, *.rpm* or *.jar*) from [downloads](https://bitbucket.org/mnellemann/jnetperf/downloads/) or compile from source.
- Run **/opt/jnetperf/bin/jperf**, if installed from package, or as **java -jar /path/to/jnetperf.jar**

```shell
Usage: jnetperf [-hV] [-l=SIZE] [-n=NUM] [-p=PORT] (-c=SERVER | -s)
For more information visit https://git.data.coop/nellemann/jnetperf
  -c, --connect=SERVER   Connect to remote server.
  -h, --help             Show this help message and exit.
  -l, --pkt-len=SIZE     Datagram size in bytes, max 65507 [default: 65507].
  -n, --pkt-num=NUM      Number of packets to send [default: 150000].
  -p, --port=PORT        Network port [default: 4445].
  -s, --server           Run server and wait for client.
  -V, --version          Print version information and exit.
```


## Examples

On *host A* run jnetperf as a server waiting for a connection from a client:

```shell
java -jar jnetperf-x.y.z-all.jar -s
```

On *host B* run jnetperf as a client connecting to the server and sending data:

```shell
java -jar jnetperf-x.y.z-all.jar -c server-ip
```





## Development Information

You need Java (JDK) version 8 or later to build jnetperf.

### Build & Test

Use the gradle build tool, which will download all required dependencies:

```shell
./gradlew clean build
```
