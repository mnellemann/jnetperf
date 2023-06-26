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
package biz.nellemann.jperf;

import java.time.Duration;
import java.time.Instant;

public class Statistics {

    private long packetsTransferred, packetsTransferredTotal = 0;
    private long bytesTransferred, bytesTransferredTotal = 0;
    private long bytesPerSec, packesPerSec = 0;
    private long packetsUnacked = 0;

    private Instant timestamp1 = Instant.now();
    private Instant timestamp2 = Instant.now();


    public void reset() {
        timestamp1 = Instant.now();
    }

    public void tick() {

        timestamp2 = Instant.now();
        if(Duration.between(timestamp1, timestamp2).toMillis() >= 1000) {
            // Because we do this every second ...
            bytesPerSec = bytesTransferred;
            packesPerSec = packetsTransferred;
            timestamp1 = timestamp2;
            print();
            bytesTransferred = 0;
            packetsTransferred = 0;
        }

    }


    public void print() {
        System.out.printf("%-30s Status: %8d pkt/s %12d B/s %10d KB/s %8d MB/s\n", Instant.now().toString(), packesPerSec, bytesPerSec, bytesPerSec/1_000, bytesPerSec/1_000_000);

    }

    public void summary() {
        System.out.printf("%-29s Summary: %8d pkts %13d B %12d KB %10d MB %6d GB\n", Instant.now().toString(), packetsTransferredTotal, bytesTransferredTotal, bytesTransferredTotal /1_000, bytesTransferredTotal /1_000_000, bytesTransferredTotal/1_000_000_000);
    }

    public void ack() {
        packetsUnacked--;
    }


    public void transferPacket() {
        packetsUnacked++;
        packetsTransferred++;
        packetsTransferredTotal++;
    }

    public void transferBytes(long bytes) {
        bytesTransferred += bytes;
        bytesTransferredTotal += bytes;
    }

    public long getPacketsUnacked() {
        return packetsUnacked;
    }

    public long getPacketsTransferredTotal() {
        return packetsTransferredTotal;
    }

}
