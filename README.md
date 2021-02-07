### io.kaif

* TODO

# Development

### JDK-11

* adoptopenjdk is recommended

### Prepare k3d and provision local database

* go to `kaif-deploy`, read README.md. include:
    - install mkcert and k3d
    - build kaif-web docker image
    - provision via terraform + helm

### Prepare Dart for IDE

* install Intellij Dart plugin
    * intellij need local copy of dart sdk, you can install it in your OS
* open `pubspec.yaml` and click `Get Dependencies`

### Prepare dev web server

* go to `kaif-web`, execute gradle tomcat, and pub serve

```
   cd kaif-web
   ../gradlew webDevServe
   ../gradlew bootRun
```

* `bootRun` will start a development embed tomcat, you can visit
  http://localhost:5980

* `webDevServe` will start pub server in 15980 port, which used by dev server

* you can use gradle in Intellij to run `bootRun` and `webDevServe`.

### Intellij IDEA configuration

* import code style in tools/idea_settings.jar (scheme select `lambda_idea`)
* in run configurations, change `Defaults` `JUnit` working directory to $MODULE_DIR$
* Intellij may prompt you use .less filewatcher, we don't use it, just dismiss.

### Development tips

* append `?kaif-locale=en` can force change locale in bootRun server, default value is zh_TW

### Deploy web app to local k3d

* build kaif-web docker image then deploy to k3d

```
  ./buildJibToLocal.sh
  
  kaif-deploy/ctl/kaif_ct.sh # open kaif_ctl console
  
  # within kaif_ctl, execute:
  cd kaif/kaif-deploy/kaif-local
  terraform init
  terraform apply 
```

* go visit https://localdev.kaif.io:5443/

### Production provision and deployment

* see kaif-deploy/README.md for detail
