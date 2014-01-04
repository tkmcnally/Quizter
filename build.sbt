name := "quizter"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.mongodb" % "mongo-java-driver" % "2.11.3",
  "org.jongo" % "jongo" % "1.0",
  "com.restfb" % "restfb" % "1.6.12",
  "com.google.code.gson" % "gson" % "2.2.4"
)     

play.Project.playJavaSettings
