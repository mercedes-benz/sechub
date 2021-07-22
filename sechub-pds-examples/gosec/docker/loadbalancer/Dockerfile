# SPDX-License-Identifier: MIT

FROM alpine:3.14

RUN apk update && \
    apk add nginx && \
    apk add openssl

# Create self-signed certificate
RUN cd /tmp && \
    openssl req \
        -new \
        -newkey rsa:4096 \
        -days 365 \
        -nodes \
        -x509 \
        -subj "/C=DE/ST=BW/L=Stuttgart/O=Loadbalancer/CN=localhost" \
        -keyout localhost.key \
        -out localhost.cert

RUN mv /tmp/localhost.cert /etc/ssl/certs/localhost.cert && \
    mv /tmp/localhost.key /etc/ssl/private/localhost.key

# copy configuration script
RUN rm /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# switch from root to non-root user
#USER nginx

CMD ["/run.sh"]