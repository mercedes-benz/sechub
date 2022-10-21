# Builder
FROM debian:11-slim as builder

RUN apt update && \
    apt install --assume-yes gcc

RUN mkdir /build 
COPY data/hello_world.c /build/hello_world.c

RUN ls /build

RUN cd /build && \
    gcc -o hello_world -static hello_world.c && \
    chmod +x hello_world

# Minimal App 
FROM scratch
COPY --from=builder /build/hello_world .
CMD ["./hello_world"]