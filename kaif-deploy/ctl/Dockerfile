FROM ubuntu:20.04

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV TZ=Asia/Taipei
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get update && apt-get install -y \
        curl \
        telnet \
        unzip \
        libarchive-tools \
        git \
        zsh \
        wget \
        tree \
        python3-pip \
        python3-setuptools \
        vim \
        tzdata \
     && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - \
     && echo "deb http://apt.postgresql.org/pub/repos/apt/ focal-pgdg main" > /etc/apt/sources.list.d/pgdg.list \
     && apt-get update \
     && apt-get -y install postgresql-client-13 \
     && rm -rf /var/lib/apt/lists/*

RUN pip3 install --no-cache-dir \
    awscli==1.19.3

RUN curl -L https://storage.googleapis.com/kubernetes-release/release/v1.20.2/bin/linux/amd64/kubectl \
    -o /usr/local/bin/kubectl \
 && chmod +x /usr/local/bin/kubectl

RUN curl -L https://get.helm.sh/helm-v3.5.2-linux-amd64.tar.gz \
    | tar xvfz - --strip-components 1 -C /usr/local/bin linux-amd64/helm \
 && chmod +x /usr/local/bin/helm

RUN curl -L https://releases.hashicorp.com/terraform/0.14.6/terraform_0.14.6_linux_amd64.zip \
    | bsdtar xvfz - -C /usr/local/bin terraform \
 && chmod +x /usr/local/bin/terraform

RUN curl -L https://github.com/derailed/k9s/releases/download/v0.24.2/k9s_Linux_x86_64.tar.gz \
    | tar xvfz - -C /usr/local/bin k9s \
 && chmod +x /usr/local/bin/k9s

RUN mkdir -p /opt \
 && curl -L https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-319.0.0-linux-x86_64.tar.gz \
    | tar xvfz - -C /opt

## for console access
WORKDIR /root
RUN wget https://github.com/robbyrussell/oh-my-zsh/raw/master/tools/install.sh -O - | zsh || true
COPY zshrc .zshrc
COPY k9s/black_and_wtf.yml .k9s/skin.yml

CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
