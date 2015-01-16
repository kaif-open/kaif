## building application

* assemble latest war

```
cd kaif
./war.sh
```

## sample commands for vagrant

* startup vm

```
vagrant up kaif
```

* ssh

```
vagrant ssh kaif
# or
ssh -F vagrant.ssh_config kaif
```

* re-provision

```
vagrant provision kaif
```

* deploy war to vagrant 

```
cd kaif/kaif-deploy
./play_vagrant_deploy_war.sh
```


* shutdown vm

```
vagrant halt kaif
```

* reload (reboot) vm

```
vagrant reload kaif
```


* destroy vm (delete all data)

```
vagrant destroy kaif
```

* remove vagrant box

```
vagrant box remove ubuntu14
```
