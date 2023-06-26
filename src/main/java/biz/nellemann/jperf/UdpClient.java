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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClient {

    final Logger log = LoggerFactory.getLogger(UdpClient.class);

    private Statistics statistics;

    private final int port;
    private final InetAddress address;
    private final DatagramSocket socket;

    private final byte[] buf = new byte[256];
    private final int packetCount;
    private final int packetSize;


    public UdpClient(String hostname, int port, int packets, int size) throws UnknownHostException, SocketException {
        log.info("UdpClient() - target: {}, port: {}", hostname, port);

        this.port = port;
        this.packetCount = packets;
        this.packetSize = size;

        socket = new DatagramSocket();
        address = InetAddress.getByName(hostname);
        statistics = new Statistics();
    }

    private void send(Datagram datagram) throws IOException {
        DatagramPacket packet = new DatagramPacket(datagram.getPayload(), datagram.getRealLength(), address, port);
        socket.send(packet);
        statistics.transferPacket();
        statistics.transferBytes(datagram.getRealLength());
    }

    private Datagram receive() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new Datagram(buf);
    }


    private void close() {
        socket.close();
    }


    public void start() throws IOException, InterruptedException {

        long sequence = 0;

        // Send handshake
        Datagram datagram = new Datagram(DataType.HANDSHAKE.getValue(), packetSize, sequence++, packetCount);
        send(datagram);

        datagram = receive();
        if(datagram.getType() != DataType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        // Data datagrams ...
        for(int i = 0; i < packetCount; i++) {
            datagram = new Datagram(DataType.DATA.getValue(), packetSize, sequence++, packetCount);
            send(datagram);
            datagram = receive();
            if(datagram.getType() != DataType.ACK.getValue()) {
                log.warn("No ACK!");
            }
            statistics.tick();
        }

        // End datagram
        //Thread.sleep(100);
        datagram = new Datagram(DataType.END.getValue(), packetSize, sequence++, packetCount);
        send(datagram);

        // TODO: Wait for ACK
        datagram = receive();
        statistics.ack();
        if(datagram.getType() != DataType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        Thread.sleep(100);
        close();
        statistics.printAverage();
        statistics.printSummary();
    }

}
