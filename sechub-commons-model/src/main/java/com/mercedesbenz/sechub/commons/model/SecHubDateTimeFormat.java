package com.mercedesbenz.sechub.commons.model;

import java.time.format.DateTimeFormatter;

/**
 * Inside SecHub we want an explicit date time format which is stable, defined
 * and is not influenced by the behavior of an external library. Jackson did
 * change its default serialization behaviour for date time objects from ISO
 * 8601 to an array based variant between two versions. We ware a long time not
 * aware about this situation, but after a while this lead to failures/problems,
 * which we want to avoid.
 *
 * This is the reason why we define an explicit format here. We use UTC as time
 * zone and use a ISO 8601 pattern to keep it human readable.
 *
 * The serialization ({@link LocalDateTimeSserializer}) and the deserialization
 * ({@link SecHubLocalDateTimeDeserializer}) are able to handle this pattern.
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubDateTimeFormat {

    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.n'Z'";

    /**
     * A formatter which uses the pattern: {@value #PATTERN}
     */
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);

}
