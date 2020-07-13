<!-- SPDX-License-Identifier: MIT --->

# About this project

sechub-doc contains all documentation about the sechub:

- user handbook
- architecture
- techdoc
- operations

All is done with `asciidoc` except those little tech info files as this one, which are done in `markdown` because bit bucket is not
able to render asciidoc files...

## Documentation content
More information can be found inside the documents.
Please refer to

- /sechub-doc/src/docs/asciidoc/sechub-architecture.adoc
- /sechub-doc/src/docs/asciidoc/sechub-operations.adoc
- /sechub-doc/src/docs/asciidoc/sechub-techdoc.adoc
- /sechub-doc/src/docs/asciidoc/sechub-user.adoc

Some document parts are complete generated , e.g.

- messaging diagrams (generated plantuml files)
- system property descriptions
- schedule descriptions
- use case documentation

More details about generation can be found inside `sechub-techdoc` document. There is a special documentation chapter inside.

## Create documentation output files and generated content
When you want to create the documentation files and also the generated content, just enter at repository root folder:
`gradlew documentation`

after the build you find the output files at
`sechub-doc/build/asciidoc/`
There will be HTML and also PDF output there.

On build server you will find the documents also for each build so a local generation is normally not necessary -
and because its asciidoc the files can read directly as well.


## Tool support for documentation
For Eclipse and IDEA you can find plugins for showing/editing `asciidoc`.

For eclipse: https://marketplace.eclipse.org/content/asciidoctor-editor