FROM r-base:3.4.3

LABEL maintainer = "krzysztof.skitek@gmail.com"

ENV RPORT 6311
ENV REPOSITORY_ADDRESS scriptsRepo

EXPOSE $RPORT

RUN mkdir -p /var/userScripts \
    && mkdir -p /var/trainedModels

# TODO git repo keys directory

RUN apt-get update \
    && apt-get install -y git libpq-dev libcurl4-openssl-dev

COPY src/R/*.R /var/userScripts/
COPY src/R/standard/*.R /var/userScripts/

COPY src/docker/*.rds /var/trainedModels/

COPY src/docker/start.sh /usr/local/src/scripts/start.sh

COPY src/R/installPackages.R /usr/local/src/scripts/installPackages.R
RUN ["Rscript", "/usr/local/src/scripts/installPackages.R"]

WORKDIR /var/userScripts/

ENTRYPOINT ["/usr/local/src/scripts/start.sh"]