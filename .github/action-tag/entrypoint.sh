#!/bin/bash

set -e

#TODO: Use the new method https://help.github.com/en/actions/configuring-and-managing-workflows/authenticating-with-the-github_token
# The git CLI is used to interact with the events repo.
# To avoid the need for user interaction, the private github deploy key for the repository is added and github.com must be added to known_hosts.
echo "Authorizing SSH connection to github.com using known hosts and base64-encoded ssh private deploy key..."
mkdir -p /root/.ssh \
    && (host=github.com; ssh-keyscan -H $host; for ip in $(dig @8.8.8.8 github.com +short); do ssh-keyscan -H $host,$ip; ssh-keyscan -H $ip; done) 2> /dev/null > /root/.ssh/known_hosts \
    && echo ${BASE64_SSH_PRIVATE_KEY} | base64 -d > /root/.ssh/id_rsa \
    && chmod 600 /root/.ssh/id_rsa \
    && git config --global user.email "chasdevs@gmail.com" \
    && git config --global user.name "Chas (Automated: Github Actions Deploy Key)"

# Change remote to use SSL
git remote set-url origin git@github.com:Footage-Firm/events.git

#get highest tag number
VERSION=`git describe --match "[0-9]*" --abbrev=0 --tags`

#create new tag
NEW_TAG=$((VERSION+1))
echo "Last tag version $VERSION;  New tag will be $NEW_TAG"

#get current hash and see if it already has a tag
GIT_COMMIT=`git rev-parse HEAD`
NEEDS_TAG=`git describe --contains ${NEW_TAG} 2>/dev/null`

#only tag if no tag already (would be better if the git describe command above could have a silent option)
if [[ -z "$NEEDS_TAG" ]]; then
    echo "Tagging with $NEW_TAG and pushing."
    git tag $NEW_TAG
    git push --tags
else
    echo "Current commit already has a tag $NEW_TAG"
    exit 1
fi