# SPDX-License-Identifier: MIT

FROM debian:12-slim

ARG PDS_SOLUTION
ARG USER=www-data

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install nginx openssl sed && \
    apt-get --assume-yes clean

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

# Create PID file and set permissions
RUN touch /var/run/nginx.pid && \
    chown -R "$USER:$USER" /certificates /var/log/nginx /var/lib/nginx /etc/nginx/conf.d /var/run/nginx.pid

# Copy configuration script
COPY nginx.conf /etc/nginx/nginx.conf
RUN sed -i "s/y_pds-solution_y/${PDS_SOLUTION}/g" /etc/nginx/nginx.conf

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
