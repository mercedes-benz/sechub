# About

## What is this folder for?
This folder contains the github pages documentation content.

## Why is this folder empty?
We do provide github pages only inside branch `documentation`.
So for all other branches this ignored by git

## How is github pages updated?
After a release build has been done the HTML documentation artifact content will be copied to
dedicated docs folder (currently we support only `latest`) in branch `documentation`. 

After a push it will be available after a short while when jekyll has rebuild pages.

