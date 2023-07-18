package biz.nellemann.jnetperf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer extends Thread {

    final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private final int port;
    private ServerSocket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private byte[] inBuffer;
    private boolean runThread = true;
    private boolean runSession = true;


    public TcpServer(int port) throws IOException {
        log.info("TcpServer()");
        this.port = port;
    }


    public void run() {

        try {
            while (runThread) {
                socket = new ServerSocket(port);
                socket.setSoTimeout(0); // Wait indefinitely
                inBuffer = new byte[Payload.DEFAULT_LENGTH];
                session();
                socket.close();
            }
        } catch(IOException e) {
            log.error(e.getMessage());
        }

    }


    public void session() throws IOException {

        Statistics statistics = new Statistics();
        boolean ackEnd = false;
        runSession = true;

        Socket server = socket.accept();
        InetAddress address = socket.getInetAddress();

        in = new DataInputStream(server.getInputStream());
        out = new DataOutputStream(server.getOutputStream());

        while (runSession) {

            Payload payload = receive();
            statistics.transferPacket();
            statistics.transferBytes(payload.getLength());

            if(payload.getType() == PayloadType.HANDSHAKE.getValue()) {
                log.info("Handshake from ... {}", address);
                // Setup to receive larger datagrams
                inBuffer = new byte[payload.getLength()];
                statistics.reset();
            }

            if(payload.getType() == PayloadType.END.getValue()) {
                ackEnd = true;
            }

            // Send ACK
            Payload responsePayload = new Payload(PayloadType.ACK.getValue(), Payload.DEFAULT_LENGTH, payload.getCurPkt(), 1);
            out.write(responsePayload.getPayload());
            statistics.ack();

            statistics.tick();
            if(ackEnd) {
                runSession = false;
                statistics.printAverage();
                statistics.printSummary();
            }

        }

        in.close();
        out.close();
        server.close();

    }


    private Payload receive() throws IOException {
        in.readFully(inBuffer);
        return new Payload(inBuffer);
    }


    public void finish() {
        runThread = false;
        runSession = false;
    }

}
