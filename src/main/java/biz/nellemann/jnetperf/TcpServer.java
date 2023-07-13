package biz.nellemann.jnetperf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpServer extends Thread {

    final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private ServerSocket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private byte[] inBuffer;


    public TcpServer(int port) throws IOException {
        log.info("TcpServer()");

        socket = new ServerSocket(port);
        socket.setSoTimeout(10000);
    }


    public void run() {

        boolean running = true;

        try {
            while (running) {
                inBuffer = new byte[Datagram.DEFAULT_LENGTH];
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

        Socket server = socket.accept();
        InetAddress address = socket.getInetAddress();

        in = new DataInputStream(server.getInputStream());
        out = new DataOutputStream(server.getOutputStream());

        while (running) {

            Datagram datagram = receive();
            statistics.transferPacket();
            statistics.transferBytes(datagram.getLength());

            if(datagram.getType() == DataType.HANDSHAKE.getValue()) {
                log.info("Handshake from ... {}", address);
                // Setup to receive larger datagrams
                inBuffer = new byte[datagram.getLength()];
                statistics.reset();
            }

            if(datagram.getType() == DataType.END.getValue()) {
                ackEnd = true;
            }

            // Send ACK
            Datagram responseDatagram = new Datagram(DataType.ACK.getValue(), Datagram.DEFAULT_LENGTH, datagram.getCurPkt(), 1);
            out.write(responseDatagram.getPayload());
            statistics.ack();

            statistics.tick();
            if(ackEnd && statistics.getPacketsTransferredTotal() > datagram.getMaxPkt()) {
                running = false;
                statistics.printAverage();
                statistics.printSummary();
            }

        }

        in.close();
        out.close();
        server.close();

    }


    private Datagram receive() throws IOException {
        in.readFully(inBuffer);
        Datagram datagram = new Datagram(inBuffer);
        return datagram;
    }

}
