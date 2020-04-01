FROM node:10

LABEL "com.github.actions.name"="Publish Typescript Events Library"
LABEL "com.github.actions.description"="Compile AVSC files into Typescript classes and publish the latest version."

RUN apt-get update \
    && apt-get install -y jq git python \
    && apt-get clean

RUN npm install -g --unsafe-perm typescript @chasdevs/avro-to-typescript

ADD entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]