scalaVersion := "2.12.0"

enablePlugins(org.nlogo.build.NetLogoExtension, org.nlogo.build.ExtensionDocumentationPlugin)

javaSource in Compile := baseDirectory.value / "src"

name := "arduino"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
  "-encoding", "us-ascii")

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
  "-encoding", "us-ascii")

netLogoZipSources   := false

netLogoClassManager := "arduino.ArduinoExtension"

netLogoTarget :=
  org.nlogo.build.NetLogoExtension.directoryTarget(baseDirectory.value)

netLogoVersion := "6.0.1-M1"
