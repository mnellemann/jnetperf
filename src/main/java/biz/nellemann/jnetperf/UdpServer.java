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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServer extends Thread {

    final Logger log = LoggerFactory.getLogger(UdpServer.class);

    private final DatagramSocket socket;
    private byte[] inBuffer;


    public UdpServer(int port) throws SocketException {
        log.info("UdpServer()");
        socket = new DatagramSocket(port);
    }

    public void run() {

        boolean running = true;

        try {
            while (running) {
                inBuffer = new byte[Payload.DEFAULT_LENGTH];
                session();
            }
            socket.close();
        } catch(IOException e) {
            log.error(e.getMessage());
        }

    }


    public void session() throws IOException {

        Statistics statistics = new Statistics();
        boolean running = true;
        boolean ackEnd = false;

        while (running) {

            DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            Payload payload = new Payload(packet.getData());
            statistics.transferPacket();
            statistics.transferBytes(payload.getLength());

            if(payload.getType() == PayloadType.HANDSHAKE.getValue()) {
                log.info("Handshake from ... {}", address);
                // Setup to receive larger datagrams
                inBuffer = new byte[payload.getLength()];
                statistics.reset();
            }

            if(payload.getType() == PayloadType.END.getValue()) {
                ackEnd = true;
            }

            // Send ACK
            Payload responsePayload = new Payload(PayloadType.ACK.getValue(), Payload.DEFAULT_LENGTH, payload.getCurPkt(), 1);
            packet = new DatagramPacket(responsePayload.getPayload(), responsePayload.getLength(), address, port);
            socket.send(packet);
            statistics.ack();

            statistics.tick();
            if(ackEnd) {
                running = false;
                statistics.printAverage();
                statistics.printSummary();
            }

        }


    }

}
