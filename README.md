# play-ebean

[![Latest release](https://img.shields.io/badge/latest_release-17.02-orange.svg?style=flat)](https://github.com/payintech/play-ebean/releases)
[![Build Status](https://travis-ci.org/payintech/play-ebean.svg?branch=master)](https://travis-ci.org/payintech/play-ebean)
[![GitHub license](https://img.shields.io/badge/license-Apache%202%2E0-blue.svg?style=flat)](https://opensource.org/licenses/Apache-2.0)


This module provides Ebean support for Play Framework 2.5 and superior.

*****

## About this project

This project was forked from the original repository [playframework/play-ebean](https://github.com/playframework/play-ebean).




## How to use


### Add the module to your Play application

play-ebean can be easily added to your Play application by adding the following line in the file `project/plugin.sbt`. You have to replace _YY.MM_ with the release number you want to use from available [releases](https://github.com/payintech/play-ebean/releases).

```
addSbtPlugin("com.payintech" % "sbt-play-ebean" % "YY.MM")
```

**Note:** If you are already using `sbt-play-ebean` provided by `com.typesafe.sbt`, you
have to comment or remove the line to avoid any conflicts.



### Configure the module

You can configure the module by adding the following keys on your `application.conf` file :

```cfg
## Ebean
# https://github.com/payintech/play-ebean
# ~~~~~
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

        # Set to true to disable L2 caching. Typically useful in performance testing
        disableL2Cache = false

        # Encryption key manager to use for fields annotated with @Encrypted
        encryptKeyManager = "com.zero_x_baadf00d.ebean.encryption.StandardEncryptKeyManager"

        # Set the user provider. This is used to populate @WhoCreated, @WhoModified an
        # support other audit features
        currentUserProvider = "com.zero_x_baadf00d.ebean.provider.CustomUserProvider"

        # Set the tenant provider
        currentTenantProvider = "com.zero_x_baadf00d.ebean.provider.CustomTenantProvider"
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
        
        # Allow connections to document stores (like ElasticSearch) that have
        # self signed certificates
        allowAllCertificates = false
      }
    }
  }

  # Ebean clustering
  # Read more at http://ebean-orm.github.io/docs/features/clustering
  # Note that this is specifically for Ebean's ebean-cluster module (L2 cache
  # implementation - near cache based). And this not required if the L2 cache
  # implementation is instead ebean-hazelcast or ebean-ignite.
  clustering {

    # Is clustering enabled?
    isActive = false

    # Define the "IP" and "PORT" (eg: 127.0.0.1:9942) of the current node
    currentNode = "127.0.0.1:9942"

    # Define all members of the cluster. This list must include the current node too
    members = [
      "127.0.0.1:9942"
    ]
  }

  # Ebean DB Migration
  # Read more at https://github.com/ebean-orm/ebean-dbmigration
  dbmigration {

    # Is Ebean DB Migration enabled?
    enabled = false

    # Defines where are located migration SQL scripts. Ebean DB Migration
    # will search SQL scripts in "conf/${migrationPath}/${serverName}-${appMode}"
    # or "conf/${migrationPath}/${serverName}"
    #
    # By example, in your run your application in development mode:
    #     conf/dbmigration/default-dev/
    #  OR conf/dbmigration/default/
    migrationPath = "dbmigration"

    # Is the migration must be auto applied?
    autoApply = false
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
  "io.ebean" % "ebean" % "X.Y.Z"
)

dependencyOverrides ++= Set(
  ...
  "io.ebean" % "ebean" % "X.Y.Z"
)
```




## License
This project is released under terms of the [Apache 2.0](https://opensource.org/licenses/Apache-2.0).
