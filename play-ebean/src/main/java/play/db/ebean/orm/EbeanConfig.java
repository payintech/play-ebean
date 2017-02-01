/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean.orm;

import io.ebean.config.ServerConfig;

import java.util.Map;

/**
 * Ebean configuration.
 *
 * @since 14.11.27
 */
public interface EbeanConfig {

    /**
     * Return the default server name.
     *
     * @return The default server name
     * @since 14.11.27
     */
    String defaultServer();

    /**
     * Return the server configurations.
     *
     * @return The server configurations
     * @since 14.11.27
     */
    Map<String, ServerConfig> serverConfigs();
}
