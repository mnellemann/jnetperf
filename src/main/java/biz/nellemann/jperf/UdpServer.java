package biz.nellemann.jperf;

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
    private byte[] buf = new byte[256];


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


    public void session() throws IOException {

        Statistics statistics = new Statistics();
        boolean running = true;
        boolean ackEnd = false;

        while (running) {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            Datagram datagram = new Datagram(buf);
            statistics.transferPacket();
            statistics.transferBytes(datagram.getRealLength());

            if(datagram.getType() == DataType.HANDSHAKE.getValue()) {
                log.info("Handshake from ... {}, length: {}", address, datagram.getLength());
                // Setup to receive larger datagrams
                buf = new byte[datagram.getLength()];
                statistics.reset();
            }

            /*
            if(datagram.getType() == DataType.DATA.getValue()) {
                bytesReceived += datagram.getRealLength();
                bytesReceivedTotal += datagram.getRealLength();
            }*/

            if(datagram.getType() == DataType.END.getValue()) {
                ackEnd = true;
            }

            // Send ACK
            Datagram responseDatagram = new Datagram(DataType.ACK.getValue(), 32, datagram.getCurPkt(), 1);
            packet = new DatagramPacket(responseDatagram.getPayload(), responseDatagram.getLength(), address, port);
            socket.send(packet);
            statistics.ack();

            statistics.tick();
            if(ackEnd && statistics.getPacketsTransferredTotal() > datagram.getMaxPkt()) {
                running = false;
                statistics.summary();
            }


        }


    }

}
