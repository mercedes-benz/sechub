#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

UPLOAD_FILE=$WORKSPACE/random-mini.tar

# login to JFROG artifactory
skopeo login $ARTIFACTORY -u $XRAY_USERNAME -p $XRAY_PASSWORD --authfile $WORKSPACE/auth.json

# get image name, tag and checksum from the tar file
skopeo list-tags docker-archive:$UPLOAD_FILE > $WORKSPACE/tags.json
skopeo inspect docker-archive:$UPLOAD_FILE > $WORKSPACE/inspect.json
IMAGE=$(jq '.Tags[0]' $WORKSPACE/tags.json | tr -d \")
SHA256=$(jq '.Digest' $WORKSPACE/inspect.json | tr -d \")

skopeo copy "docker-archive:${UPLOAD_FILE}" "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile $WORKSPACE/auth.json

# TODO checksum not correct this way :(

# run as jar
java -jar $TOOL_FOLDER/wrapperxray.jar $IMAGE $SHA256
