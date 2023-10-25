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
REGISTER=$XRAY_DOCKER_REGISTER

check_valid_upload () {
  # count number of files/ folders uploaded and checks number of uploads greater equals 2
  if [ $(ls $UPLOAD_DIR | wc -l) -ge 2 ]
  then
    echo "Error: more than one file was uploaded: $(ls $UPLOAD_DIR)"
    exit 1
  fi
}

login_into_artifactory () {
  # login to JFROG artifactory
  LOGIN=$(skopeo login "$XRAY_ARTIFACTORY" --username "$XRAY_USERNAME" --password "$XRAY_PASSWORD" --authfile "$WORKSPACE/$SKOPEO_AUTH")
  if [ "$LOGIN" = "Login Succeeded!" ]
  then
    continue
  else
    echo "Error: Skopeo could not login to $XRAY_ARTIFACTORY with user $XRAY_USERNAME"
    exit 1
  fi
}

clean_workspace () {
  rm -rf "$WORKSPACE/"*
  rm -rf "$UPLOAD_DIR/"*
}

# main program
check_valid_upload
login_into_artifactory

# Get docker archives from binary upload folder
TAR_FILES=$(find $UPLOAD_DIR -type f -name "*.tar")
for UPLOAD_FILE in "$TAR_FILES"
do
  echo "---------"
  echo "Processing Xray upload and docker scan for file ${UPLOAD_FILE}"
  echo "---------"

  # get image name and tag
  skopeo list-tags "docker-archive:$UPLOAD_FILE" > "$WORKSPACE/tags.json"
  IMAGE=$(jq '.Tags[0]' "$WORKSPACE/tags.json"| tr -d \")

  # copy local docker archive as docker image to artifactory register
  skopeo copy "docker-archive:${UPLOAD_FILE}" "docker://${XRAY_ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile "$WORKSPACE/$SKOPEO_AUTH"

  # get checksum from the docker image
  skopeo inspect "docker://${XRAY_ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile "$WORKSPACE/auth.json" > "$WORKSPACE/inspect.json"
  SHA256=$(jq '.Digest' "$WORKSPACE/inspect.json" | tr -d \")

  java -jar "$TOOL_FOLDER/wrapperxray.jar" "--name" "$IMAGE" "--checksum" "$SHA256" "--scantype" "docker" "--outputfile" "$PDS_JOB_RESULT_FILE"
  
  # SPDX report can be returned as followed - SPDX does not contain vulnerabilities
  cp "$WORKSPACE/XrayArtifactoryReports/"*SPDX.json "$PDS_JOB_RESULT_FILE"
done

clean_workspace
