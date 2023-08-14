// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Supplier;

public class MultiPartBodyPublisherBuilder {
    private List<MultiPartData> multiPartData = new ArrayList<>();

    private String boundary;

    public MultiPartBodyPublisherBuilder() {
        boundary = UUID.randomUUID().toString();
    }

    public HttpRequest.BodyPublisher build() {
        if (multiPartData.size() == 0) {
            throw new IllegalStateException("Must have at least one part to build a multipart message.");
        }
        /* at the end add the final boundary */
        multiPartData.add(MultiPartData.boundary(boundary));

        return HttpRequest.BodyPublishers.ofByteArrays(MultiPartIterator::new);
    }

    public String getBoundary() {
        return boundary;
    }

    public MultiPartBodyPublisherBuilder addString(String name, String value) {
        multiPartData.add(MultiPartData.forStringContent(name, value));
        return this;
    }

    public MultiPartBodyPublisherBuilder addFile(String name, Path path) {
        multiPartData.add(MultiPartData.forFile(name, path));
        return this;
    }

    public MultiPartBodyPublisherBuilder addStream(String name, Supplier<InputStream> value, String filename, String contentType) {
        multiPartData.add(MultiPartData.forStream(name, value, filename, contentType));
        return this;
    }

    class MultiPartIterator implements Iterator<byte[]> {

        private static final int BUFER_SIZE = 8192;
        private Iterator<MultiPartData> iter;
        private InputStream inputStream;

        private boolean done;
        private byte[] next;

        MultiPartIterator() {
            iter = multiPartData.iterator();
        }

        @Override
        public boolean hasNext() {
            if (done) {
                return false;
            }
            if (next != null) {
                return true;
            }
            try {
                next = computeNext();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (next == null) {
                done = true;
                return false;
            }
            return true;
        }

        @Override
        public byte[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            byte[] res = next;
            next = null;
            return res;
        }

        private byte[] computeNext() throws IOException {
            if (inputStream == null) {
                if (!iter.hasNext()) {
                    return null;
                }
                MultiPartData nextPart = iter.next();

                if (MultiPartType.STRING.equals(nextPart.getType())) {
                    String part = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=" + nextPart.getName() + "\r\n"
                            + "Content-Type: text/plain; charset=UTF-8\r\n\r\n" + nextPart.getValue() + "\r\n";
                    return part.getBytes(StandardCharsets.UTF_8);
                }
                if (MultiPartType.BOUNDARY.equals(nextPart.getType())) {
                    return nextPart.getValue().getBytes(StandardCharsets.UTF_8);
                }

                String filename;
                String contentType;

                if (MultiPartType.FILE.equals(nextPart.getType())) {
                    Path path = nextPart.getPath();
                    filename = path.getFileName().toString();
                    contentType = Files.probeContentType(path);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    inputStream = Files.newInputStream(path);
                } else {
                    filename = nextPart.getFilename();
                    contentType = nextPart.getContentType();
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    inputStream = nextPart.getStream().get();
                }
                String partHeader = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=" + nextPart.getName() + "; filename=" + filename + "\r\n"
                        + "Content-Type: " + contentType + "\r\n\r\n";
                return partHeader.getBytes(StandardCharsets.UTF_8);
            } else {
                byte[] buffer = new byte[BUFER_SIZE];

                int amountOfBytesRead = inputStream.read(buffer);

                if (amountOfBytesRead > 0) {
                    byte[] actualBytes = new byte[amountOfBytesRead];
                    System.arraycopy(buffer, 0, actualBytes, 0, amountOfBytesRead);

                    return actualBytes;

                } else {
                    /* nothing read - no more data available */
                    inputStream.close();
                    inputStream = null;

                    return "\r\n".getBytes(StandardCharsets.UTF_8);
                }
            }
        }
    }
}
