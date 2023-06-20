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


    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public UdpServer() throws SocketException {
        log.info("UdpServer()");
        socket = new DatagramSocket(4445);
    }

    public void run() {
        running = true;

        try {
            while (running) {

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();


                Datagram datagram = new Datagram(buf);

                if(datagram.getType() == DataType.HANDSHAKE.getValue()) {
                    log.info("Handshake from ...");

                }

                if(datagram.getType() == DataType.END.getValue()) {
                    running = false;
                    log.info("Stopping ....");
                }

                if(datagram.getType() == DataType.DATA.getValue()) {
                    log.info("Data .... size: {}", datagram.getLength());
                }


                // Send response ACK
                Datagram responseDatagram = new Datagram(DataType.ACK.getValue(), 32, datagram.getSequence());
                packet = new DatagramPacket(responseDatagram.getPayload(), responseDatagram.getLength(), address, port);
                socket.send(packet);

            }

            socket.close();
        } catch(IOException e) {

        }

    }


    private void ack() {


    }

    private void receive(String msg) {
        log.info("receive() - msg: {}", msg);
    }


}
