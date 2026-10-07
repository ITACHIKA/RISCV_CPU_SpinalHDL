ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "riscv"

val spinalVersion = "1.12.3"

lazy val root = (project in file("."))
  .settings(
    name := "RISCV-SpinalHDL",
    libraryDependencies ++= Seq(
      "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion,
      "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion,
      compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)
    ),
    // Preserve the RTL hierarchy under src/, separate from build outputs.
    Compile / scalaSource := baseDirectory.value / "src",
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Compile / run / fork := true,
    Compile / run / mainClass := Some("riscv.Generate")
  )
