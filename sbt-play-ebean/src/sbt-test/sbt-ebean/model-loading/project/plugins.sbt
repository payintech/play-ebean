resolvers ++= DefaultOptions.resolvers(snapshot = true)
addSbtPlugin("com.payintech" % "sbt-play-ebean" % sys.props("play-ebean.version"))
