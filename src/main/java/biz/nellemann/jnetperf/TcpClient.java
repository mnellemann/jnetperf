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

    private final byte[] inBuffer = new byte[Payload.DEFAULT_LENGTH];
    private final int packetCount;
    private final int packetSize;
    private final int packetTime;


    public TcpClient(String hostname, int port, int size, int maxPackets, int maxTime) throws IOException {
        log.info("TcpClient() - target: {}, port: {}", hostname, port);

        this.port = port;
        this.packetSize = size;
        this.packetCount = maxPackets;
        this.packetTime = maxTime;

        address = InetAddress.getByName(hostname);
        statistics = new Statistics();
    }


    private void send(Payload payload) throws IOException {
        out.write(payload.getPayload());
        statistics.transferPacket();
        statistics.transferBytes(payload.getLength());
    }


    private Payload receive() throws IOException {
        in.readFully(inBuffer);
        return new Payload(inBuffer);
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
        Payload payload = new Payload(PayloadType.HANDSHAKE.getValue(), packetSize, sequence++, packetCount);
        send(payload);

        payload = receive();
        if(payload.getType() != PayloadType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        // Data datagrams ...
        for(int i = 0; i < packetCount; i++) {
            payload = new Payload(PayloadType.DATA.getValue(), packetSize, sequence++, packetCount);
            send(payload);
            payload = receive();
            if(payload.getType() != PayloadType.ACK.getValue()) {
                log.warn("No ACK!");
            }
            statistics.tick();
        }

        // End datagram
        //Thread.sleep(100);
        payload = new Payload(PayloadType.END.getValue(), packetSize, sequence++, packetCount);
        send(payload);

        // TODO: Wait for ACK
        payload = receive();
        statistics.ack();
        if(payload.getType() != PayloadType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        Thread.sleep(100);
        close();
        statistics.printAverage();
        statistics.printSummary();
    }

}
