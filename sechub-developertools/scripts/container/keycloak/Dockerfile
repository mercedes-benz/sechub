FROM quay.io/keycloak/keycloak:26.2.5

USER root

COPY run.sh /opt/keycloak/bin/
COPY KeycloakAvailabilityChecker.java /opt/keycloak/bin/
RUN chmod +x /opt/keycloak/bin/run.sh

COPY sechub-int-keycloak-realm.json /opt/keycloak/data/import/realm-export.json

USER keycloak

ENTRYPOINT ["/opt/keycloak/bin/run.sh"]
