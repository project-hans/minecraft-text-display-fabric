FROM itzg/minecraft-server
LABEL authors="Caesar"

# Server config
ENV EULA=TRUE
ENV TYPE=PAPER
ENV VELOCITY_VERSION=3.3.0-SNAPSHOT
ENV MINECRAFT_VERSION=1.21.4

ADD build/libs/poc-1.0.0-SNAPSHOT-all.jar /server/plugins/TextDisplayExperimentsPlugin-1.0-SNAPSHOT.jar

