<!-- SPDX-License-Identifier: MIT --->
# About the sechub-authorization project

Here only spring security is done and roles (user, admin)
is handled.

Resource security is done inside the domains itself.

So e.g. sechub-schedule will provide access only to users 
having role "USER" but also only for those which 
can access the project...

On the other hand will sechub-authorization handle the
spring security roles here inside.

