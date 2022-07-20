# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL maintainer="SecHub FOSS Team"

# Build args
ARG OWASPZAP_VERSION="2.11.1"
ARG OWASPZAP_CHECKSUM="abbfe9ad057b3511043a0f0317d5f91d914145ada5b102a5708f8af6a5e191f8"
ARG PDS_VERSION="0.30.0"

ARG JAVA_VERSION="11"
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"
ARG USER="zap"
ARG WORKSPACE="/workspace"

# Environment vars
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="${SCRIPT_FOLDER}/mocks"
ENV PDS_VERSION="${PDS_VERSION}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="${SHARED_VOLUMES}/uploads"
ENV TOOL_FOLDER="/tools"

# non-root user
# using fixed group and user ids
# zap needs a home directory for the plugins
RUN groupadd --gid 2323 "$USER" \
     && useradd --uid 2323 --no-log-init --create-home --gid "$USER" "$USER"

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "${SCRIPT_FOLDER}" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" "/home/$USER/.ZAP/plugin"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-${JAVA_VERSION}-jre firefox-esr && \
    apt-get clean

# Install OWASP ZAP
RUN cd "$TOOL_FOLDER" && \
	# download latest release of owasp zap
	wget --no-verbose https://github.com/zaproxy/zaproxy/releases/download/v${OWASPZAP_VERSION}/zaproxy_${OWASPZAP_VERSION}-1_all.deb && \
	# verify that the checksum and the checksum of the file are same
    echo "${OWASPZAP_CHECKSUM} zaproxy_${OWASPZAP_VERSION}-1_all.deb" | sha256sum --check && \
	dpkg -i zaproxy_${OWASPZAP_VERSION}-1_all.deb && \
	# remove zaproxy deb package
	rm zaproxy_${OWASPZAP_VERSION}-1_all.deb
	

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Install SecHub OWASP ZAP wrapper
RUN cd "$TOOL_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-wrapperowaspzap-$PDS_VERSION.jar.sha256sum" && \
    # download wrapper jar
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-wrapperowaspzap-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-wrapperowaspzap-$PDS_VERSION.jar.sha256sum && \
    ln -s sechub-pds-wrapperowaspzap-$PDS_VERSION.jar wrapperowaspzap.jar
    
# Copy default full ruleset file
COPY owasp-zap-full-ruleset-all-release-status.json ${TOOL_FOLDER}/owasp-zap-full-ruleset-all-release-status.json

# Copy mock folders
COPY mocks/ "$MOCK_FOLDER"

# Setup scripts
COPY owasp-zap.sh ${SCRIPT_FOLDER}/owasp-zap.sh
COPY owasp-zap-mock.sh ${SCRIPT_FOLDER}/owasp-zap-mock.sh

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy zap addon download urls into container
COPY zap-addons.txt "$TOOL_FOLDER/zap-addons.txt"

# Copy run script into container
COPY run.sh /run.sh

# Make scripts executable
RUN chmod +x ${SCRIPT_FOLDER}/owasp-zap.sh ${SCRIPT_FOLDER}/owasp-zap-mock.sh /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive "$USER:$USER" $TOOL_FOLDER ${SCRIPT_FOLDER} $WORKSPACE $PDS_FOLDER ${SHARED_VOLUMES} /run.sh /home/$USER/.ZAP

# Switch from root to non-root user
USER "$USER"

# Install OWASP ZAP addons
# see: https://www.zaproxy.org/addons/
# via addon manager: owasp-zap -cmd -addoninstall webdriverlinux
RUN cd "/home/$USER/.ZAP/plugin" && \
    wget --no-verbose --input-file="$TOOL_FOLDER/zap-addons.txt"

CMD ["/run.sh"]
