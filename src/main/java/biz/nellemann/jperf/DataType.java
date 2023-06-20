package biz.nellemann.jperf;

public enum DataType {


    HANDSHAKE(1), DATA(2), ACK(4), END(9);

    private final int value;

    private DataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}