/*
   Copyright 2023 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package biz.nellemann.jnetperf;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


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

public class Payload {

    public final static int MIN_LENGTH = 64;
    public final static int MAX_UDP_LENGTH = 65507;
    public final static int DEFAULT_LENGTH = 1432;
    public final static int HEADER_LENGTH = 32;

    private final byte[] MAGIC_ID = "jPerfTok".getBytes(StandardCharsets.UTF_8);    // Must be 8-bytes

    private final int type;
    private final int length;
    private final long curPkt;
    private final long maxPkt;
    private final byte[] data;


    /**
     * Create new empty datagram
     * @param type
     * @param length
     * @param currentPkt
     */
    public Payload(int type, int length, long currentPkt, long maxPkt) {
        this.type = type;
        this.curPkt = currentPkt;
        this.maxPkt = maxPkt;
        this.length = length;

        if (type == PayloadType.HANDSHAKE.getValue()) {
            data = new byte[DEFAULT_LENGTH - HEADER_LENGTH];
        } else {
            data = new byte[length - HEADER_LENGTH];
        }
    }

    /**
     * Assemble datagram from byte[] payload
     * @param payload
     */
    public Payload(byte[] payload) {
        this(ByteBuffer.wrap(payload));
    }


    /**
     * Assemble datagram from ByteBuffer payload
     * @param payload
     */
    public Payload(ByteBuffer payload) {

        byte[] id = new byte[8];
        payload.get(id);
        if(!Arrays.equals(id, MAGIC_ID)) {
            System.out.println(Arrays.toString(id));
            System.out.println(Arrays.toString(MAGIC_ID));
            throw new RuntimeException("Datagram magic ID does not match: " + MAGIC_ID);
        }

        // Order is importent when assembling header fields like this
        type = payload.getInt();
        length = payload.getInt();
        curPkt = payload.getLong();
        maxPkt = payload.getLong();

        data = new byte[payload.limit() - payload.position()];
        payload.get(data);
    }

    public int getLength() {
        return length;
    }


    public byte[] getPayload() {

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
