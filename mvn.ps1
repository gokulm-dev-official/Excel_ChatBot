$mvnHome = "C:\Users\GOKUL M\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6"
$jar = (Get-ChildItem "$mvnHome\boot\plexus-classworlds*.jar").FullName
& 'C:\Program Files\Java\jdk-26\bin\java.exe' "-Dclassworlds.conf=$mvnHome\bin\m2.conf" "-Dmaven.home=$mvnHome" "-Dmaven.multiModuleProjectDirectory=$PSScriptRoot" -classpath $jar org.codehaus.plexus.classworlds.launcher.Launcher $args

