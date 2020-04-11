### io.kaif

 * TODO

# Development

### Prepare Dart for IDE

 * install Intellij Dart plugin
   * intellij need local copy of dart sdk, you can install it in your OS
 * open `pubspec.yaml` and click `Get Dependencies`

### Prepare Vagrant

 * go to `kaif-deploy`, read README. Install vagrant and provision an VM
 * you may need to retry provision if failed

```
   cd kaif-deploy
   vagrant up
   ansible-playbook -dev site.yml  # if failed on postfix, just retry again
```

 * after vagrant complete, run `play_vagrant_recreate_db.sh` to initialize
   DB schema

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

 * append `?kaif-locale=en` can force change locale in bootRun server, default
   value is zh_TW


### Deploy web app to vagrant

 * build kaif-web app then deploy to vagrant

```
  ./assemble.sh
  cd kaif-deploy
  ./play_vagrant_deploy.sh
```

 * go visit https://localdev.kaif.io/ to check nginx is ready

### Production provision and deployment

 * you need secret files to operate production servers. see kaif-deploy/README.md for detail
