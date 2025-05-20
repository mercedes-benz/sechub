# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Inject the target architecture
# For more information:
# - https://docs.docker.com/engine/reference/builder/#automatic-platform-args-in-the-global-scope
ARG TARGETARCH

# Build args
ARG BUILD_TYPE="download"

ARG SECHUB_VERSION="0.35.2"
ARG TAG=""
ARG BRANCH=""

ARG GO="go1.21.6.linux-${TARGETARCH}.tar.gz"

# possible values: temurin, openj9, openjdk
ARG JAVA_DISTRIBUTION="temurin"
# possible values: 17
ARG JAVA_VERSION="17"

# Artifact folder
ARG SECHUB_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
ARG GO
ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_VERSION
ARG JAVA_DISTRIBUTION
ARG TAG
ARG BRANCH

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"

ENV DOWNLOAD_FOLDER="/downloads"
ENV PATH="/usr/local/go/bin:$PATH"

RUN echo "Builder: Build"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN microdnf update --assumeyes && \
    microdnf install --assumeyes epel-release && \
    microdnf install --assumeyes wget w3m git tar findutils which && \
    microdnf clean all

# Install Go
RUN cd "$DOWNLOAD_FOLDER" && \
    # Get checksum from Go download site
    GO_CHECKSUM=`w3m https://go.dev/dl/ | grep "$GO" | tail -1 | awk '{print $6}'` && \
    # create checksum file
    echo "$GO_CHECKSUM $GO" > "$GO.sha256sum" && \
    # download Go
    wget --no-verbose https://go.dev/dl/"${GO}" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "$GO.sha256sum" && \
    # extract Go
    tar --extract --file "$GO" --directory /usr/local/ && \
    # remove go tar.gz
    rm "$GO"

COPY --chmod=755 install-java/rocky "$DOWNLOAD_FOLDER/install-java/"

# Install Java
RUN cd "$DOWNLOAD_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" "jdk"

# Copy clone script
COPY --chmod=755 clone.sh "$BUILD_FOLDER/clone.sh"

# Build SecHub
RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    # execute the clone script
    ./clone.sh "$GIT_URL" "$BRANCH" "$TAG" && \
    cd "sechub" && \
    # Java version
    java --version && \
    java_location=$( which java ) && \
    export JAVA_HOME=$( readlink -f "$java_location" | sed "s:/bin/java::" ) && \
    # Build SecHub
    "./buildExecutables" && \
    cp sechub-server/build/libs/sechub-server-*.jar --target-directory "$SECHUB_ARTIFACT_FOLDER"

#-------------------
# Builder Download
#-------------------

FROM ${BASE_IMAGE} AS builder-download

ARG SECHUB_ARTIFACT_FOLDER
ARG SECHUB_VERSION

RUN echo "Builder: Download"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

RUN microdnf update --assumeyes && \
    microdnf install --assumeyes wget && \
    microdnf clean all

# Download the SecHub server
RUN cd "$SECHUB_ARTIFACT_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECHUB_VERSION-server/sechub-server-$SECHUB_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECHUB_VERSION-server/sechub-server-$SECHUB_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "sechub-server-$SECHUB_VERSION.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG SECHUB_ARTIFACT_FOLDER
ARG SECHUB_VERSION

RUN echo "Builder: Copy"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-server-*.jar "$SECHUB_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder

#-------------------
# SecHub Server Image
#-------------------

FROM ${BASE_IMAGE} AS sechub

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Rocky Linux Image"
LABEL org.opencontainers.image.description="A container for SecHub based on Rocky Linux"
LABEL maintainer="SecHub FOSS Team"

ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_DISTRIBUTION
ARG JAVA_VERSION

# env vars in container
ENV USER="sechub"
ENV UID="7474"
ENV GID="${UID}"
ENV LANG="C.UTF-8"
ENV SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR="/shared_volumes/uploads"

ARG SECHUB_FOLDER="/sechub"

# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Mounted secret files (like e.g. SSL certificates) go to $SECHUB_FOLDER/secrets. See deployment.yaml file.
RUN mkdir --parent "$SECHUB_FOLDER/secrets" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
COPY --from=builder "$SECHUB_ARTIFACT_FOLDER" "$SECHUB_FOLDER"

# Copy run script into container and make it executable
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Upgrade system
RUN microdnf update --assumeyes && \
    microdnf clean all

COPY --chmod=755 install-java/rocky/ "$SECHUB_FOLDER/install-java/"

# Install Java
RUN cd "$SECHUB_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jre

# Set permissions and clean up install folder
RUN chown --recursive "$USER:$USER" "$SECHUB_FOLDER" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR" && \
    rm --recursive --force "$SECHUB_FOLDER/install-java/"

# Set workspace
WORKDIR "$SECHUB_FOLDER"

# Switch to non-root user
USER "$USER"

CMD ["/run.sh"]
