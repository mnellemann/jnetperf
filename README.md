# jnetperf

Small utility to measure (single threaded) network performance between two hosts.

## Requirements

You need Java (JRE) version 8 or later to run jnetperf.

## Usage Instructions

- Install the jnetperf package (*.deb*, *.rpm* or *.jar*) from [releases](https://github.com/mnellemann/jnetperf/releases) or compile from source.
- Run **/opt/jnetperf/bin/jnetperf**, if installed from package, or as **java -jar /path/to/jnetperf-x.y.z-all.jar**

```shell
Usage: jnetperf [-huV] [-l=NUM] [-n=NUM] [-p=NUM] [-t=SEC] (-c=SRV | -s)
For more information visit https://github.com/mnellemann/jnetperf
  -c, --connect=SRV   Connect to remote server (client).
  -h, --help          Show this help message and exit.
  -l, --pkt-len=NUM   Packet size in bytes (client) [default: 1432].
  -n, --pkt-num=NUM   Number of packets to send (client) [default: 150000].
  -p, --port=NUM      Network port [default: 4445].
  -s, --server        Run server and wait for client (server).
  -t, --runtime=SEC   Time to run, supersedes pkt-num (client) [default: 0].
  -u, --udp           Use UDP network protocol [default: false].
  -V, --version       Print version information and exit.
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

-----

<details closed>
  <summary><B>Development and Local Testing</B></summary>

## Development Information

You need Java (JDK) version 8 or later to build jnetperf.


### Build & Test

Use the gradle build tool, which will download all required dependencies:

```shell
./gradlew clean build
```
