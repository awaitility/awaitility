#!/bin/sh
read -p "Version to generate javadoc for: " version
project_names=(awaitility awaitility-groovy awaitility-kotlin awaitility-scala)

echo "Generating Javadoc for version ${version}."

for project_name in ${project_names[*]}
do
    echo "Generating for ${project_name}"
    curl -Ss http://www.javadoc.io/doc/org.awaitility/${project_name}/${version} >/dev/null 2>&1
done
echo "Completed"

