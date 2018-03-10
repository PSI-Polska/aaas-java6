FROM jkarlos/git-server-docker

RUN apk add --no-cache curl

VOLUME /repos

# TODO provide certs
# COPY ??? /etc/ssh_host_rsa_key
# COPY ??? /etc/ssh_host_rsa_key.pub

# COPY ??? /etc/ssh_host_ecdsa_key
# COPY ??? /etc/ssh_host_ecdsa_key.pub

# TODO StrictHostKeyChecking should be enabled, additionally correct known_hosts should be provided
# RUN sed -ie 's/StrictHostKeyChecking ask/StrictHostKeyChecking no/' /etc/ssh/ssh_config
# TODO think twice if this one is really needed
RUN sed -ie 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config

RUN echo -e '4gorki2pagorki\n4gorki2pagorki' | passwd git

# TODO maybe provide way to introduce new keys
# superbardzotajnehaslo
COPY id_rsa.pub /root/.ssh/id_rsa.pub
COPY id_rsa.nopwd /root/.ssh/id_rsa
RUN chmod 400 /root/.ssh/id_rsa

# TODO we can either have empty repo or have some very basic repo prepared. For now lets choose the later case
# TODO StrictHostKeyChecking should be enabled, additionally correct known_hosts should be provided
RUN GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" git clone --bare git@gitlab.com:edmpsi/aaas-scripts.git /git-server/repos/aaas-scripts.git
COPY post-update.hook /git-server/repos/aaas-scripts.git/hooks/post-update

# USER git
