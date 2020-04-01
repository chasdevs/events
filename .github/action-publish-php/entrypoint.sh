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
    PHP_REPO="$ROOT/target/events-php"

    echo "Checking-out events-php..."
    rm -rf target/events-php
    git clone git@github.com:chasdevs/events-php.git "$PHP_REPO" 2>&1
    # (Redirect stderr to make logs sensible in github actions)

    echo "Changing directory to $PHP_REPO"
    cd "$PHP_REPO"
    echo "Pulling latest code..."
    git pull 2>&1
    echo "Clearing events directory..."
    rm -rf Example/*

    AVSC_DIR="$ROOT/target/generated/avsc"
    echo "Compiling PHP classes from $AVSC_DIR into $PHP_REPO"
    avro-to-php compile "$AVSC_DIR" "$PHP_REPO"

    echo "Committing changes and pushing to master. Version: $TAG"
    git add . 2>&1
    git commit -m "$TAG" --allow-empty 2>&1
    git push 2>&1

    echo "Pushing new tag: $TAG"
    git tag "$TAG" 2>&1
    git push --tags 2>&1
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

main