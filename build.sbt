name := "quizter"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.mongodb" % "mongo-java-driver" % "2.11.3",
  "org.jongo" % "jongo" % "1.0"
)     

play.Project.playJavaSettings
