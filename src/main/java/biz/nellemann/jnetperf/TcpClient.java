package biz.nellemann.jnetperf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

public class TcpClient {

    final Logger log = LoggerFactory.getLogger(TcpClient.class);

    private Statistics statistics;

    private DataOutputStream out;
    private DataInputStream in;


    private final int port;
    private final InetAddress address;
    private Socket socket;

    private final byte[] inBuffer = new byte[Datagram.DEFAULT_LENGTH];
    private final int packetCount;
    private final int packetSize;


    public TcpClient(String hostname, int port, int packets, int size) throws IOException {
        log.info("TcpClient() - target: {}, port: {}", hostname, port);

        this.port = port;
        this.packetCount = packets;
        this.packetSize = size;

        address = InetAddress.getByName(hostname);
        statistics = new Statistics();
    }


    private void send(Datagram datagram) throws IOException {
        out.write(datagram.getPayload());
        statistics.transferPacket();
        statistics.transferBytes(datagram.getLength());
    }


    private Datagram receive() throws IOException {
        in.readFully(inBuffer);
        return new Datagram(inBuffer);
    }


    private void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }


    public void start() throws IOException, InterruptedException {

        socket = new Socket(address, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

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
        System.out.println("Sending END datagram");

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
