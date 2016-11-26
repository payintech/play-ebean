# play-ebean

[![Build Status](https://travis-ci.org/payintech/play-ebean.svg?branch=master)](https://travis-ci.org/payintech/play-ebean)
[![Latest release](https://img.shields.io/badge/latest_release-16.11-orange.svg?style=flat)](https://github.com/0xbaadf00d/play-rabbitmq-module/releases)
[![GitHub license](https://img.shields.io/badge/license-Apache%202%2E0-blue.svg?style=flat)](https://opensource.org/licenses/Apache-2.0)


This module provides Ebean support for Play Framework 2.5 and superior.

*****

## About this project

This project was forked from the original repository [playframework/play-ebean](https://github.com/playframework/play-ebean).




## Configure your Play application


#### project/plugin.sbt

```
resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.payintech" % "sbt-play-ebean" % "YY.MM")
```

You have to replace _YY.MM_ with available release you want to use (see Releases tab).


#### build.sbt

```
resolvers += Resolver.sonatypeRepo("releases")
```




## License
This project is released under terms of the [Apache 2.0](https://opensource.org/licenses/Apache-2.0).
