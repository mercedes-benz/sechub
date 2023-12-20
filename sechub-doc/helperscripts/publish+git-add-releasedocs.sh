#!/bin/bash
# SPDX-License-Identifier: MIT

FILE_LIST=""
SOURCE_DIR="build/docs/final-html"
DEST_DIR="../docs/latest"
IMAGE_DIR="images"
GIT_RELEASE_BRANCH="master"

function add_changed_images {
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

function add_changed_html_files {
  pushd "$SOURCE_DIR/" >/dev/null 2>&1
  local htmlfiles=`ls *.html`
  popd >/dev/null 2>&1

  echo -n "# Adding changed or new html files:"
  for htmlfile in $htmlfiles ; do
    if ! cmp --silent "$SOURCE_DIR/$htmlfile" "$DEST_DIR/$htmlfile" ; then
      echo -n " '$htmlfile'"
      FILE_LIST="$FILE_LIST $htmlfile"
    fi
  done
  echo
}

#######################
cd "`dirname $0`/.."

BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ "$BRANCH" != "$GIT_RELEASE_BRANCH" ] ; then
  echo "$0: This release script is intended to run only on '$GIT_RELEASE_BRANCH' branch. You are on branch '$BRANCH'."
  echo "Exiting..."
  exit 1
fi

# Update images directory (changed files only)
add_changed_images

# Add changed html files
add_changed_html_files

# Copy files to destination and stage them for commit
for file in $FILE_LIST ; do
 /bin/cp "$SOURCE_DIR/$file" "$DEST_DIR/$file"
  echo "git add -f \"$DEST_DIR/$file\""
  git add -f "$DEST_DIR/$file"
done

# Important: We do no git commit here - so everything the script does, can be undone.
