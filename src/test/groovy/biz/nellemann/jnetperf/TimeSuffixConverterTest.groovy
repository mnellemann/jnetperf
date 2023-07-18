package biz.nellemann.jnetperf

import spock.lang.Shared
import spock.lang.Specification

class TimeSuffixConverterTest extends Specification {

    @Shared
    TimeSuffixConverter timeSuffixConverter = new TimeSuffixConverter();


    def "test second to seconds"() {
        when:
        int seconds = timeSuffixConverter.convert("12s")

        then:
        seconds == 12;
    }


    def "test minute to seconds"() {
        when:
        int seconds = timeSuffixConverter.convert("120m")

        then:
        seconds == 7200;
    }


    def "test hour to seconds"() {
        when:
        int seconds = timeSuffixConverter.convert("48h")

        then:
        seconds == 172800;
    }

}
