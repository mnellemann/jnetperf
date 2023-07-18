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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClient {

    final Logger log = LoggerFactory.getLogger(UdpClient.class);

    private final Statistics statistics;

    private final int port;
    private final InetAddress address;
    private final DatagramSocket socket;

    private final byte[] inBuffer = new byte[Payload.DEFAULT_LENGTH];
    private final int length;
    private final int packets;
    private final int runtime;


    public UdpClient(String hostname, int port, int length, int packets, int runtime) throws UnknownHostException, SocketException {
        log.info("UdpClient() - target: {}, port: {}", hostname, port);

        this.port = port;
        this.length = length;
        this.packets = packets;
        this.runtime = runtime;

        socket = new DatagramSocket();
        address = InetAddress.getByName(hostname);
        statistics = new Statistics();
    }

    private void send(Payload payload) throws IOException {
        DatagramPacket packet = new DatagramPacket(payload.getPayload(), payload.getLength(), address, port);
        socket.send(packet);
        statistics.transferPacket();
        statistics.transferBytes(payload.getLength());
    }

    private Payload receive() throws IOException {
        DatagramPacket packet = new DatagramPacket(inBuffer, Payload.DEFAULT_LENGTH);
        socket.receive(packet);
        return new Payload(inBuffer);
    }


    private void close() {
        socket.close();
    }


    public void start() throws IOException, InterruptedException {

        AtomicBoolean keepRunning = new AtomicBoolean(true);
        Thread shutdownHook = new Thread(() -> {
            keepRunning.set(false);
            System.out.println("Stopping jnetperf, please wait ...");
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        long sequence = 0;

        // Send handshake
        Payload payload = new Payload(PayloadType.HANDSHAKE.getValue(), Payload.DEFAULT_LENGTH, sequence++, packets);
        send(payload);
        payload = receive();
        if(payload.getType() != PayloadType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        // Send data
        do {
            payload = new Payload(PayloadType.DATA.getValue(), length, sequence++, packets);
            send(payload);
            payload = receive();
            if(payload.getType() != PayloadType.ACK.getValue()) {
                log.warn("No ACK!");
            }
            statistics.tick();

            if (sequence > packets) {
                System.out.println("Max packets reached");
                keepRunning.set(false);
            }

            if(runtime > 0  && statistics.getRuntime() > runtime) {
                System.out.println("Max runtime reached");
                keepRunning.set(false);
            }

        } while (keepRunning.get());


        // Send end
        //Thread.sleep(100);
        payload = new Payload(PayloadType.END.getValue(), length, sequence++, packets);
        send(payload);
        payload = receive();
        statistics.ack();
        if(payload.getType() != PayloadType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        Thread.sleep(100);
        close();
        statistics.printAverage();
        statistics.printSummary();
    }

}
