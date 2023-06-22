package biz.nellemann.jperf;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServer extends Thread {

    final Logger log = LoggerFactory.getLogger(UdpServer.class);


    private final DatagramSocket socket;
    private byte[] buf = new byte[256];

    long pktsReceived, pktsReceivedTotal = 0;
    long bytesReceived, bytesReceivedTotal = 0;
    long bytesPerSec, pktsPerSec = 0;

    public UdpServer(int port) throws SocketException {
        log.info("UdpServer()");
        socket = new DatagramSocket(port);
    }

    public void run() {

        boolean running = true;

        try {

            while (running) {
                session();
            }

            socket.close();
        } catch(IOException e) {
            log.error(e.getMessage());
        }

    }


    public void printStatistics() {
        // Because we do this every second ...
        bytesPerSec = bytesReceived;
        pktsPerSec = pktsReceived;

        System.out.printf("%s recv: %d pkt/s\t %d B/s\t %d KB/s\t %d MB/s\n", Instant.now().toString(), pktsPerSec, bytesPerSec, bytesPerSec/1_000, bytesPerSec/1_000_000);
        pktsReceived = 0;
        bytesReceived = 0;
    }

    public void printSummary() {
        System.out.printf("%s recv: %d pkts\t %d B\t %d KB\t %d MB\n", Instant.now().toString(), pktsReceivedTotal, bytesReceivedTotal, bytesReceivedTotal/1_000, bytesReceivedTotal/1_000_000);
    }


    public void session() throws IOException {

        boolean running = true;

        boolean ackEnd = false;
        long thisSequence, lastSequence = 0;
        Instant startInstant = Instant.now();
        Instant checkInstant;

        while (running) {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            Datagram datagram = new Datagram(buf);
            thisSequence = datagram.getCurPkt();


            if(datagram.getType() == DataType.HANDSHAKE.getValue()) {
                log.info("Handshake from ... {}, length: {}", address, datagram.getLength());

                // Setup to receive larger datagrams
                buf = new byte[datagram.getLength()];

                // Send ACK
                Datagram responseDatagram = new Datagram(DataType.ACK.getValue(), 32, datagram.getCurPkt(), 1);
                packet = new DatagramPacket(responseDatagram.getPayload(), responseDatagram.getLength(), address, port);
                socket.send(packet);

            }

            if(datagram.getType() == DataType.DATA.getValue()) {
                bytesReceived += datagram.getLength();
                bytesReceivedTotal += datagram.getLength();

                if(thisSequence == lastSequence + 1) {
                    //log.info("Data .... size: {}, sequence: {}", datagram.getLength(), thisSequence);
                } else {
                    //log.warn("Data .... out of sequence: {} vs {}", thisSequence, lastSequence);
                }
            }


            if(datagram.getType() == DataType.END.getValue()) {
                ackEnd = true;
            }


            // Every second
            checkInstant = Instant.now();
            if(Duration.between(startInstant, checkInstant).toSeconds() >= 1) {
                printStatistics();
                startInstant = checkInstant;
            }

            if(ackEnd && pktsReceivedTotal > datagram.getMaxPkt()) {
                // Send ACK
                Datagram responseDatagram = new Datagram(DataType.ACK.getValue(), 32, datagram.getCurPkt(), 1);
                packet = new DatagramPacket(responseDatagram.getPayload(), responseDatagram.getLength(), address, port);
                socket.send(packet);

                printSummary();
                running = false;
            }


            lastSequence = thisSequence;
            pktsReceived++;
            pktsReceivedTotal++;

        }


    }

}
