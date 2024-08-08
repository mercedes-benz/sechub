# SPDX-License-Identifier: MIT

ARG BASE_IMAGE
FROM ${BASE_IMAGE}

#  SCANCODE_VERSION: see https://github.com/nexB/scancode-toolkit/releases Use the version number only
ARG SCANCODE_VERSION
#  SPDX_TOOL_VERSION: see https://mvnrepository.com/artifact/org.spdx/tools-java
ARG SPDX_TOOL_VERSION

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Scancode-Toolkit+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Scancode-Toolkit with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Environment variables in container
ENV SCANCODE_VERSION="${SCANCODE_VERSION}"
ENV SPDX_TOOL_VERSION="${SPDX_TOOL_VERSION}"

USER root

# Copy run_additional script
COPY run_additional.sh /run_additional.sh
RUN chmod +x /run_additional.sh

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Update image and install dependencies
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install bzip2 libgomp1 libpopt0 libxml2-dev libxslt1-dev procps python3-dev python3 python3-distutils python3-pip tar tree wget xz-utils zlib1g libbz2-1.0 &&\
    apt-get --assume-yes clean

# Install Scancode
# the constraint makes sure exactly the requiered packages are installed
# https://peps.python.org/pep-0668/[PEP 668 – Marking Python base environments as “externally managed”]
# wants to prevent developers from mixing Python Package Index (PyPI) packages with Debian packages.
# Interesting idea, but not as useful inside a container, which in essence is already a virtual environment.
# Use `--break-system-packages` to let the Python package manager `pip` mix packages from Debian and Python
RUN pip install --break-system-packages --constraint "https://raw.githubusercontent.com/nexB/scancode-toolkit/v${SCANCODE_VERSION}/requirements.txt" "scancode-toolkit[full]==${SCANCODE_VERSION}"

# Install SPDX Tools Java converter
RUN cd "$TOOL_FOLDER" && \
    # download SPDX Tools Java
    wget --no-verbose "https://repo1.maven.org/maven2/org/spdx/tools-java/${SPDX_TOOL_VERSION}/tools-java-${SPDX_TOOL_VERSION}-jar-with-dependencies.jar" && \
    # download SHA1 checksum for SPDX Tools Java
    spdx_tool_sha1sum=$( wget --quiet --output-document=- https://repo1.maven.org/maven2/org/spdx/tools-java/${SPDX_TOOL_VERSION}/tools-java-${SPDX_TOOL_VERSION}-jar-with-dependencies.jar.sha1 ) && \
    # check against checksum
    echo "${spdx_tool_sha1sum} tools-java-${SPDX_TOOL_VERSION}-jar-with-dependencies.jar" | sha1sum -c

# Patch Scancode to avoid timeout bug: https://github.com/nexB/scancode-toolkit/issues/2908
COPY pool.py /usr/local/lib/python3.9/dist-packages/scancode/pool.py

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
