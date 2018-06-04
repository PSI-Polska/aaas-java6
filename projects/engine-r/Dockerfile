FROM registry.gitlab.com/edmpsi/aaas/engine/r-base:0.2

LABEL maintainer = "krzysztof.skitek@gmail.com"

ENV RPORT 6311
ENV REPOSITORY_ADDRESS scriptsRepo

EXPOSE $RPORT

COPY src/R/*.R /var/userScripts/
COPY src/R/standard/*.R /var/userScripts/

COPY src/docker/*.rds /var/trainedModels/

WORKDIR /var/userScripts/
