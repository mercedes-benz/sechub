#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd "$(dirname "$0")" || exit 1

docker compose --file docker-compose_pds_owasp_zap_external_network.yaml down --remove-orphans
