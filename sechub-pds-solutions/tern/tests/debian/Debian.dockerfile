FROM debian:11-slim

ENV DATA="/data"

COPY data "$DATA"

COPY shared/run.sh /run.sh
RUN chmod +x /run.sh

CMD ["/run.sh"]