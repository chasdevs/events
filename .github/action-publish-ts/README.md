# Publish Typescript Action

This action compiles AVSC files into Typescript classes and then publishes the library to npm.

# Secrets

- `NPM_TOKEN` - Token for interacting with npm repositories at npmjs.org
- `BASE64_SSH_PRIVATE_KEY_TS` - A base64-encoded private ssh key for a github user with access to the [events-ts](https://github.com/chasdevs/events-ts) repository.
