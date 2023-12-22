#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

cd "$(dirname "$0")" || exit 1

docker compose --file docker-compose_pds_iac_external-network.yaml down --remove-orphans