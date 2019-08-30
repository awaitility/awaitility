#!/bin/bash
read -s -p "Bintray api key: " api_key
echo
read -p "Version to release: " version

tmpFolderRootName=/tmp/$RANDOM
mkdir -p "${tmpFolderRootName}"

maven_awaitility_folder=$HOME/.m2/repository/org/awaitility
project_names=(awaitility awaitility-groovy awaitility-kotlin awaitility-scala)

for project in ${project_names[*]} ; do
    folder="${maven_awaitility_folder}/${project}/${version}"
    artifact_prefix="${project}-${version}"
    folder_artifact_prefix="${folder}/${artifact_prefix}"
    zip -q "${tmpFolderRootName}/${project}-${version}.zip" "${folder_artifact_prefix}.jar" "${folder_artifact_prefix}-sources.jar" "${folder_artifact_prefix}-javadoc.jar"
done

echo "Files to deploy:"

files_to_deploy=`ls "${tmpFolderRootName}"`
for zipFile in ${files_to_deploy}; do
  printf "${zipFile}:\n\t\t"
  unzip -l "${tmpFolderRootName}/${zipFile}"
  printf "\n"
done

read -p "Is this correct? [y/N]" -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
	for file in ${files_to_deploy}; do
        absolute_path="${tmpFolderRootName}/${file}"
		echo "Uploading ${absolute_path}"
		curl -T "${absolute_path}" -ujohanhaleby:"${api_key}" https://api.bintray.com/content/johanhaleby/generic/awaitility/"${version}"/
	done
echo "Awaitility $version was deployed to Bintray. Login to Bintray to publish the release."
fi
rm -rf "${tmpFolderRootName}"