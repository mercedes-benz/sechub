<!-- SPDX-License-Identifier: MIT --->
# Change Log

All notable changes to the "sechub-plugin-vscode" extension will be documented in this file.

Check [Keep a Changelog](http://keepachangelog.com/) for recommendations on how to structure this file.

## [1.1.0] - 2025-08-04
- New features:
  - **WebScan**: Support for WebScan reports (limited to viewing issues)
  - **Remote Report Fetching**: Fetch reports directly from the SecHub server.
  - **False Positive Handling**: Mark issues as false positives directly in the report.
- Added Server View with server connection state and report table
- Replaced Report Tree View with a new Report Web View for better navigation and usability
- New commands:
  - `SecHub: Change Server URL`: set the SecHub server URL for the plugin.
  - `SecHub: Change Credentials` set the basic authentication credentials for the SecHub server (userId and ApiToken).
  - `SecHub: Select Project`: select a SecHub project from the list of your available projects.
  - `SecHub: Fetch Report by UUID from Server`: Fetch a remote report by its UUID from the SecHub server.
  - `SecHub: Clear SecHub Data`: Clear all SecHub data from the plugin, except current report in view.
  - `SecHub: Refresh Server View`: Refresh the server view to show the latest reports.
  - `SecHub: Change Web UI URL`: Change the SecHub Web UI URL (Button shown when try to open a `WebScan` report)
- Updated README with new features and usage instructions
- Updated dependencies
- Added build script for easier setup and build

## [1.0.1] - 2023-06-14
- Fix README issue by converting AsciiDoc to Markdown

## [1.0.0] - 2023-06-14
- Fixed issues importing reports
- Dependencies updated
- Minimum NodeJS version 16

## [0.1.3] - 2022-03-22
- Dependencies updated
- Workflow uses Node 14
- Notice about move of namespace added

## [0.1.2] - 2022-03-18
- Dependencies updated 
- rebranding to Mercedes-Benz

## [0.1.1] - 2021-12-14
- Dependencies updated

## [0.1.0]
- Initial release
- User has possibility to load existing SecHub report from local file system.
- Loaded reports can be crawled and editor will show up when source is available
- If CWE information is available a link is provided in details

## [Unreleased]
