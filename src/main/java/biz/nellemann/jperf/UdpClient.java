package biz.nellemann.jperf;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClient {

    final Logger log = LoggerFactory.getLogger(UdpClient.class);

    private Statistics statistics;

    private final int port;
    private final InetAddress address;
    private final DatagramSocket socket;

    private byte[] buf = new byte[256];

    private int packetCount;
    private int packetSize;


    public UdpClient(String hostname, int port, int packets, int size) throws UnknownHostException, SocketException {
        log.info("UdpClient() - target: {}, port: {}", hostname, port);
        this.port = port;
        socket = new DatagramSocket();
        address = InetAddress.getByName(hostname);
        this.packetCount = packets;
        this.packetSize = size;
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

        // Start datagram
        Datagram datagram = new Datagram(DataType.HANDSHAKE.getValue(), packetSize, sequence++, packetCount);
        send(datagram);

        // TODO: Wait for ACK
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
        statistics.summary();
    }

}
