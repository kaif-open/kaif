ansible-playbook -i dev \
  --vault-password-file=secret/vault_password_file \
  site.yml
