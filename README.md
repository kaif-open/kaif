### io.kaif

 * TODO

# Development

### Prepare dev server

 * go to `kaif-deploy`, read README. Install vagrant and provision an VM
 * you may need to retry provision if failed

```
   cd kaif-deploy
   vagrant up
   vagrant provision   # if failed on postfix, just retry again
```

 * after vagrant complete, run `play_vagrant_recreate_db.sh` to initialize
   DB schema
 * go to `kaif-web`, execute gradle tomcat

```
   cd kaif-web
   ../gradlew bootRun
```

 * `bootRun` will start a development embed tomcat, you can visit
   http://localhost:5980

### Development with Dart

 * download dart sdk, install to a path -- `/path/to/dart-sdk`
 * export env in your shell `DART_SDK=/path/to/dart-sdk`
 * set your execute path include `DART_SDK/bin`
 * install Intellij Dart plugin
 * install Intellij File watcher plugin
 * import File watcher configs in `kaif/watchers.xml`
 * after import file watcher, edit `dart pub build debug`
 * in that watcher, change program path to your absolute path of dart-sdk
 * open `pubspec.yaml` and click `Get Dependencies`

### Deploy to vagrant

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

 * go visit https://localdev.kaif.io/  to check nginx is ready