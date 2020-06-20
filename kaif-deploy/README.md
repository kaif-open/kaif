
The Ansible playbook for server provision. Playbooks can
apply to vagrant VM or production servers, depends on inventory file.

Install required software
==========================

* Prepare for Mac

```
  # install ansible specified version (other version not work) 
  brew install python
  pip install ansible==2.9.10 markupsafe  

  # vagrant version >= 1.8.1
  # go to https://www.vagrantup.com/downloads and install .dmg

  # virtualbox > 5.0
  # go to https://www.virtualbox.org/wiki/Downloads and install .dmg
```


Production provision and deployment
===================================

* To use ansible with production server, you need to prepare secret files first.

* secret files
  
  prepare ansible vault password in file:
  
  ```
  secret/vault_password_file
  ```

  then decrypt vault encrypted ssh keys:

  ```
  ansible-playbook -i production deploy/decrypt_secret.yml --vault-password-file=secret/vault_password_file 
  ```
  
* NEVER commit `vault_password_file` and `kaif_rsa*` to git !!! See `kaif/kaif-deploy/.gitignore`

* after secret files ready, you can execute production commands in `COMMAND.md`

* there are several how-to guide for configure GCE instances, see `howto` folder.