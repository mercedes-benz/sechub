<!-- SPDX-License-Identifier: MIT --->
# Changelog

## [1.1.0]
### Added
- Added SecHub Finding explanation Button to Finding details view. The button
  opens a page in the editor with detailed information about the finding explained by AI.

## [0.5.0]
### Added
- Support for marking false positives for SAST, Secret and IaC scans

## [0.4.1]
## Changed
- Using new OpenAPI client for robust SecHub server communication
- Make plugin part of the SecHub repository
- Update plugin dependencies to latest versions

## [0.3.0]
### Changed
- Needs now min JRE 11
- Improved DND support, report files can now be imported by different drop locations
  _(formerly DND worked only in upper table)_

### Fixed
- File locations containing regexp parts (e.g. `+`) can now be imported
- Diverse smaller bugs on importing report files are fixed

### Security
- Publishing is now with signed content upload
## [0.2.1]
### Added
- Needs min JRE 8
- SecHub report file import functionality
- Introduced initial parts
<!--
## [Unreleased]
### Added
- Example item

### Changed

### Deprecated

### Removed

### Fixed

### Security
-->