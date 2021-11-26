# SPDX-License-Identifier: MIT

FROM alpine:3.15

RUN apk update --no-cache && \
    apk add --no-cache nginx openssl

# Create self-signed certificate
RUN cd /tmp && \
    openssl req \
        -new \
        -newkey rsa:2048 \
        -days 365 \
        -nodes \
        -x509 \
        -subj "/C=DE/ST=BW/L=Stuttgart/O=Loadbalancer/CN=localhost" \
        -keyout localhost.key \
        -out localhost.cert

# Certificates
RUN mkdir -p /certificates && \
    mv /tmp/localhost.cert /certificates/localhost.cert && \
    mv /tmp/localhost.key /certificates/localhost.key

# Generate ephemeral Diffie-Hellman paramaters for perfect forward secrecy
# see: https://raymii.org/s/tutorials/Strong_SSL_Security_On_nginx.html#toc_5
RUN openssl dhparam -out /certificates/certsdhparam.pem 2048

RUN chown -R nginx:nginx /certificates

# Copy configuration script
RUN rm /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Switch from root to non-root user
USER nginx

CMD ["/run.sh"]
