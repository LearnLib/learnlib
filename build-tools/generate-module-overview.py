#!/usr/bin/python3

import os
import sys
import xml.etree.ElementTree as ET

XPATH_ARTIFACT_ID = "./mvn:artifactId"
XPATH_DESCRIPTION = "./mvn:description"
XPATH_SUBMODULES = "./mvn:modules/mvn:module"
MAVEN_NAMESPACE = {'mvn': 'http://maven.apache.org/POM/4.0.0'}


def main():
    if len(sys.argv) < 2:
        raise Exception("You need to specify a Maven project folder")

    project_dir = parse_target_directory(sys.argv[1])
    generate_entry(0, project_dir)


def parse_target_directory(path):
    if os.path.isabs(path):
        if not os.path.isdir(path):
            raise Exception("%s is not a valid directory", path)
        return path
    else:
        result = os.path.join(os.getcwd(), path)
        if not os.path.isdir(result):
            raise Exception("%s is not a valid directory", result)
        return result


def generate_entry(indent, project_folder):
    pom_path = os.path.join(project_folder, "pom.xml")
    pom = ET.parse(pom_path)

    if indent == 0:
        padding = ''
    else:
        padding = ('{:' + str(indent * 2) + '}').format(' ')

    artifact = pom.find(XPATH_ARTIFACT_ID, namespaces=MAVEN_NAMESPACE)
    description = pom.find(XPATH_DESCRIPTION, namespaces=MAVEN_NAMESPACE)

    print('{}* **{}**: {}'.format(padding, artifact.text, description.text))

    for module in pom.findall(XPATH_SUBMODULES, namespaces=MAVEN_NAMESPACE):
        generate_entry(indent + 1, os.path.join(project_folder, module.text))


if __name__ == '__main__':
    main()
