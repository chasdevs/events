FROM gradle:5.4.1-jdk12

LABEL "com.github.actions.name"="Synchronize Schemas"
LABEL "com.github.actions.description"="Synchronize local schemas with the schema registry."

RUN apt update \
    && apt install -y jq

ADD entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]