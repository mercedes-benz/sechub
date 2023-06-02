# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub OWASP ZAP + PDS Image"
LABEL org.opencontainers.image.description="A container which combines OWASP ZAP with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Build args
ARG OWASPZAP_VERSION="2.12.0"
ARG OWASPZAP_SHA256SUM="7eaf340d9fcc42576c7a5572249fe0bcad6e7acd68098a7ca110e64beab46207"

ARG OWASPZAP_WRAPPER_VERSION="1.0.0"

# OWASP ZAP host and port
ENV ZAP_HOST="127.0.0.1"
ENV ZAP_PORT="8080"

USER root

# Copy mock folders
COPY mocks "$MOCK_FOLDER"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-11-jre firefox-esr && \
    apt-get clean

# Install OWASP ZAP
RUN cd "$DOWNLOAD_FOLDER" && \
	# download latest release of owasp zap
	wget --no-verbose https://github.com/zaproxy/zaproxy/releases/download/v${OWASPZAP_VERSION}/zaproxy_${OWASPZAP_VERSION}-1_all.deb && \
	# verify that the checksum and the checksum of the file are same
    echo "${OWASPZAP_SHA256SUM} zaproxy_${OWASPZAP_VERSION}-1_all.deb" | sha256sum --check && \
	dpkg -i zaproxy_${OWASPZAP_VERSION}-1_all.deb && \
	# remove zaproxy deb package
	rm zaproxy_${OWASPZAP_VERSION}-1_all.deb


# Install SecHub OWASP ZAP wrapper
RUN cd "$TOOL_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$OWASPZAP_WRAPPER_VERSION-owaspzap-wrapper/sechub-pds-wrapperowaspzap-$OWASPZAP_WRAPPER_VERSION.jar.sha256sum" && \
    # download wrapper jar
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$OWASPZAP_WRAPPER_VERSION-owaspzap-wrapper/sechub-pds-wrapperowaspzap-$OWASPZAP_WRAPPER_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-wrapperowaspzap-$OWASPZAP_WRAPPER_VERSION.jar.sha256sum && \
    ln -s sechub-pds-wrapperowaspzap-$OWASPZAP_WRAPPER_VERSION.jar wrapperowaspzap.jar

# Copy default full ruleset file
COPY owasp-zap-full-ruleset-all-release-status.json ${TOOL_FOLDER}/owasp-zap-full-ruleset-all-release-status.json

# Copy zap addon download urls into container
COPY zap-addons.txt "$TOOL_FOLDER/zap-addons.txt"

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"

# Install OWASP ZAP addons
# see: https://www.zaproxy.org/addons/
# via addon manager: owasp-zap -cmd -addoninstall webdriverlinux
RUN mkdir --parents "/home/$USER/.ZAP/plugin" && \
    cd "/home/$USER/.ZAP/plugin" && \
    wget --no-verbose --input-file="$TOOL_FOLDER/zap-addons.txt"
