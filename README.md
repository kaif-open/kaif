### io.kaif

 * TODO

# Development

### Prepare Dart

 * download dart sdk, install to a path -- `/path/to/dart-sdk`
 * export env in your shell `export DART_SDK=/path/to/dart-sdk`
 * install Intellij Dart plugin
 * open `pubspec.yaml` and click `Get Dependencies`

### Prepare Vagrant

 * go to `kaif-deploy`, read README. Install vagrant and provision an VM
 * you may need to retry provision if failed

```
   cd kaif-deploy
   vagrant up
   vagrant provision   # if failed on postfix, just retry again
```

 * after vagrant complete, run `play_vagrant_recreate_db.sh` to initialize
   DB schema

### Prepare dev web server

 * go to `kaif-web`, execute gradle tomcat, and pub serve

```
   cd kaif-web
   ../gradlew pubServe
   ../gradlew bootRun
```

 * `bootRun` will start a development embed tomcat, you can visit
   http://localhost:5980

 * `pubServe` will start pub server in 15980 port, which used by dev server

 * you can use gradle in Intellij to run `bootRun` and `pubServe`. For `pubServe`
   you need to specify vm arguments: `-DDART_SDK=/absolute/path/to/dart-sdk`

### Intellij IDEA configuration

 * import code style in tools/idea_settings.jar (scheme select `lambda_idea`)

### Development tips

 * append `?kaif-locale=en` can force change locale in bootRun server, default
   value is zh_TW

### Deploy web app to vagrant

 * build kaif-web.war then deploy to vagrant

```
  ./war.sh
  cd kaif-deploy
  ./play_vagrant_deploy_war.sh
```

 * edit your /etc/hosts

```
  192.168.59.59  localdev.kaif.io
```

 * go visit https://localdev.kaif.io/ to check nginx is ready