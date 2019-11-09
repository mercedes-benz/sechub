<!-- SPDX-License-Identifier: MIT --->
## About sechub-storage-core
This is an absolute independent core library for storage and was necessary to
divide storage parts and storage testing (s3mock is a spring boot application
which did fail to integrate as normal junit tests when having other spring
boot dependencies (from sechub...))

This contains only the core parts for sechub-storage.
Will be used by :
a) sechub-shared-kernel
b) dedicated storage implementation