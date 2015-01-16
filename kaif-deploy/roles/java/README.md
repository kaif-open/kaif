Ansible role to install Java
============================

Description
-----------

By default Debian and Ubuntu only provide OpenJDK, but sometimes you may want to use the official Oracle JRE/JDK.

This uses the Ubuntu PPA maintained by [Webupd8](http://www.webupd8.org/). It works without problems on Debian too.

How to use
----------

Installing a Oracle JDK
-----------------------

Override the list *java_versions* with a list of JDK you want to install.

The valid values are:

  * oracle-jdk7-installer (Oracle JDK 7)
  * oracle-java6-installer (Oracle JRE 6)
  * oracle-java7-installer (Oracle JRE 7)
  * oracle-java8-installer (Oracle JRE 8 Preview)
  * openjdk-6-jdk (OpenJDK JDK 6)
  * openjdk-7-jdk (OpenJDK JDK 7)
  * openjdk-6-jre (OpenJDK JRE 6)
  * openjdk-6-jre-headless (OpenJDK JRE 6 Headless)
  * openjdk-7-jre (OpenJDK JRE 7)
  * openjdk-7-jre-headless (OpenJDK JRE 7 Headless)
