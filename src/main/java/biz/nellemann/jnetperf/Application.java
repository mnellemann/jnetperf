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
package biz.nellemann.jnetperf;


import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;


@Command(name = "jnetperf", mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "For more information visit https://git.data.coop/nellemann/jnetperf")
public class Application implements Callable<Integer> {

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    RunMode runMode;

    static class RunMode {
        @CommandLine.Option(names = { "-c", "--connect" }, required = true, description = "Connect to remote server.", paramLabel = "HOST")
        String remoteServer;

        @CommandLine.Option(names = { "-s", "--server" }, required = true, description = "Run server and wait for client.")
        boolean runServer = false;
    }

    @CommandLine.Option(names = { "-l", "--pkt-len" }, paramLabel = "NUM", description = "Packet size in bytes [default: ${DEFAULT-VALUE}].", converter = SuffixConverter.class)
    int packetSize = Payload.DEFAULT_LENGTH;

    @CommandLine.Option(names = { "-n", "--pkt-num" }, paramLabel = "NUM", description = "Number of packets to send [default: ${DEFAULT-VALUE}].", converter = SuffixConverter.class)
    int packetCount = 150_000;

    @CommandLine.Option(names = { "-p", "--port" }, paramLabel = "PORT", description = "Network port [default: ${DEFAULT-VALUE}].")
    int port = 4445;

    @CommandLine.Option(names = { "-u", "--udp" }, description = "Use UDP network protocol [default: ${DEFAULT-VALUE}].")
    boolean useUdp = false;



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

        if(packetSize < Payload.MIN_LENGTH) {
            packetSize = Payload.MIN_LENGTH;
        }

        if(useUdp) {
            if(packetSize > Payload.MAX_UDP_LENGTH) {
                packetSize = Payload.MAX_UDP_LENGTH;
            }
            UdpClient udpClient = new UdpClient(remoteHost, port, packetSize, packetCount, 0);
            udpClient.start();

        } else {
            TcpClient tcpClient = new TcpClient(remoteHost, port, packetSize, packetCount, 0);
            tcpClient.start();
        }
    }


    private void runServer() throws IOException, InterruptedException {
        if(useUdp) {
            UdpServer udpServer = new UdpServer(port);
            udpServer.start();
            udpServer.join();
        } else {
            TcpServer tcpServer = new TcpServer(port);
            tcpServer.start();
            tcpServer.join();
        }
    }

}
