# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub OWASP ZAP + PDS Image"
LABEL org.opencontainers.image.description="A container which combines OWASP ZAP with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Build args
ARG OWASPZAP_VERSION="2.14.0"
ARG OWASPZAP_SHA256SUM="219d7f25bbe25247713805ab02cc12279898c870743c1aae3c2b0b1882191960"

ARG OWASPZAP_WRAPPER_VERSION="1.3.1"

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
    apt-get install --assume-yes openjdk-17-jre firefox-esr wget && \
    apt-get clean

# Download ZAP
RUN cd "$DOWNLOAD_FOLDER" && \
	# download latest release of zap
	wget --no-verbose https://github.com/zaproxy/zaproxy/releases/download/v${OWASPZAP_VERSION}/ZAP_${OWASPZAP_VERSION}_Linux.tar.gz && \
	# verify that the checksum and the checksum of the file are same
    echo "${OWASPZAP_SHA256SUM} ZAP_${OWASPZAP_VERSION}_Linux.tar.gz" | sha256sum --check && \
    # install ZAP
	tar xf ZAP_${OWASPZAP_VERSION}_Linux.tar.gz -C "$TOOL_FOLDER" && \
	ln -s "$TOOL_FOLDER/ZAP_${OWASPZAP_VERSION}/zap.sh" "/usr/local/bin/zap" && \
	# remove plugins installed on default
	rm $TOOL_FOLDER/ZAP_${OWASPZAP_VERSION}/plugin/*.zap

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

# Install OWASP ZAP addons
# see: https://www.zaproxy.org/addons/
# via addon manager: owasp-zap -cmd -addoninstall webdriverlinux
RUN mkdir --parents "/home/$USER/.ZAP/plugin" && \
    chown --recursive "$USER:$USER" "/home/$USER/" && \
    cd "/home/$USER/.ZAP/plugin" && \
    wget --no-verbose --input-file="$TOOL_FOLDER/zap-addons.txt"

# Switch from root to non-root user
USER "$USER"

# Switch to workspace folder
WORKDIR "$WORKSPACE"
