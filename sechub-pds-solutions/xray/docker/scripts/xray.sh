#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

UPLOAD_FILE=$WORKSPACE/random-mini.tar

# login to JFROG artifactory
skopeo login $ARTIFACTORY -u $XRAY_USERNAME -p $XRAY_PASSWORD --authfile $WORKSPACE/auth.json

# get image name, tag
skopeo list-tags docker-archive:$UPLOAD_FILE > $WORKSPACE/tags.json
IMAGE=$(jq '.Tags[0]' $WORKSPACE/tags.json | tr -d \")

# upload image as docker image to artifactory
skopeo copy "docker-archive:${UPLOAD_FILE}" "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile $WORKSPACE/auth.json

# get checksum vom docker image
skopeo inspect "docker://${ARTIFACTORY}/${REGISTER}/${IMAGE}" --authfile $WORKSPACE/auth.json > $WORKSPACE/inspect.json
SHA256=$(jq '.Digest' $WORKSPACE/inspect.json | tr -d \")

# run as jar
java -jar $TOOL_FOLDER/wrapperxray.jar $IMAGE $SHA256
