;; I have not actually tested this, but it should work
@echo off
cls
set CLASSPATH=.;.\Includes\mysql-connector-java-5.1.38-bin.jar;%classpath%
javac *.java
java Program
