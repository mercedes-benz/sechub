#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

scan_results_folder="$PDS_JOB_WORKSPACE_LOCATION/results"

echo ""
echo "----------"
echo "Kics Setup"
echo "----------"
echo ""

if [ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "true" ]
then
    echo "Found sources to scan."
else
    echo ""
    echo "ERROR: No sources found."
    echo ""
    echo "Workspace location structure:"
    echo ""
    tree "$PDS_JOB_WORKSPACE_LOCATION"
    exit 1
fi

echo ""
echo "-------------"
echo "Starting scan"
echo "-------------"
echo ""

echo "Starting Kics"
cd $PDS_JOB_SOURCECODE_UNZIPPED_FOLDER
kics scan --ci --exclude-categories "Best practices" --disable-full-descriptions --report-formats "sarif" --output-path "$scan_results_folder" --path "."

#######################################################################################################################
# Workaround: Since there are no CWEs we add a fixed CWE taxonomy to the SARIF report for false-positive handling     #
# This won't be needed anymore once Checkmarx adds CWEs to their reports                                              #
#######################################################################################################################

cat $scan_results_folder/results.sarif | jq '.runs[].taxonomies += [{
					"name": "CWE",
					"version": "4.13",
					"releaseDateUtc": "2023-12-08",
					"guid": "33333333-0000-1111-8888-000000000000",
					"informationUri": "https://cwe.mitre.org/data/published/cwe_v4.13.pdf/",
					"downloadUri": "https://cwe.mitre.org/data/xml/cwec_v4.13.xml.zip",
					"organization": "MITRE",
					"shortDescription": {
					  "text": "The MITRE Common Weakness Enumeration"
					},
					"contents": [
					  "localizedData",
					  "nonLocalizedData"
					],
					"isComprehensive": true,
					"minimumRequiredLocalizedDataSemanticVersion": "4.13",
					"taxa": [
					  {
						"id": "1349",
						"guid": "33333333-0000-1111-8888-111111111111",
						"name": "OWASP Top Ten 2021 Category A05:2021 - Security Misconfiguration",
						"shortDescription": {
						  "text": "Weaknesses in this category are related to the A05 category Security Misconfiguration in the OWASP Top Ten 2021."
						},
						"defaultConfiguration": {
						  "level": "warning"
						}
					  }
					]
 }]' > $scan_results_folder/intermediate.sarif

cat $scan_results_folder/intermediate.sarif | jq '.runs[].tool.driver.rules[].relationships += [{
                    "target": {
                        "id": "1349",
                        "guid": "33333333-0000-1111-8888-111111111111",
                        "toolComponent": {
                            "name": "CWE",
                            "guid": "33333333-0000-1111-8888-000000000000"
                        }
                    }
}]' > $scan_results_folder/results-fixedcwe.sarif

mv $scan_results_folder/results-fixedcwe.sarif $scan_results_folder/results.sarif

######################
# End of workaround  #
######################

echo "Copy result file"
echo "Results folder: $scan_results_folder"
tree "$scan_results_folder"

cp "$scan_results_folder/results.sarif" "$PDS_JOB_RESULT_FILE"