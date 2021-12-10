# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

# Folders
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"
ENV TOOL_FOLDER="/tools"
ARG WORKSPACE="/workspace"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"
ENV DOWNLOAD_FOLDER="/downloads"

# Other
ENV USER="zap"

# PDS
ENV PDS_VERSION=0.24.0
ARG PDS_CHECKSUM="ecc69561109ee98a57a087fd9e6a4980a38ac72d07467d6c69579c83c16b3255"

# OWASP ZAP
ARG OWASP_ZAP_CHECKSUM="0e0d8198f60dad56b010c5e26c069395af5bee333f4aa9c47f231c767be1f995"
ARG OWASP_ZAP_VERSION=2.11.0

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# Create download folder
RUN mkdir "$DOWNLOAD_FOLDER"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN groupadd --gid 2323 "$USER" \
     && useradd --uid 2323 --no-log-init --create-home --gid "$USER" "$USER"

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-11-jre firefox firefox-geckodriver && \
    apt-get clean

# Install OWASP ZAP
RUN mkdir --parents "$TOOL_FOLDER" && \
	cd "$TOOL_FOLDER" && \
	# download latest release of owasp zap
	wget https://github.com/zaproxy/zaproxy/releases/download/v$OWASP_ZAP_VERSION/zaproxy_$OWASP_ZAP_VERSION-1_all.deb && \
	# verify that the checksum and the checksum of the file are same
    echo "$OWASP_ZAP_CHECKSUM zaproxy_$OWASP_ZAP_VERSION-1_all.deb" | sha256sum --check && \
	dpkg -i zaproxy_$OWASP_ZAP_VERSION-1_all.deb && \
	# remove zaproxy deb package
	rm zaproxy_$OWASP_ZAP_VERSION-1_all.deb
	
# Install the Product Delegation Server (PDS)
RUN mkdir --parents "$PDS_FOLDER" && \
    cd "$PDS_FOLDER" && \
    # create checksum file
    echo "$PDS_CHECKSUM  sechub-pds-$PDS_VERSION.jar" > sechub-pds-$PDS_VERSION.jar.sha256sum && \
    # download pds
    wget "https://github.com/Daimler/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy mock folders
COPY mocks/ "$MOCK_FOLDER"

# Setup scripts
COPY owasp-zap.sh $SCRIPT_FOLDER/owasp-zap.sh
RUN chmod +x $SCRIPT_FOLDER/owasp-zap.sh

COPY owasp-zap-mock.sh $SCRIPT_FOLDER/owasp-zap-mock.sh
RUN chmod +x $SCRIPT_FOLDER/owasp-zap-mock.sh

# OWASP ZAP wrapper
COPY owaspzap-wrapper.jar $TOOL_FOLDER/owaspzap-wrapper.jar

# Create shared volumes and upload dir
RUN mkdir --parents "$SHARED_VOLUME_UPLOAD_DIR"

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Copy zap addon download urls into container
COPY zap-addons.txt $TOOL_FOLDER/zap-addons.txt

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive "$USER:$USER" $TOOL_FOLDER $SCRIPT_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# Switch from root to non-root user
USER "$USER"

# Install OWASP ZAP addons
# see: https://www.zaproxy.org/addons/
# via addon manager: owasp-zap -cmd -addoninstall webdriverlinux

RUN mkdir --parents "/home/$USER/.ZAP/plugin" && \
    cd "/home/$USER/.ZAP/plugin" && \
    wget --input-file="$TOOL_FOLDER/zap-addons.txt"

COPY reports-release-0.10.0.zap /home/$USER/.ZAP/plugin/reports-release-0.10.0.zap

CMD ["/run.sh"]
