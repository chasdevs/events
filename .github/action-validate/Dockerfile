FROM gradle:5.4.1-jdk12

LABEL "com.github.actions.name"="Validate Events"
LABEL "com.github.actions.description"="Validate event schemas locally and against schema registry."

ADD entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]