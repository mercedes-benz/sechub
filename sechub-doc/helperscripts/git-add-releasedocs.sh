#!/bin/bash
# SPDX-License-Identifier: MIT

FILE_LIST=""
SOURCE_DIR="build/docs/asciidoc"
DEST_DIR="../docs/latest"
IMAGE_DIR="images"
GIT_RELEASE_BRANCH="master"

function add_changed_images(){
  pushd "$SOURCE_DIR/" >/dev/null 2>&1
  local imagefiles=`ls $IMAGE_DIR/*`
  popd >/dev/null 2>&1

  echo -n "# Adding changed or new image files:"
  for imagefile in $imagefiles ; do
    if ! cmp --silent "$SOURCE_DIR/$imagefile" "$DEST_DIR/$imagefile" ; then
      echo -n " '$imagefile'"
      FILE_LIST="$FILE_LIST $imagefile"
    fi
  done
  echo
}

function add_files(){
  local product="$1"
  local files_to_add=""
  echo -n "# Adding files for '$product': "

  case "$product" in
    client)
      files_to_add="sechub-client.html client-download.html"
      ;;
    pds)
      files_to_add="sechub-product-delegation-server.html pds-download.html"
      ;;
    server)
      files_to_add="sechub-architecture.html sechub-operations.html sechub-quickstart-guide.html sechub-restapi.html sechub-techdoc.html server-download.html"
      ;;
    *)
      echo "Ignoring unknown product name '$product' in git tag."
      ;;
  esac
  echo $files_to_add

  FILE_LIST="$FILE_LIST $files_to_add"
}

#######################
cd "`dirname $0`/.."

BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ "$BRANCH" != "$GIT_RELEASE_BRANCH" ] ; then
  echo "$0: This release script is intended to run only on '$GIT_RELEASE_BRANCH' branch. You are on branch '$BRANCH'."
  echo "Exiting..."
  exit 1
fi

# Always update images directory
add_changed_images

# Iterate over tags of git HEAD. (e.g. v1.0.0-client, v1.0.0-server, v1.0.0-pds)
while read tag; do
  add_files $(echo "$tag" | awk -F "-" '{print $NF}')
done < <(git tag --points-at HEAD)

# Copy files to destination and stage them for commit
for file in $FILE_LIST ; do
  /bin/cp "$SOURCE_DIR/$file" "$DEST_DIR/$file"
  echo "git add -f \"$DEST_DIR/$file\""
  git add -f "$DEST_DIR/$file"
done

# Important: We do no git commit here - so everything the script does, can be undone.
