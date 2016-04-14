clear
export CLASSPATH=.:./Includes/mysql-connector-java-5.1.38-bin.jar
if [ -z `systemctl status mysqld.service | grep -o running` ]
then
  echo "Starting MySQL service..."
  `systemctl start mysqld.service`
fi
`javac -Xstdout build_log.txt *.java`
build_result=`cat build_log.txt`
if [ -z `cat build_log.txt | grep -o -m1 error` ]
then
  echo "Build Succeeded!"
  `rm build_log.txt`
  java Program
else
  echo "$build_result"
  echo "Build Failed!"
fi
