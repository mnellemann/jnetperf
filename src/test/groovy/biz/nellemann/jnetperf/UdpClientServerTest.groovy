package biz.nellemann.jnetperf

import spock.lang.Shared
import spock.lang.Specification

class UdpClientServerTest extends Specification {

    static final int port = 9876;

    @Shared
    UdpServer udpServer = new UdpServer(port)

    // run before every feature method
    def setup() {
        udpServer.start();

    }

    // run after every feature method
    def cleanup() {
        udpServer.finish()
    }

    // run before the first feature method
    def setupSpec() {
    }

    // run after the last feature method
    def cleanupSpec() {
    }


    def "test client to server communication"() {
        setup:
        UdpClient client = new UdpClient("localhost", port, 512, 100, 60)

        when:
        client.start()

        then:
        client.getStatistics().getPacketsTransferredTotal() == 102  // packets + handshake + end
        client.getStatistics().getBytesTransferredTotal() == 53144  // TODO: Why is this larger than the TCP test ?
    }

}
