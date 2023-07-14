package biz.nellemann.jnetperf;

import picocli.CommandLine;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuffixConverter implements CommandLine.ITypeConverter<Integer> {

    final private Pattern pattern = Pattern.compile("(\\d+)([kmg])?b?", Pattern.CASE_INSENSITIVE);

    public Integer convert(String value) {
        int bytes = 0;

        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            if(matcher.group(2) != null) {  // We got the kilo, mega og giga suffix
                String suffix = matcher.group(2);
                switch (suffix.toLowerCase(Locale.ROOT)) {
                    case "k":
                        bytes = number * 1024;
                        break;
                    case "m":
                        bytes = number * 1024 * 1024;
                        break;
                    case "g":
                        bytes = number * 1024 * 1024 * 1024;
                        break;
                    default:
                        System.err.println("Unknown suffix: " + suffix);
                        bytes = number;
                }
            } else {
                bytes = number;
            }
        }
        return bytes;
    }

}
