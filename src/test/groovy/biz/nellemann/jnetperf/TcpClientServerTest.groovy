package biz.nellemann.jnetperf

import spock.lang.Shared
import spock.lang.Specification

class TcpClientServerTest extends Specification {

    static final int port = 9876;

    @Shared
    TcpServer tcpServer = new TcpServer(port)

    // run before every feature method
    def setup() {
        tcpServer.start();

    }

    // run after every feature method
    def cleanup() {
        tcpServer.finish()
    }

    // run before the first feature method
    def setupSpec() {
    }

    // run after the last feature method
    def cleanupSpec() {
    }


    def "test client to server communication"() {
        setup:
        TcpClient client = new TcpClient("localhost", port, 512, 100, 60)

        when:
        client.start()

        then:
        client.getStatistics().getPacketsTransferredTotal() == 102  // packets + handshake + end
        client.getStatistics().getBytesTransferredTotal() == 52224
    }

}
