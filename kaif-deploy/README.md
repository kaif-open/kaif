
The Ansible playbook for server provision. Playbooks can
apply to vagrant VM or production servers, depends on inventory file.

Install required software
==========================

* Prepare for Mac

```
  # ansible
  brew install ansible

  # vagrant
  # go to https://www.vagrantup.com/downloads and install .dmg

  # virtualbox
  # go to https://www.virtualbox.org/wiki/Downloads and install .dmg
```


Production provision and deployment
===================================

* To use ansible with production server, you need to prepare secret files first.
  There are three files required to operate production servers:

```
kaif/kaif-deploy
                /secret/kaif_rsa
                /secret/kaif_rsa.pub
                /production/group_vars/webs/secret.yml
```

* please copy corresponding secret files to correct locations.

* NEVER commit secret files to git !!! See `kaif/kaif-deploy/.gitignore`

* after secret files ready, you can execute production commands in `COMMAND.md`

* there are several how-to guide for configure GCE instances and apply SSL certification
  see `howto` folder.