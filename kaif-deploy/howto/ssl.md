### Create CSR

* generate private/public key first,  keyalg and keysize is important (or iphone could not used)

```
keytool -genkey -keystore kaifio.jks -alias kaif -keyalg RSA -keysize 2048 -dname "CN=kaif.io, OU=Kaif, O=Kaif, L=Taipei, ST=Taiwan, C=tw"
```

* generate certification request (csr) for godaddy

```
keytool -certreq -keyalg RSA -alias kaif -file ccrtreq.csr -keystore kaifio.jks
```

* upload csr to godaddy and download certifications, the download folder should include:

```
xxxxxxxxxxxxxxxx.crt       # our certification
gd_bundle-g2-g1.crt        # godaddy intermediate certification
```

* generate p12 file from jks

```
keytool -v -importkeystore -srckeystore kaifio.jks -srcalias kaif -destkeystore kaifio.p12 -deststoretype PKCS12
```

* extract private key from p12

```
openssl pkcs12 -in kaif.p12 -out kaif_private.pem -nodes
```