FROM alpine

LABEL "com.github.actions.name"="Git Tag"
LABEL "com.github.actions.description"="Increment tag version in repo."

RUN apk add git openssh bash

ADD entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]