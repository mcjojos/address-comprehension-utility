/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper utility class.
 *
 * Created by karanikasg@gmail.com.
 */
public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    private static final DateTimeFormatter POTENTIAL_FORMATS = DateTimeFormatter.ofPattern("[HH:mm:ss][HH:mm]");

    /**
     * This function attempts to parse a time. Supporting the use of potentially multiple formats from a single place.
     *
     * @param string the time
     * @return a LocalTIme object
     */
    private static LocalTime parseLocalTime(String string) {
        return LocalTime.parse(string, POTENTIAL_FORMATS);
    }

    public static Optional<Long> parseOptionalLong(String string) {
        try {
            return Optional.of(Long.parseLong(string));
        } catch (NumberFormatException e) {
            log.warn("Value {} cannot be parsed as Long", string);
        }
        return Optional.empty();
    }

    public static Optional<LocalTime> parseOptionalLocalTime(String string) {
        Optional<LocalTime> result = Optional.empty();

        try {
            result = Optional.of(parseLocalTime(string));
        } catch (DateTimeParseException e) {
            log.warn("Value {} cannot be parsed as LocalTime", string);
        }

        return result;
    }

    /**
     * Creates a comma-separated list of values.
     *
     * @param values the collection of values
     * @param mapFunction a function returning the string
     * @param <T> the type of the values
     * @return a string like "{a, b, c}"
     */
    public static <T> String toString(Collection<T> values,
                                      Function<T, String> mapFunction) {
        return values.stream().map(mapFunction).collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Same functionality as {@link #toString(Collection, Function)} but with optional parameters
     * for delimiter, prefix and suffix
     * @param values the collection of values
     * @param mapFunction a function returning the string
     * @param delimiter an optional string defining the delimiter, "," is used in case it's empty.
     * @param prefix an optional string defining the prefix, "{" is used in case it's empty.
     * @param suffix an optional string defining the suffix, "}" is used in case it's empty.
     * @param <T> the type of the values
     * @return a delimiter-separated string like having a prefix and s suffix.
     */
    public static <T> String toString(Collection<T> values,
                                      Function<T, String> mapFunction,
                                      Optional<String> delimiter,
                                      Optional<String> prefix,
                                      Optional<String> suffix) {
        return values.stream().map(mapFunction).collect(
                Collectors.joining(delimiter.orElse(","),
                        prefix.orElse("{"),
                        suffix.orElse("}")));
    }

    public static String getTodaysDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd-HHmmss");
        System.out.println();
        return sdf.format(date);
    }

}