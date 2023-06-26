/*
   Copyright 2023 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package biz.nellemann.jperf;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Callable;

@Command(name = "jperf", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class, description = "Network performance measurement tool.")
public class Application implements Callable<Integer> {

    final Logger log = LoggerFactory.getLogger(Application.class);


    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    RunMode runMode;

    static class RunMode {
        @CommandLine.Option(names = { "-c", "--connect" }, required = true, description = "Connect to remote server", paramLabel = "HOST")
        String remoteServer;

        @CommandLine.Option(names = { "-s", "--server" }, required = true, description = "Run server and wait for client")
        boolean runServer = false;
    }

    @CommandLine.Option(names = { "-l", "--pkt-len" }, paramLabel = "SIZE", description = "Datagram size in bytes, max 65507 [default: ${DEFAULT-VALUE}]")
    int packetSize = 65507; // Min: 256  Max: 65507

    @CommandLine.Option(names = { "-n", "--pkt-num" }, paramLabel = "NUM", description = "Number of packets to send [default: ${DEFAULT-VALUE}]")
    int packetCount = 150_000;

    @CommandLine.Option(names = { "-p", "--port" }, paramLabel = "PORT", description = "Network port [default: ${DEFAULT-VALUE}]")
    int port = 4445;



    @Override
    public Integer call() throws Exception {

        if(runMode.runServer) {
            runServer();
        } else if(runMode.remoteServer != null) {
            runClient(runMode.remoteServer);
        }

        return 0;
    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }



    private void runClient(String remoteHost) throws InterruptedException, IOException {
        UdpClient udpClient = new UdpClient(remoteHost, port, packetCount, packetSize);
        udpClient.start();
    }


    private void runServer() throws SocketException, InterruptedException {
        UdpServer udpServer = new UdpServer(port);
        udpServer.start();
        udpServer.join();
    }

}
