# SPDX-License-Identifier: MIT

ARG FILE_NAME="apache2.go"

# Builder
FROM debian:12-slim as builder

ARG FILE_NAME

RUN apt update && \
    apt install --assume-yes golang-go

RUN mkdir /build
COPY data/"$FILE_NAME" /build/"$FILE_NAME"

RUN ls /build

RUN cd /build && \
    go build -o "apache2" "$FILE_NAME" && \
    chmod +x "apache2"

# Minimal App
FROM scratch

ARG FILE_NAME

COPY --from=builder /build/apache2 .

# Using a variable is not possible,
# because Shell expansion does not work in this case
# there is no shell in a `scratch` environment
#
# - https://docs.docker.com/engine/reference/builder/#cmd
# - https://github.com/moby/moby/issues/5509
CMD ["./apache2"]