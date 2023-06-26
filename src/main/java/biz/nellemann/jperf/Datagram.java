package biz.nellemann.jperf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Datagram consists of the following
 * <p>
 * <------------------------- HEADER 32 bytes -------------->  <---------- DATA bytes min 32, max 65475 -------->
 *      _long      _int     _int       _long     _long
 *     8_bytes   4_bytes   4_bytes    8_bytes   8_bytes
 *    MAGIC-ID     TYPE    LENGTH     CUR_PKT    MAX_PKT
 *
 */

public class Datagram {

    final Logger log = LoggerFactory.getLogger(Datagram.class);

    private final int HEADER_LENGTH = 32;
    private final byte[] MAGIC_ID = "jPerfTok".getBytes(StandardCharsets.UTF_8);    // Must be 8-bytes

    private final int type;
    private final int length;
    private final int realLength;
    private final long curPkt;
    private final long maxPkt;
    private final byte[] data;


    /**
     * Create new empty datagram
     * @param type
     * @param length
     * @param currentPkt
     */
    public Datagram(int type, int length, long currentPkt, long maxPkt) {

        log.debug("Datagram() - of type: {}, length: {}, sequence: {}", type, length, currentPkt, maxPkt);

        this.type = type;
        this.length = length;
        this.curPkt = currentPkt;
        this.maxPkt = maxPkt;

        if(type == DataType.DATA.getValue()) {
            realLength = length;
            data = new byte[length - HEADER_LENGTH];
        } else {
            realLength = HEADER_LENGTH * 2;
            data = new byte[HEADER_LENGTH * 2];
        }

        //random.nextBytes(data);
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
        curPkt = buffer.getLong();
        maxPkt = buffer.getLong();

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

        log.debug("getPayload() - with type: {}, length: {}, sequence: {}", type, length, curPkt);
        ByteBuffer buffer = ByteBuffer.allocate(data.length + HEADER_LENGTH);

        // Order is important
        buffer.put(MAGIC_ID);
        buffer.putInt(type);
        buffer.putInt(length);
        buffer.putLong(curPkt);
        buffer.putLong(maxPkt);

        buffer.put(data);

        return buffer.array();
    }


    public int getType() {
        return type;
    }


    public long getCurPkt() {
        return curPkt;
    }

    public long getMaxPkt() {
        return maxPkt;
    }


}
