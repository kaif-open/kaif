 * after install, each domain will generate following pems:
 
```
/etc/nginx_pki/
               domain1.com/dhparam.pem
                           letsencrypt-cert.pem
                           letsencrypt-chain.pem
                           letsencrypt-key.pem
```
 
 * sample role configuration
 
```
   - role: letsencrypt
     become: true
     letsencrypt_domain_names:
       - domain1.com
       - repository.liquable.com
``` 

 * nginx configuration
 
```
   server {
      ssl_dhparam /etc/nginx_pki/domain1.com/dhparam.pem;
      ssl_certificate /etc/nginx_pki/domain1.com/letsencrypt-cert.pem;
      ssl_certificate_key /etc/nginx_pki/domain1.com/letsencrypt-key.pem;
      ssl_trusted_certificate /etc/nginx_pki/domain1.com/letsencrypt-chain.pem;
      
      # reference:
      listen 443 ssl http2;
      ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
      ssl_prefer_server_ciphers on;
      ssl_ciphers 'ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:kEDH+AESGCM:ECDHE-RSA-AES128-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES256-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:ECDHE-ECDSA-AES256-SHA:DHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA256:DHE-RSA-AES256-SHA256:DHE-DSS-AES256-SHA:DHE-RSA-AES256-SHA:AES128-GCM-SHA256:AES256-GCM-SHA384:AES128:AES256:AES:DES-CBC3-SHA:HIGH:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!PSK';
      ssl_stapling on;
      ssl_stapling_verify on;
      resolver 8.8.4.4 8.8.8.8 valid=300s;
      resolver_timeout 10s;
      ssl_session_cache builtin:1000 shared:SSL:10m;
      ssl_session_timeout 10m;
   }
```