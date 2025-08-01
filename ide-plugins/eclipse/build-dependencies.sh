#!/bin/bash
# SPDX-License-Identifier: MIT

set -e

# Get the directory of the script
script_dir=$(dirname "$(realpath "$0")")

  # Calculate the absolute path to the folder
libraries_folder="$script_dir/sechub-eclipse-plugin/lib"

# Function to check for JAR files in a folder
check_jars() {
  local folder="$1"  # Get the folder path from the argument

  # Check if the folder exists
  if [ ! -d "$folder" ]; then
    echo "Folder '$folder' does not exist."
    return 1  # Return 1 to indicate failure
  fi

  # Count the JAR files in the folder
  local jar_count=$(find "$folder" -name "*.jar" | wc -l)

  # Check if no JAR files are present
  if [ "$jar_count" -eq 0 ]; then
    echo "No JAR files found in folder '$folder'."
    return 0  # Return 0 to indicate success (no JARs)
  else
    echo "JAR files found in folder '$folder'."
    return 2  # Return 2 to indicate JARs were found
  fi
}


if check_jars "$libraries_folder"; then
  echo "No JAR files, start building openapi-java-client from SecHub root"
  echo "#--------------------------------------#"
    echo "# Build OpenAPI java cient for eclipse #"
    echo "#--------------------------------------#"
    cd "$script_dir/../.." # ./eclipse/ide-plugins/... -> afterwards in sechub root folder
    pwd
    ./gradlew provideOpenAPIJavaClientForEclipse --console=plain > "$libraries_folder/provideOpenAPIJavaClientForEclipse-output.log"
    echo "> client generated"
else
  echo "JAR files found, skipping openapi-java-client generation (if you want a new client, call gradle provideOpenAPIJavaClientForEclipse manual or clean lib folder) "
fi



