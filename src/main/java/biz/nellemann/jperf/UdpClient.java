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

    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public UdpClient() throws UnknownHostException, SocketException {
        log.info("UdpClient");
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void send(Datagram datagram) throws IOException {
        DatagramPacket packet = new DatagramPacket(datagram.getPayload(), datagram.getLength(), address, 4445);
        socket.send(packet);
    }


    public String sendEcho(String msg) throws IOException {
        log.info("send() - msg: {}", msg);

        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String( packet.getData(), 0, packet.getLength() );
        return received;
    }

    public void close() {
        socket.close();
    }

}
