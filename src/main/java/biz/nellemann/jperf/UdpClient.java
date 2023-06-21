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

    private final int port;
    private final InetAddress address;

    private DatagramSocket socket;

    private byte[] buf = new byte[256];

    public UdpClient(String hostname, int port) throws UnknownHostException, SocketException {
        log.info("UdpClient() - target: {}, port: {}", hostname, port);
        this.port = port;
        socket = new DatagramSocket();
        address = InetAddress.getByName(hostname);
    }

    public void send(Datagram datagram) throws IOException {
        DatagramPacket packet = new DatagramPacket(datagram.getPayload(), datagram.getRealLength(), address, port);
        socket.send(packet);
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

}
