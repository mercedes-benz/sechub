#!/bin/bash
# SPDX-License-Identifier: MIT

DOCS_FILE_LIST=""
DOCS_SOURCE_DIR="build/docs/final-html"
DEST_DIR="../docs"
DOCS_DEST_DIR="$DEST_DIR/latest"
DOCS_IMAGE_DIR="images"
GIT_RELEASE_BRANCH="master"
WEBSITE_HOME="../sechub-website"
WEBSITE_BUILD_DIR="$WEBSITE_HOME/.output/public"
WEBSITE_FILE_LIST=""

function website_add_changed_files {
  pushd "$WEBSITE_BUILD_DIR/" >/dev/null 2>&1
  local website_files=`find . -type f | sed 'sX^./XX'`
  popd >/dev/null 2>&1

  local nuxt_dir="$DEST_DIR/_nuxt"
  if [ -d "$nuxt_dir" ] ; then
    echo "# Wiping _nuxt destination directory"
    # because file names include checksums and thus change each time
    git rm -rf "$nuxt_dir/"
  fi

  echo -n "# Adding changed or new website files:"
  for website_file in $website_files ; do
    if ! cmp --silent "$WEBSITE_BUILD_DIR/$website_file" "$DEST_DIR/$website_file" ; then
      echo -n " '$website_file'"
      WEBSITE_FILE_LIST="$WEBSITE_FILE_LIST $website_file"
    fi
  done
  echo
}

function docs_add_changed_images {
  pushd "$DOCS_SOURCE_DIR/" >/dev/null 2>&1
  local imagefiles=`ls $DOCS_IMAGE_DIR/*`
  popd >/dev/null 2>&1

  echo -n "# Adding changed or new image files:"
  for imagefile in $imagefiles ; do
    if ! cmp --silent "$DOCS_SOURCE_DIR/$imagefile" "$DOCS_DEST_DIR/$imagefile" ; then
      echo -n " '$imagefile'"
      DOCS_FILE_LIST="$DOCS_FILE_LIST $imagefile"
    fi
  done
  echo
}

function docs_add_changed_html_files {
  pushd "$DOCS_SOURCE_DIR/" >/dev/null 2>&1
  local htmlfiles=`ls *.html`
  popd >/dev/null 2>&1

  echo -n "# Adding changed or new html files:"
  for htmlfile in $htmlfiles ; do
    if ! cmp --silent "$DOCS_SOURCE_DIR/$htmlfile" "$DOCS_DEST_DIR/$htmlfile" ; then
      echo -n " '$htmlfile'"
      DOCS_FILE_LIST="$DOCS_FILE_LIST $htmlfile"
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

echo "# SecHub website: Build and publish"
"$WEBSITE_HOME"/build-sechub-website.sh
website_add_changed_files

# Copy files to destination and stage them for commit
for file in $WEBSITE_FILE_LIST ; do
  /bin/cp "$WEBSITE_BUILD_DIR/$file" "$DEST_DIR/$file"
  echo "git add \"$DEST_DIR/$file\""
  git add "$DEST_DIR/$file"
done

exit 0  # Remove after development

echo "# Docs: Update images directory (changed files only)"
docs_add_changed_images

echo "# Docs: Add changed html files"
docs_add_changed_html_files

# Copy files to destination and stage them for commit
for file in $DOCS_FILE_LIST ; do
  /bin/cp "$DOCS_SOURCE_DIR/$file" "$DOCS_DEST_DIR/$file"
  echo "git add -f \"$DOCS_DEST_DIR/$file\""
  git add -f "$DOCS_DEST_DIR/$file"
done

# Important: We do no git commit here - so everything the script does, can be undone.
