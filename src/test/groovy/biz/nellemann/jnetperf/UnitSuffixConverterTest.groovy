package biz.nellemann.jnetperf

import spock.lang.Shared
import spock.lang.Specification

class UnitSuffixConverterTest extends Specification {

    @Shared
    UnitSuffixConverter unitSuffixConverter = new UnitSuffixConverter();


    def "test byte (b) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("16b")

        then:
        bytes == 16;
    }


    def "test kilo (k) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("2048k")

        then:
        bytes == 2097152;
    }

    def "test kilo (kb) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("2048kb")

        then:
        bytes == 2097152;
    }

    def "test mega (m) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("2m")

        then:
        bytes == 2097152;
    }

    def "test mega (mb) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("2mb")

        then:
        bytes == 2097152;
    }


    def "test giga (g) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("1g")

        then:
        bytes == 1073741824;
    }


    def "test giga (gb) to bytes"() {
        when:
        long bytes = unitSuffixConverter.convert("1gb")

        then:
        bytes == 1073741824;
    }

}
