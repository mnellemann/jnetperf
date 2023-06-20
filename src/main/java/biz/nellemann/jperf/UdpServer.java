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
    private boolean running;
    private byte[] buf = new byte[256];

    public UdpServer() throws SocketException {
        log.info("UdpServer()");
        socket = new DatagramSocket(4445);
    }

    public void run() {

        running = true;
        long thisSequence = 0;
        long lastSequence = 0;

        try {

            while (running) {

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                log.debug("run() - buffer is: {}", buf.length);

                Datagram datagram = new Datagram(buf);
                thisSequence = datagram.getSequence();

                if(datagram.getType() == DataType.HANDSHAKE.getValue()) {
                    log.info("Handshake from ... {}, length: {}", address, datagram.getLength());

                    // Setup to receive larger datagrams
                    buf = new byte[datagram.getLength()];

                    // TODO: Send ACK
                    Datagram responseDatagram = new Datagram(DataType.ACK.getValue(), 32, datagram.getSequence());
                    packet = new DatagramPacket(responseDatagram.getPayload(), responseDatagram.getLength(), address, port);
                    socket.send(packet);

                }

                if(datagram.getType() == DataType.END.getValue()) {
                    running = false;
                    log.info("Stopping ....");
                    // TODO: Reset ?
                }

                if(datagram.getType() == DataType.DATA.getValue()) {
                    if(thisSequence == lastSequence + 1) {
                        log.info("Data .... size: {}, sequence: {}", datagram.getLength(), thisSequence);
                    } else {
                        log.warn("Data .... out of sequence: {} vs {}", thisSequence, lastSequence);
                    }
                }


                lastSequence = thisSequence;


            }

            socket.close();
        } catch(IOException e) {
            log.error(e.getMessage());
        }

    }


    private void ack() {


    }

    private void receive(String msg) {
        log.info("receive() - msg: {}", msg);
    }


}
