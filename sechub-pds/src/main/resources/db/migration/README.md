<!-- SPDX-License-Identifier: MIT --->
https://flywaydb.org/documentation/migrations#sql-based-migrations

The file name consists of the following parts:

- Prefix: V for versioned migrations, U for undo migrations, R for repeatable migrations
- Version: Underscores (automatically replaced by dots at runtime) separate as many parts as you like (Not for repeatable migrations)
- Separator: __ (two underscores)
- Description: Underscores (automatically replaced by spaces at runtime) separate the words
