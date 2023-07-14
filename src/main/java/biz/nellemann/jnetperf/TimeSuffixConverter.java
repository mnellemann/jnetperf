package biz.nellemann.jnetperf;

import picocli.CommandLine;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeSuffixConverter implements CommandLine.ITypeConverter<Integer> {

    final private Pattern pattern = Pattern.compile("(\\d+)([smh])?", Pattern.CASE_INSENSITIVE);

    public Integer convert(String value) {
        int seconds = 0;

        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            if(matcher.group(2) != null) {  // We got the second, minute or hour suffix
                String suffix = matcher.group(2);
                switch (suffix.toLowerCase(Locale.ROOT)) {
                    case "s":
                        seconds = number;
                        break;
                    case "m":
                        seconds = number * 60;
                        break;
                    case "h":
                        seconds = number * 60 * 60;
                        break;
                    default:
                        System.err.println("Unknown suffix: " + suffix);
                        seconds = number;
                }
            } else {
                seconds = number;
            }
        }
        return seconds;
    }

}
