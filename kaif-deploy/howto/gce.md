### Mount gce disk to /volume

* format disk (attach new disk to server first)

```
mkfs.ext4 /dev/sdb
sudo mkdir -p /volume
sudo mount -t ext4 -o noatime /dev/sdb /volume
```

* add to /etc/fstab

```
/dev/sdb   /volume   ext4    defaults,noatime,nodiratime  1   1
```