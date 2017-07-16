scalaVersion in ThisBuild        := "2.10.4"
dependencyOverrides in ThisBuild += "com.github.nscala-time" %% "nscala-time" % "1.0.0"

lazy val root = (project in file("."))
  .dependsOn(p1 % Compile)
  .settings(
    inThisBuild(List(
      organizationName := "eed3si9n",
      organizationHomepage := Some(url("http://example.com/")),
      homepage := Some(url("https://github.com/example/example")),
      scmInfo := Some(ScmInfo(url("https://github.com/example/example"), "git@github.com:example/example.git")),
      developers := List(
        Developer("harrah", "Mark Harrah", "@harrah", url("https://github.com/harrah")),
        Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n")),
        Developer("jsuereth", "Josh Suereth", "@jsuereth", url("https://github.com/jsuereth")),
        Developer("dwijnand", "Dale Wijnand", "@dwijnand", url("https://github.com/dwijnand")),
        Developer("gkossakowski", "Grzegorz Kossakowski", "@gkossakowski", url("https://github.com/gkossakowski")),
        Developer("Duhemm", "Martin Duhem", "@Duhemm", url("https://github.com/Duhemm"))
      ),
      version := "0.3.1-SNAPSHOT",
      description := "An HTTP client for Scala with Async Http Client underneath.",
      licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    )),
    ivyPaths := IvyPaths(
      (baseDirectory in ThisBuild).value,
      Some((baseDirectory in LocalRootProject).value / "ivy-cache")
    ),
    libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.0.0",

    // https://github.com/sbt/sbt/pull/1620
    // sbt resolves dependencies every compile when using %% with dependencyOverrides
    TaskKey[Unit]("check") := {
      val s = (streams in update).value
      val cacheStoreFactory = s.cacheStoreFactory sub updateCacheName.value
      val module = ivyModule.value
      val config = updateConfiguration.value

      import sbt.librarymanagement.ivy.IvyConfiguration
      import sbt.librarymanagement.{ ModuleSettings, UpdateConfiguration }
      import sbt.internal.util.HListFormats._

      type In = IvyConfiguration :+: ModuleSettings :+: UpdateConfiguration :+: HNil

      import sbt.util.CacheImplicits._
      import sbt.internal.util.HListFormats._
      import sbt.internal.AltLibraryManagementCodec._

      val f: In => Unit =
        Tracked.inputChanged(cacheStoreFactory make "inputs") { (inChanged: Boolean, in: In) =>
          if (inChanged)
            sys.error(s"Update cache is invalidated: ${module.owner.configuration}, ${module.moduleSettings}, $config")
        }
      f(module.owner.configuration :+: module.moduleSettings :+: config :+: HNil)
    },

    // https://github.com/sbt/sbt/issues/3226
    // update caching is not working on sbt 1.0.x
    TaskKey[Unit]("check2") := {
      val ur = update.value
      if (!ur.stats.cached) {
        sys.error(s"update.value is not cached! $ur")
      }
      val tu = transitiveUpdate.value
      if (tu.exists(!_.stats.cached)) {
        sys.error(s"uncached transitiveUpdate exists! $tu")
      }
    }
  )

lazy val p1 = project
  .settings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11"
  )
