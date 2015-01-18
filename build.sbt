name := "hex-trips"

version := "1.0"

organization := "com.esri"

scalaVersion := "2.10.4"

resolvers += "Local Maven Repository" at "file:///" + Path.userHome + "/.m2/repository"

publishMavenStyle := true

pomExtra := (
  <url>https://github.com/mraad/hex-trips</url>
    <licenses>
      <license>
        <name>Apache License, Verision 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:mraad/hex-trips.git</url>
      <connection>scm:git:git@github.com:mraad/hex-trips.git</connection>
    </scm>
    <developers>
      <developer>
        <id>mraad</id>
        <name>Mansour Raad</name>
        <url>https://github.com/mraad</url>
        <email>mraad@esri.com</email>
      </developer>
    </developers>)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.2.0",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.2.0-SNAPSHOT",
  "com.esri" %% "hex-grid" % "1.0",
  "com.esri" % "webmercator" % "1.0",
  "joda-time" % "joda-time" % "2.4"
)
