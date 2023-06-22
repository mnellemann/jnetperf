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

    private final int port;
    private final InetAddress address;
    private final DatagramSocket socket;

    private byte[] buf = new byte[256];
    private long packetsSent = 0;
    private long bytesSent = 0;


    public UdpClient(String hostname, int port) throws UnknownHostException, SocketException {
        log.info("UdpClient() - target: {}, port: {}", hostname, port);
        this.port = port;
        socket = new DatagramSocket();
        address = InetAddress.getByName(hostname);
    }

    public void send(Datagram datagram) throws IOException {
        DatagramPacket packet = new DatagramPacket(datagram.getPayload(), datagram.getRealLength(), address, port);
        socket.send(packet);
        packetsSent++;
        bytesSent += datagram.getRealLength();
    }

    public Datagram receive() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new Datagram(buf);
    }

    public String sendEcho(String msg) throws IOException {
        log.info("send() - msg: {}", msg);

        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new String( packet.getData(), 0, packet.getLength() );
    }

    public void close() {
        socket.close();
    }

    public void printStatistics() {
        System.out.printf("%s sent: %d pkts\t %d B\t %d KB\t %d MB\n", Instant.now().toString(), packetsSent, bytesSent, bytesSent/1000, bytesSent/1_000_000);
    }
}
