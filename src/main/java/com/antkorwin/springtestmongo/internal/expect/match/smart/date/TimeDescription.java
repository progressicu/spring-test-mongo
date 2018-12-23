package com.antkorwin.springtestmongo.internal.expect.match.smart.date;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 22.12.2018.
 *
 * @author Korovin Anatoliy
 */
public class TimeDescription {

    private static final String TIME_DESCRIPTION_PATTERN =
            "^(\\[NOW\\])" +
            "((\\+|\\-)([0-9]{1,7})(\\((DAYS|HOURS|MINUTES|SECONDS)\\))){0,1}" +
            "(\\{THR=([0-9]{1,10})\\}){0,1}$";

    private final static int DEFAULT_THRESHOLD = 10_000;

    private final String description;

    private Integer threshold = null;

    public TimeDescription(String description) {
        this.description = description;
    }

    //@formatter:off
    /**
     * Parse time operation from the String description.
     *
     * direction (group 3)
     *    |
     *    |  TimeUnit (group 6)
     *    |    |
     *    V    V
     * NOW+17(DAYS)
     *      ^
     *      |
     *    amount of time (group 4)
     *
     * @return time operation
     */
    //@formatter:on
    public TimeOperation getTimeOperation() {

        Matcher matcher = matchTimeDescriptionPattern(description);

        if (matcher.group(2) == null) {
            return new TimeOperation(TimeDirection.UNDEFINED, null, 0);
        }

        TimeDirection direction = (matcher.group(3).equals("+"))
                                  ? TimeDirection.PLUS
                                  : TimeDirection.MINUS;

        int count = Integer.valueOf(matcher.group(4));

        TimeUnit unit = TimeUnit.valueOf(matcher.group(6));

        threshold = parseThreshold(matcher);

        return new TimeOperation(direction, unit, count);
    }

    /**
     * Match a description with the pattern of expected time
     *
     * @return true if description match to pattern and false if not
     */
    public boolean matches() {
        Matcher matcher = matchTimeDescriptionPattern(description);
        return matcher.matches();
    }

    /**
     * @return amount of the possible threshold
     */
    public long getThreshold() {
        if (threshold == null) {
            Matcher matcher = matchTimeDescriptionPattern(description);
            threshold = parseThreshold(matcher);
        }
        return threshold;
    }

    private Matcher matchTimeDescriptionPattern(String value) {
        Pattern pattern = Pattern.compile(TIME_DESCRIPTION_PATTERN);
        Matcher matcher = pattern.matcher(value);
        matcher.find();
        return matcher;
    }

    private Integer parseThreshold(Matcher matcher){
        return threshold = (matcher.group(8) != null)
                    ? Integer.valueOf(matcher.group(8))
                    : DEFAULT_THRESHOLD;
    }
}