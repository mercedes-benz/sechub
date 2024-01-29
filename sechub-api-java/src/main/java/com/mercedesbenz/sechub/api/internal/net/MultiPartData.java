// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.net;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * This class represents data entries inside a multipart body
 *
 * @author Albert Tregnaghi
 *
 */
class MultiPartData {

    private String name;

    private MultiPartType type;

    private String value;

    private Path path;

    private Supplier<InputStream> stream;

    private String filename;

    private String contentType;

    public static MultiPartData forStringContent(String name, String value) {
        MultiPartData data = new MultiPartData();
        data.name = name;
        data.value = value;
        data.type = MultiPartType.STRING;

        return data;

    }

    public static MultiPartData forFile(String name, Path path) {
        MultiPartData data = new MultiPartData();
        data.name = name;
        data.path = path;
        data.type = MultiPartType.FILE;

        return data;
    }

    public static MultiPartData forStream(String name, Supplier<InputStream> value, String filename, String contentType) {
        MultiPartData data = new MultiPartData();
        data.name = name;
        data.stream = value;
        data.filename = filename;
        data.contentType = contentType;
        data.type = MultiPartType.STREAM;

        return data;
    }

    private MultiPartData() {
    }

    public String getName() {
        return name;
    }

    public MultiPartType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Path getPath() {
        return path;
    }

    public Supplier<InputStream> getStream() {
        return stream;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public static MultiPartData boundary(String boundary) {

        MultiPartData data = new MultiPartData();
        data.type = MultiPartType.BOUNDARY;
        data.value = "--" + boundary + "--";

        return data;
    }

}