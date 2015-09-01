enablePlugins(ScalaJSPlugin)
import sbt.Keys._

name := "bull-rush"

lazy val root = project.in(file(".")).
  aggregate(bullRushJS, bullRushJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val bullRush = crossProject.in(file(".")).
  settings(
    name := "bull-rush",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.6",

    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.3.4"
    )
  ).
  jvmSettings(

    libraryDependencies ++= {
      val akkaStreamsV = "1.0-RC4"
      val sprayV = "1.3.2"
      Seq(
        "io.spray"            %%  "spray-can"     % sprayV,
        "io.spray"            %%  "spray-routing" % sprayV,
        "io.spray"            %%  "spray-client"  % sprayV,
        "io.spray"            %%  "spray-http"    % sprayV, // replace with akka http
        "io.spray"            %%  "spray-httpx"   % sprayV,
        "io.spray"            %%  "spray-json"    % sprayV,
        "io.spray"            %%  "spray-util"    % sprayV,
        "io.spray"            %%  "spray-json"    % sprayV,
        "com.typesafe.akka"   %% "akka-stream-experimental"    % akkaStreamsV,
        "com.typesafe.akka"   %% "akka-http-core-experimental" % akkaStreamsV,
        "org.scalatest"              %% "scalatest"            % "2.2.4"  % "test"
      )
    }
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.1",
      "com.github.japgolly.scalajs-react" %%% "core" % "0.9.2",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.9.2",
      "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
    ),
    jsDependencies ++= Seq(
    "org.webjars" % "react" % "0.12.2" / "react-with-addons.js" commonJSName "React"
    )
  )

lazy val bullRushJVM = bullRush.jvm
lazy val bullRushJS = bullRush.js