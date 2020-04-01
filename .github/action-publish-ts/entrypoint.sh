#!/bin/bash

set -e

function main() {

    TAG=$(echo ${GITHUB_REF} | sed -r 's/refs\/tags\/(([0-9]+\.?){1,3})/\1/g')

    if [[ -z "$TAG" ]] || ! [[ "$TAG" =~ ^([0-9]+\.?){1,3}$ ]]; then
        echo "Git tag from parsed Github event is invalid. Ref: $GITHUB_REF";
        exit 3
    fi

    auth_github
    
    ROOT=$(pwd)
    TS_REPO="$ROOT/target/events-ts"

    echo "Checking-out events-ts..."
    rm -rf target/events-ts
    git clone git@github.com:chasdevs/events-ts.git "$TS_REPO" 2>&1
    # (Redirect stderr to make logs sensible in github actions)

    echo "Changing directory to $TS_REPO"
    cd "$TS_REPO"

    echo "Installing node modules..."
    npm --unsafe-perm install --ignore-scripts # Skip compilation of binaries since we only need Typescript to compile.

    echo "Clearing events directory..."
    rm -rf example/*

    AVSC_DIR="$ROOT/target/generated/avsc"
    SCRIPT_VER=$(npm show -g @chasdevs/avro-to-typescript version)
    echo "Compiling Typescript classes from $AVSC_DIR into $TS_REPO (script version $SCRIPT_VER)"
    $(npm -g bin)/avro-to-typescript --compile "$AVSC_DIR" "$TS_REPO"

    NPM_VERSION=$(parseSemver "$TAG")
    echo "Setting new version in package.json: $NPM_VERSION"
    npm --no-git-tag-version version --allow-same-version "$NPM_VERSION"
    
    echo "Committing changes and pushing to master. Version: $TAG"
    git add . 2>&1
    git commit -m "$TAG" --allow-empty 2>&1
    git push 2>&1
    
    # DEMO; not actually publishing to npm
    echo "Publishing to npm. Version: $NPM_VERSION"
#    echo //registry.npmjs.org/:_authToken="$NPM_TOKEN" >> .npmrc
#    npm --unsafe-perm publish
}

function auth_github() {
    #TODO: Use the new method https://help.github.com/en/actions/configuring-and-managing-workflows/authenticating-with-the-github_token
    # The git CLI is used to interact with the events-ts repo.
    # To avoid the need for user interaction, the private github deploy key for the repository is added and github.com must be added to known_hosts.
    echo "Authorizing SSH connection to github.com using known hosts and base64-encoded ssh private deploy key..."
    mkdir -p /root/.ssh \
        && (host=github.com; ssh-keyscan -H $host; for ip in $(dig @8.8.8.8 github.com +short); do ssh-keyscan -H $host,$ip; ssh-keyscan -H $ip; done) 2> /dev/null > /root/.ssh/known_hosts \
        && echo ${BASE64_SSH_PRIVATE_KEY} | base64 -d > /root/.ssh/id_rsa \
        && chmod 600 /root/.ssh/id_rsa \
        && git config --global user.email "chasdevs@gmail.com" \
        && git config --global user.name "Chas (Automated: Github Actions Deploy Key)"
}

# npm semver requires the semver format of X.X.X
function parseSemver() {
    VERSION=$1
    local version_numbers
    split_by version_numbers . ${VERSION}

    while [[ ${#version_numbers[*]} -lt 3 ]]; do
        version_numbers+=(0)
    done

    SEMVER=$(join_by . "${version_numbers[@]:0:3}")
    echo ${SEMVER}
}

function join_by {
    local IFS="$1"; shift; echo "$*";
}

function split_by {
    local -n arr=$1; shift
    local IFS="$1"; shift
    read -r -a arr <<< "$*"
}

main