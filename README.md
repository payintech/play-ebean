# play-ebean

[![Build Status](https://travis-ci.org/payintech/play-ebean.svg?branch=master)](https://travis-ci.org/payintech/play-ebean)
[![Latest release](https://img.shields.io/badge/latest_release-16.12-orange.svg?style=flat)](https://github.com/payintech/play-ebean/releases)
[![GitHub license](https://img.shields.io/badge/license-Apache%202%2E0-blue.svg?style=flat)](https://opensource.org/licenses/Apache-2.0)


This module provides Ebean support for Play Framework 2.5 and superior.

*****

## About this project

This project was forked from the original repository [playframework/play-ebean](https://github.com/playframework/play-ebean).




## How to use


### Add the module to your Play appliction

![Settings](https://www.iconfinder.com/icons/465051/download/png/16) **project/plugin.sbt**
```
resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.payintech" % "sbt-play-ebean" % "YY.MM")
```

You have to replace _YY.MM_ with available release you want to use (see Releases tab).


![Settings](https://www.iconfinder.com/icons/465051/download/png/16) **build.sbt**

```
resolvers += Resolver.sonatypeRepo("releases")
```


### Configure the module

You can configure the module by adding the following keys on your `application.conf` file :

```cfg
# Ebean
# ~~~~~
# https://github.com/payintech/play-ebean
ebean {
  servers {

    # You can declare as many servers as you want.
    # By convention, the default server is named `default`
    default {

      # Locations of the classes to enhance
      enhancement = ["models.*"]

      # Extra server settings
      settings {
      
        # Set to true if this server is Document store only
        onlyUseDocStore = false
      
        # Encryption key manager to use for fields annotated with @Encrypted
        encryptKeyManager = "com.zero_x_baadf00d.ebean.encryption.StandardEncryptKeyManager"
      }

      # Document store
      docstore {
      
        # URL of the ElasticSearch server to use
        url = "http://127.0.0.1:9200"
      
        # Enable document store integration
        active = true
      
        # Set the relative file system path to resources when generating mapping files
        pathToResources = "conf"
      
        # Generate mapping files for each index and these will by default be
        # generated into ${pathToResources} under "elastic-mapping"
        generateMapping = false
      
        # Drop and re-create all indexes
        dropCreate = false
      
        # Create only indexes that have not already been defined
        create = false
      }
    }
  }
}
```



### Override Ebean version

In case you need to use a newest version of Ebean, you have the possibility
to override built-in Ebean version by adding these lines in your `build.sbt`
file.

```sbt
libraryDependencies ++= Seq(
  ...
  "org.avaje.ebean" % "ebean" % "X.Y.Z"
)

dependencyOverrides ++= Set(
  ...
  "org.avaje.ebean" % "ebean" % "X.Y.Z"
)
```




## License
This project is released under terms of the [Apache 2.0](https://opensource.org/licenses/Apache-2.0).
