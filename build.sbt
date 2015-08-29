name := "bull-rush"

version := "1.0"

scalaVersion := "2.11.6"

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