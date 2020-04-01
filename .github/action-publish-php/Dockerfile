FROM chasdevs/avro-to-php

LABEL "com.github.actions.name"="Publish PHP Events Library"
LABEL "com.github.actions.description"="Compile AVSC files into PHP classes and publish the latest version."

RUN apk add git openssh bash jq

ADD entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]