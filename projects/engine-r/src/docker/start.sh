#!/bin/bash

git clone $REPOSITORY_ADDRESS
#OR
#git pull $2

R -e "Rserve::run.Rserve(port = $RPORT, remote = TRUE)"
