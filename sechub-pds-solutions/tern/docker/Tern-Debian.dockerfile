# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# See: https://github.com/tern-tools/tern/releases
ARG TERN_VERSION
# See: https://github.com/nexB/scancode-toolkit/releases
ARG SCANCODE_VERSION

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Tern+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Tern with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get --quiet update && \
    apt-get --quiet --assume-yes upgrade && \
    apt-get --quiet --assume-yes install attr bzip2 git jq libgomp1 libpopt0 libxml2-dev libxslt1-dev procps python3 python3-distutils python3-pip skopeo tar wget xz-utils zlib1g && \
    apt-get --quiet --assume-yes clean

# python-dev

# Install Tern + Scancode
# the constraint makes sure exactly the requiered packages are installed
# https://peps.python.org/pep-0668/[PEP 668 – Marking Python base environments as “externally managed”]
# wants to prevent developers from mixing Python Package Index (PyPI) packages with Debian packages.
# Interesting idea, but not as useful inside a container, which in essence is already a virtual environment.
# Use `--break-system-packages` to let the Python package manager `pip` mix packages from Debian and Python
RUN pip install --break-system-packages  --no-warn-script-location "tern==${TERN_VERSION}" "scancode-toolkit[full]==${SCANCODE_VERSION}"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"
