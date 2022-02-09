#!/bin/bash
# SPDX-License-Identifier: MIT

FILE_LIST=""
SOURCE_DIR="latest"
DEST_DIR="released/sechub"
IMAGE_DIR="images"

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
      files_to_add="sechub-client.html"
      ;;
    pds)
      files_to_add="sechub-product-delegation-server.html"
      ;;
    server)
      files_to_add="sechub-architecture.html sechub-operations.html sechub-quickstart-guide.html sechub-restapi.html sechub-techdoc.html"
      ;;
    *)
      echo "Ignoring unknown product name '$product' in git tag."
      ;;
  esac
  echo $files_to_add

  FILE_LIST="$FILE_LIST $files_to_add"
}

#######################
cd `dirname $0`/..

# Check if we are on 'master' branch
BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ "$BRANCH" != "master" ] ; then
  echo "$0: This release script is intended to run only on 'master' branch. You are on branch '$BRANCH'."
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
