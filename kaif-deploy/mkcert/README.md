Installation
===============

* mac:

```
brew install mkcert
```

* linux:

```
sudo apt install libnss3-tools
wget https://github.com/FiloSottile/mkcert/releases/download/v1.4.1/mkcert-v1.4.1-linux-amd64
mv mkcert-v1.4.1-linux-amd64 ~/app/mkcert 
``` 

ROOT CA
===============

```
mkcert -install
```

COPY ROOT CA for k8s cert-manager issuer
=========================================

```
root_ca_dir=$(mkcert -CAROOT)
cp $root_ca_dir/*.pem .
```

Localhost certificate
=======================

* for fe dev server

```
mkcert localhost
```

* for spring boot server
    * password is `changeme`

```
mkcert -pkcs12 localhost
```
