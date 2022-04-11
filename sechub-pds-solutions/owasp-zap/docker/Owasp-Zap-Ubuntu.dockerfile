# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE

ARG SECHUB_ARTIFACT_FOLDER="/artifacts"
ARG JAVA_VERSION="11"

#--------------------------
# Build OWASP ZAP Wrapper
#--------------------------

FROM ${BASE_IMAGE} AS builder-zap-wrapper

ARG JAVA_VERSION
ARG SECHUB_ARTIFACT_FOLDER

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --quiet --assume-yes git "openjdk-$JAVA_VERSION-jdk-headless" && \
    apt-get clean

RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    git clone "$GIT_URL" && \
    cd "sechub" && \
    ./gradlew buildWrapperOwaspZap && \
    mkdir --parent "$SECHUB_ARTIFACT_FOLDER" && \
    ls "sechub-wrapper-owasp-zap/build/libs/" && \
    cp "sechub-wrapper-owasp-zap/build/libs/wrapperowaspzap-0.0.0.jar" --target-directory "$SECHUB_ARTIFACT_FOLDER" && \
    mv "$SECHUB_ARTIFACT_FOLDER/wrapperowaspzap-0.0.0.jar" "$SECHUB_ARTIFACT_FOLDER/wrapperowaspzap.jar"
   
#--------------------------
# OWASP ZAP + PDS
#--------------------------

FROM ${BASE_IMAGE}

ARG SECHUB_ARTIFACT_FOLDER

# Folders
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"
ENV TOOL_FOLDER="/tools"
ARG WORKSPACE="/workspace"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"
ENV DOWNLOAD_FOLDER="/downloads"

# Other
ARG USER="zap"

# PDS
ENV PDS_VERSION=0.27.0

# OWASP ZAP
ARG OWASP_ZAP_CHECKSUM="abbfe9ad057b3511043a0f0317d5f91d914145ada5b102a5708f8af6a5e191f8"
ARG OWASP_ZAP_VERSION=2.11.1

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR"

# non-root user
# using fixed group and user ids
# zap needs a home directory for the plugins
RUN groupadd --gid 2323 "$USER" \
     && useradd --uid 2323 --no-log-init --create-home --gid "$USER" "$USER"

RUN apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-11-jre firefox firefox-geckodriver && \
    apt-get clean

# Install OWASP ZAP
RUN cd "$TOOL_FOLDER" && \
	# download latest release of owasp zap
	wget --no-verbose https://github.com/zaproxy/zaproxy/releases/download/v$OWASP_ZAP_VERSION/zaproxy_$OWASP_ZAP_VERSION-1_all.deb && \
	# verify that the checksum and the checksum of the file are same
    echo "$OWASP_ZAP_CHECKSUM zaproxy_$OWASP_ZAP_VERSION-1_all.deb" | sha256sum --check && \
	dpkg -i zaproxy_$OWASP_ZAP_VERSION-1_all.deb && \
	# remove zaproxy deb package
	rm zaproxy_$OWASP_ZAP_VERSION-1_all.deb
	

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
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
COPY --from=builder-zap-wrapper "$SECHUB_ARTIFACT_FOLDER" "$TOOL_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy zap addon download urls into container
COPY zap-addons.txt "$TOOL_FOLDER/zap-addons.txt"

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive "$USER:$USER" $TOOL_FOLDER $SCRIPT_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# Create OWASP ZAP plugin folder
RUN mkdir --parents "/home/$USER/.ZAP/plugin"
RUN chown --recursive "$USER:$USER" /home/$USER/.ZAP

# Switch from root to non-root user
USER "$USER"

# Install OWASP ZAP addons
# see: https://www.zaproxy.org/addons/
# via addon manager: owasp-zap -cmd -addoninstall webdriverlinux
RUN cd "/home/$USER/.ZAP/plugin" && \
    wget --no-verbose --input-file="$TOOL_FOLDER/zap-addons.txt"

CMD ["/run.sh"]
