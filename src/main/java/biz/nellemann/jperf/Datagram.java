package biz.nellemann.jperf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Datagram consists of the following
 * <p>
 * <------------------------- HEADER 32 bytes -------------->  <---------- DATA min 32 bytes -------->
 *      _long      _int     _int       _long     _long
 *     8_bytes   4_bytes   4_bytes    8_bytes   8_bytes
 *    MAGIC-ID     TYPE    LENGTH    SEQUENCE  TIMESTAMP
 *
 */

public class Datagram {

    final Logger log = LoggerFactory.getLogger(Datagram.class);

    private final int HEADER_LENGTH = 32;

    private final byte[] MAGIC_ID = "jPerfTok".getBytes(StandardCharsets.UTF_8);    // Must be 8-bytes

    private final int type;
    private final int length;
    private final int realLength;
    private final long sequence;
    private final long timestamp;
    private final byte[] data;


    /**
     * Create new empty datagram
     * @param type
     * @param length
     * @param sequence
     */
    public Datagram(int type, int length, long sequence) {

        log.debug("Datagram() - of type: {}, length: {}, sequence: {}", type, length, sequence);

        this.type = type;
        this.length = length;
        this.sequence = sequence;
        this.timestamp = System.currentTimeMillis();

        if(type == DataType.DATA.getValue()) {
            realLength = length;
            data = new byte[length - HEADER_LENGTH];
        } else {
            realLength = HEADER_LENGTH * 2;
            data = new byte[HEADER_LENGTH * 2];
        }
    }



    /**
     * Assemble datagram from payload
     * @param payload
     */
    public Datagram(byte[] payload) throws IOException {

        log.debug("Datagram() magic ID is: {} bytes long and contains: {}", MAGIC_ID.length, MAGIC_ID.toString());

        ByteBuffer buffer = ByteBuffer.wrap(payload);
        byte[] id = new byte[8];
        buffer.get(id);
        if(!Arrays.equals(id, MAGIC_ID)) {
            log.warn("Datagram() - magic ID does not match!");
            throw new IOException();
        }

        // Order is importent when assembling header fields like this
        type = buffer.getInt();
        length = buffer.getInt();
        sequence = buffer.getLong();
        timestamp = buffer.getLong();

        realLength = length;
        if(type == DataType.DATA.getValue()) {
            data = new byte[length - HEADER_LENGTH];
            buffer.get(data, 0, data.length);
        } else {
            data = new byte[HEADER_LENGTH * 2];
        }
    }


    public int getLength() {
        return length;
    }

    public int getRealLength() {
        return realLength;
    }

    public byte[] getPayload() throws IOException {

        log.debug("getPayload() - with type: {}, length: {}, sequence: {}", type, length, sequence);
        ByteBuffer buffer = ByteBuffer.allocate(data.length + HEADER_LENGTH);

        // Order is important
        buffer.put(MAGIC_ID);
        buffer.putInt(type);
        buffer.putInt(length);
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
