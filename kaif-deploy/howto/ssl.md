### Create CSR

* generate private/public key first,  keyalg and keysize is important (or iphone could not used)

```
keytool -genkey -keystore kaifio.jks -alias kaif -keyalg RSA -keysize 2048 -dname "CN=kaif.io, OU=Kaif, O=Kaif, L=Taipei, ST=Taiwan, C=TW"
```

* generate p12 file from jks

```
keytool -v -importkeystore -srckeystore kaifio.jks -srcalias kaif -destkeystore kaifio.p12 -deststoretype PKCS12
```

* extract private key from p12

```
openssl pkcs12 -in kaifio.p12 -out kaif_private.pem -nodes
```

* generate certification request (csr) for godaddy

```
keytool -certreq -keyalg RSA -alias kaif -file ccrtreq.csr -keystore kaifio.jks
```

* godaddy: upload csr to godaddy and download certifications, the download folder should include:

```
xxxxxxxxxxxxxxxx.crt       # our certification, kaif-io.crt
gd_bundle-g2-g1.crt        # godaddy intermediate certification
```

* comodo: upload csr to comodo and dowload, the download folder should include:

```
kaif_io.crt                # our certification, kaif-io.crt
COMODORSAAddTrustCA.crt    # comodo intermediate
COMODORSADomainValidationSecureServerCA.crt  # comodo intermediate
```

* comodo: concatenate certification for nginx
 * edit file production/group_vars/webs/secret.yml
 * in `inv_nginx_ssl_crt_content`, it should contains 3 certificates 
   * the first part is content of purchased certification for kaif.io 
   * the rest part is comodo intermediate certifications. the intermediate files 
     `COMODORSADomainValidationSecureServerCA.crt` and `COMODORSAAddTrustCA.crt`
     both should be included, So finally the field `inv_nginx_ssl_crt_content` will like:

```
inv_nginx_ssl_crt_content: |
                           -----BEGIN CERTIFICATE-----
                           ...
                           ... content of purchased kaif-io.crt 
                           ...
                           -----END CERTIFICATE-----
                           -----BEGIN CERTIFICATE-----
                           ...
                           ... content of COMODORSADomainValidationSecureServerCA.crt
                           ...
                           -----END CERTIFICATE-----
                           -----BEGIN CERTIFICATE-----
                           ...
                           ... content of COMODORSAAddTrustCA.crt
                           ...
                           -----END CERTIFICATE-----
```


* dodaddy: concatenate certification for nginx

 * edit file production/group_vars/webs/secret.yml
 * in `inv_nginx_ssl_crt_content`, it should contains 3 certificates 
   * the first part is content of purchased certification for kaif.io 
   * the rest part is _two_ of godaddy intermediate certifications. the intermediate file
     `gd_bundle-g2-g1.crt` include 3 certificates and the last one is root certificate of godaddy
     , which already included in browser. so we can safely exclude it to save bandwidth.
     So finally the field `inv_nginx_ssl_crt_content` will like:

```
inv_nginx_ssl_crt_content: |
                           -----BEGIN CERTIFICATE-----
                           ...
                           ... content of purchased kaif-io.crt 
                           ...
                           -----END CERTIFICATE-----
                           -----BEGIN CERTIFICATE-----
                           ...
                           ... content of 1st certificate in gd_bundle-g2-g1.crt
                           ...
                           -----END CERTIFICATE-----
                           -----BEGIN CERTIFICATE-----
                           ...
                           ... content of 2nd certificate in gd_bundle-g2-g1.crt
                           ...
                           -----END CERTIFICATE-----
```