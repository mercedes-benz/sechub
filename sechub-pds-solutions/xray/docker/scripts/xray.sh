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

SKOPEO_AUTH="auth.json"
# UPLOAD_DIR=$PDS_JOB_EXTRACTED_BINARIES_FOLDER
UPLOAD_DIR=$PDS_JOB_EXTRACTED_SOURCES_FOLDER

check_valid_upload () {
  if [ $(ls $UPLOAD_DIR | wc -l) -ge 2 ]
  then
    echo "Error: more than one file was uploaded: $(ls $UPLOAD_DIR)"
    exit 1
  fi
}

skopeo_login () {
  # login to JFROG artifactory
  LOGIN=$(skopeo login "$ARTIFACTORY" --username "$XRAY_USERNAME" --password "$XRAY_PASSWORD" --authfile "$WORKSPACE/$SKOPEO_AUTH")
  if [ "$LOGIN" = "Login Succeeded!" ]
  then
    continue
  else
    echo "Error: Skopeo could not login to $ARTIFACTORY with user $XRAY_USERNAME"
    exit 1
  fi
}

clean_workspace () {
  rm -rf "$WORKSPACE/*"
  rm -rf "$UPLOAD_DIR/*"
}

check_valid_upload
skopeo_login

# Get docker archives from source folder
TAR_FILES=$(find $UPLOAD_DIR -type f -name "*.tar")
for UPLOAD_FILE in $TAR_FILES # TAR_FILES
do
  echo "---------"
  echo "Processing Xray scan for ${UPLOAD_FILE}"
  echo "---------"

  # get image name and tag
  skopeo list-tags "docker-archive:$UPLOAD_FILE" > "$WORKSPACE/tags.json"
  IMAGE=$(jq '.Tags[0]' "$WORKSPACE/tags.json"| tr -d \")

  # copy local docker archive as docker image to artifactory register
  skopeo copy "docker-archive:${UPLOAD_FILE}" "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile "$WORKSPACE/$SKOPEO_AUTH"

  # get checksum from the docker image
  skopeo inspect "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile "$WORKSPACE/auth.json" > "$WORKSPACE/inspect.json"
  SHA256=$(jq '.Digest' "$WORKSPACE/inspect.json" | tr -d \")

  java -jar "$TOOL_FOLDER/wrapperxray.jar" "--name" "$IMAGE" "--sha256" "$SHA256" "--scantype" "docker" "--outputfile" "$PDS_JOB_RESULT_FILE"
done

clean_workspace