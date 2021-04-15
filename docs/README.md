<!-- SPDX-License-Identifier: MIT --->
# About

## What is this folder for?
This folder is used for SecHub [GitHub pages](https://daimler.github.io/sechub/).
 

## Why is this folder empty?
Only branch `documentation` contains data - so for all other branches this ignored by git.

## How is github pages updated?
After a release build has been done, the HTML documentation artifact content will be copied to a
dedicated subfolder (currently we support only `latest`) in branch `documentation`. 

After a push it will be available after a short while when jekyll has rebuild pages.

