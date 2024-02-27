#!/usr/bin/bash
# SPDX-License-Identifier: MIT

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""
echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

SKOPEO_AUTH="auth.json"
UPLOAD_DIR=$PDS_JOB_EXTRACTED_BINARIES_FOLDER
REGISTRY=$XRAY_DOCKER_REGISTRY

check_valid_upload () {
  # count number of files/ folders uploaded and checks number of uploads greater equals 2
  if [ $(ls $UPLOAD_DIR | wc --lines) -ge 2 ]
  then
    echo "Error: more than one file was uploaded: $(ls $UPLOAD_DIR)"
    exit 1
  fi
}

login_into_artifactory () {
  # login to JFROG artifactory
  LOGIN=$(skopeo login "$XRAY_ARTIFACTORY" --username "$XRAY_USERNAME" --password "$XRAY_PASSWORD" --authfile "$PDS_JOB_WORKSPACE_LOCATION/$SKOPEO_AUTH")
  if [ "$LOGIN" != "Login Succeeded!" ]
  then
    echo "Error: Skopeo could not login to $XRAY_ARTIFACTORY with user $XRAY_USERNAME"
    exit 1
  fi
}

# main program
check_valid_upload
login_into_artifactory
cd "$PDS_JOB_WORKSPACE_LOCATION"

# Get docker archives from binary upload folder
TAR_FILES=$(find $UPLOAD_DIR -type f -name "*.tar")

# Get first element in list
UPLOAD_FILE=$(echo $TAR_FILES | cut --delimiter=" " --fields=1)

echo "---------"
echo "Processing Xray upload and docker scan for file ${UPLOAD_FILE}"
echo "---------"

# get image name and tag
skopeo list-tags "docker-archive:$UPLOAD_FILE" > "$PDS_JOB_WORKSPACE_LOCATION/tags.json"
IMAGE=$(jq '.Tags[0]' "$PDS_JOB_WORKSPACE_LOCATION/tags.json"| tr --delete \")

# copy local docker archive as docker image to artifactory register
skopeo copy "docker-archive:${UPLOAD_FILE}" "docker://${XRAY_ARTIFACTORY}/${REGISTRY}/${IMAGE}" --authfile "$PDS_JOB_WORKSPACE_LOCATION/$SKOPEO_AUTH"

# get checksum from the docker image
skopeo inspect "docker://${XRAY_ARTIFACTORY}/${REGISTRY}/${IMAGE}" --authfile "$PDS_JOB_WORKSPACE_LOCATION/auth.json" > "$PDS_JOB_WORKSPACE_LOCATION/inspect.json"
SHA256=$(jq '.Digest' "$PDS_JOB_WORKSPACE_LOCATION/inspect.json" | tr --delete \")

java -jar "$TOOL_FOLDER/wrapper-xray.jar" "--name" "$IMAGE" "--checksum" "$SHA256" "--scantype" "docker" "--outputfile" "$PDS_JOB_RESULT_FILE" "--workspace" "$PDS_JOB_WORKSPACE_LOCATION"

# SPDX report can be returned as followed - SPDX does not contain vulnerabilities
cp "$PDS_JOB_WORKSPACE_LOCATION/XrayArtifactoryReports/"*SPDX.json "$PDS_JOB_RESULT_FILE"