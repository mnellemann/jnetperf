package biz.nellemann.jperf;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A datagram consists of
 *
 * <-------------------- HEADER 32 bytes -------------->  <---------- DATA -------->
 *  int-4bytes   int-4bytes  long-8bytes  long-8bytes
 *     TYPE         SIZE      SEQUENCE     TIMESTAMP
 *
 */

public class Datagram {

    final Logger log = LoggerFactory.getLogger(Datagram.class);

    private final int HEADER_LENGTH = 24;

    private final int type;
    private final int size;
    private final long sequence;
    private final long timestamp;

    private final byte[] data;


    /**
     * Create new empty datagram
     * @param type
     * @param size
     * @param sequence
     */
    public Datagram(int type, int size, long sequence) {
        this.type = type;
        this.size = size;
        this.sequence = sequence;
        this.timestamp = System.currentTimeMillis();

        this.data = new byte[size];
    }



    /**
     * Assemble datagram from payload
     * @param payload
     */
    public Datagram(byte[] payload) {

        log.info("Datagram() 1");

        ByteBuffer buffer = ByteBuffer.allocate(payload.length);

        // Order is importent when assembling header fields like this
        type = buffer.getInt();
        size = buffer.getInt();
        sequence = buffer.getLong();
        timestamp = buffer.getLong();

        log.info("Datagram() 2 ");


        // Read everything after Header
        data = new byte[size];

        log.info("Datagram() 3 ");

        buffer.get(data, HEADER_LENGTH, size);  // ERROR

        log.info("Datagram() 4");
    }


    public int getLength() {
        return HEADER_LENGTH + data.length;
    }


    public byte[] getPayload() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH + data.length + 1);

        // Order is important
        buffer.putInt(type);
        buffer.putInt(size);
        buffer.putLong(sequence);
        buffer.putLong(timestamp);

        buffer.put(data);

        return buffer.array();
    }


    public int getType() {
        return type;
    }


    public long getSequence() {
        return sequence;
    }


}
