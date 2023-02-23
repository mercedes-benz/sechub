#!/bin/sh
baseDir=$(pwd)
sh clone-repo.sh
cd sechub/
sh "$baseDir"/start-sechub.sh
sh "$baseDir"/start-pds-gosec.sh
sh "$baseDir"/setup-project.sh