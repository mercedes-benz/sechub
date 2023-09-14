#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""

echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

# login to JFROG artifactory
skopeo login $ARTIFACTORY -u $XRAY_USERNAME -p $XRAY_PASSWORD --authfile $WORKSPACE/auth.json

# Get docker archives from source folder and process
TAR_FILES=$(find $PDS_JOB_EXTRACTED_SOURCES_FOLDER -type f -name "*.tar")
for UPLOAD_FILE in $TAR_FILES # TAR_FILES
do
  echo "--------"
  echo "Processing ${UPLOAD_FILE}"
  echo "---------"

  # get image name, tag
  skopeo list-tags docker-archive:$UPLOAD_FILE > $WORKSPACE/tags.json
  IMAGE=$(jq '.Tags[0]' $WORKSPACE/tags.json | tr -d \")

  # upload image as docker image to artifactory
  skopeo copy "docker-archive:${UPLOAD_FILE}" "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile $WORKSPACE/auth.json

  # get checksum vom docker image
  skopeo inspect "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile $WORKSPACE/auth.json > $WORKSPACE/inspect.json
  SHA256=$(jq '.Digest' $WORKSPACE/inspect.json | tr -d \")

  # run as jar
  java -jar $TOOL_FOLDER/wrapperxray.jar "--image" $IMAGE "--sha256" $SHA256
done

