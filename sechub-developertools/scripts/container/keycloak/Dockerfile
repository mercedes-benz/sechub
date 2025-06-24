FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
# Install curl for health check
RUN dnf install --installroot /mnt/rootfs curl --releasever 9 --setopt install_weak_deps=false --nodocs -y && \
    dnf --installroot /mnt/rootfs clean all && \
    rpm --root /mnt/rootfs -e --nodeps setup

FROM quay.io/keycloak/keycloak:26.2.5
COPY --from=ubi-micro-build /mnt/rootfs /

USER root

COPY run.sh /opt/keycloak/bin/
RUN chmod +x /opt/keycloak/bin/run.sh

COPY sechub-int-keycloak-realm.json /opt/keycloak/data/import/realm-export.json

USER keycloak

ENTRYPOINT ["/opt/keycloak/bin/run.sh"]
