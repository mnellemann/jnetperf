package biz.nellemann.jnetperf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpClient {

    final Logger log = LoggerFactory.getLogger(TcpClient.class);

    private final Statistics statistics;

    private DataOutputStream out;
    private DataInputStream in;


    private final int port;
    private final InetAddress address;
    private Socket socket;

    private final byte[] inBuffer = new byte[Payload.DEFAULT_LENGTH];
    private final int packets;
    private final int length;
    private final int runtime;


    public TcpClient(String hostname, int port, int length, int packets, int runtime) throws IOException {
        log.info("TcpClient() - target: {}, port: {}", hostname, port);

        this.port = port;
        this.length = length;
        this.packets = packets;
        this.runtime = runtime;

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

        AtomicBoolean keepRunning = new AtomicBoolean(true);
        Thread shutdownHook = new Thread(() -> {
            keepRunning.set(false);
            System.out.println("Stopping jnetperf, please wait ...");
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        long sequence = 0;
        socket = new Socket(address, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());


        // Send handshake
        Payload payload = new Payload(PayloadType.HANDSHAKE.getValue(), length, sequence++, packets);
        send(payload);
        payload = receive();
        if(payload.getType() != PayloadType.ACK.getValue()) {
            log.warn("No ACK!");
            return;
        }

        // Send data
        do {
            payload = new Payload(PayloadType.DATA.getValue(), length, sequence++, packets);
            send(payload);
            payload = receive();
            if(payload.getType() != PayloadType.ACK.getValue()) {
                log.warn("No ACK!");
            }
            statistics.tick();

            if (sequence > packets) {
                System.out.println("Max packets reached");
                keepRunning.set(false);;
            }

            if(runtime > 0 && statistics.getRuntime() > runtime) {
                System.out.println("Max runtime reached");
                keepRunning.set(false);
            }

        } while (keepRunning.get());

        // Send end
        payload = new Payload(PayloadType.END.getValue(), length, sequence++, packets);
        send(payload);
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
