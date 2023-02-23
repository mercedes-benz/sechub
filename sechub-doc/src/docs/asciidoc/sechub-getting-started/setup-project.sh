export SECHUB_SERVER=https://localhost:8443
export SECHUB_USERID=admin
export SECHUB_APITOKEN='myTop$ecret!'
export SECHUB_TRUSTALL=true
export PATH="$PATH:`pwd`/sechub-cli/build/go/platform/linux-amd64:`pwd`/sechub-developertools/scripts"

./sechub-solution/setup-pds/setup-gosec.sh
