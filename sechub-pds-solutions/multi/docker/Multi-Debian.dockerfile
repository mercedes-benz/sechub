# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Multiple Tools + PDS Image"
LABEL org.opencontainers.image.description="A container which combines Multiple Tools with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Mock folder
COPY mocks "$MOCK_FOLDER"

# Update image and install dependencies
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install sed wget pip git && \
    apt-get --assume-yes clean

# Install Flawfinder, Bandit, njsscan and mobsfscan
COPY packages.txt $TOOL_FOLDER/packages.txt

# https://peps.python.org/pep-0668/[PEP 668 – Marking Python base environments as “externally managed”]
# wants to prevent developers from mixing Python Package Index (PyPI) packages with Debian packages.
# Interesting idea, but not as useful inside a container, which in essence is already a virtual environment.
# Use `--break-system-packages` to let the Python package manager `pip` mix packages from Debian and Python
RUN pip install --break-system-packages -r $TOOL_FOLDER/packages.txt
# pip --editable option allows for installing from VCS Urls
# https://pip.pypa.io/en/stable/cli/pip_install/#cmdoption-e
# It is possible to specify a GIT ref in a VCS Url using @ delimeter
# https://pip.pypa.io/en/stable/topics/vcs-support/
RUN pip install --break-system-packages --editable "git+https://github.com/alexdd/bandit-sarif-formatter@main#egg=bandit-sarif-formatter"

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
