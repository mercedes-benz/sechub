# SPDX-License-Identifier: MIT
import argparse
import json
import re
from datetime import datetime

from bs4 import BeautifulSoup
from requests import get

alert_url = 'https://www.zaproxy.org/docs/alerts/'


def generate_owaspzap_ruleset(rule_release_status, output_file):
    response = get(alert_url)

    soup = BeautifulSoup(response.content, 'html.parser')
    table_body = soup.find('tbody')
    if table_body is None:
        raise TypeError(
            'Unable to find table body. The table body should exists. Value of table body variable "tbody" was "None".')

    rules = create_rules(table_body, rule_release_status)
    owasp_zap_ruleset = {'timestamp': datetime.now().__str__(), 'origin': alert_url, 'rules': rules}

    with open(output_file, 'w') as file:
        file.write(json.dumps(owasp_zap_ruleset, indent=2))


def create_rules(table_body, wanted_status):
    rows = table_body.find_all('tr')
    rules = {}
    for row in rows:
        splitted_row = row.text.strip().split(sep='\n')
        id_value = splitted_row[0].strip()
        name = splitted_row[1].strip()
        status = splitted_row[2].strip()

        type_value = splitted_row[4].strip().lower()

        if '-' in id_value:
            continue
        if status not in wanted_status:
            continue

        rules[generate_reference(id_value, name)] = {'id': id_value, 'name': name, 'type': type_value,
                                                     'link': alert_url + id_value}

    return rules


def generate_reference(id_value, name):
    spaces_replaced = name.replace(' ', '-')
    reference = re.sub('-+', '-', spaces_replaced)

    if reference.endswith('-'):
        reference = reference + id_value
    else:
        reference = reference + '-' + id_value

    return reference


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--output-file', required=True)

    parser.add_argument('--rule-release-status', required=False, nargs='*',
                        type=str, choices=['release', 'beta', 'alpha'], default=['release', 'beta', 'alpha'],
                        help='Specify values separated by spaces like: release beta alpha')
    args = parser.parse_args()
    generate_owaspzap_ruleset(args.rule_release_status, args.output_file)

