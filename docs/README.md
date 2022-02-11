<!-- SPDX-License-Identifier: MIT --->
# About

## What is this folder for?
This folder is used for [SecHub's GitHub Pages](https://mercedes-benz.github.io/sechub/).
 
## How are GitHub Pages updated?
The update is done by gradle task `documentation-with-pages`. The generated HTML documentation is in subfolder `latest/`.

That gradle task is executed in job [Releases](https://github.com/mercedes-benz/sechub/actions/workflows/create-releases.yml).

In step 'update_release_documentation', only the relevant HTML and image files for the released product(s) will be copied to subfolder: `latest/`.
(See `helperscripts/git-add-releasedocs.sh` for details.)

The updated docs will be available after a short while when jekyll has rebuilt pages.
