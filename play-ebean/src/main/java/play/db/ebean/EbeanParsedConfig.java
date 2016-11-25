/*
 * Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;
import play.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The raw parsed config from Ebean, as opposed to the EbeanConfig which
 * actually requires starting database connection pools to create.
 *
 * @since 15.04.30
 */
public class EbeanParsedConfig {

    /**
     * @since 15.04.30
     */
    private final String defaultDatasource;

    /**
     * @since 15.04.30
     */
    private final Map<String, List<String>> datasourceModels;

    /**
     * Build default instance.
     *
     * @param defaultDatasource The datasource name
     * @param datasourceModels  A list of models linked to this datasource
     * @since 15.04.30
     */
    public EbeanParsedConfig(final String defaultDatasource, final Map<String, List<String>> datasourceModels) {
        this.defaultDatasource = defaultDatasource;
        this.datasourceModels = datasourceModels;
    }

    /**
     * Retrieve a {@code EbeanParsedConfig} instance from a PlayFramework
     * {@code Configuration} instance.
     *
     * @param configuration The configuration to parse
     * @return A new instance of {@code EbeanParsedConfig}
     * @since 15.04.30
     */
    public static EbeanParsedConfig parseFromConfig(final Configuration configuration) {
        final Config config = configuration.underlying();
        final Config playEbeanConfig = config.getConfig("play.ebean");
        final String defaultDatasource = playEbeanConfig.getString("defaultDatasource");
        final String ebeanConfigKey = playEbeanConfig.getString("config");
        final Map<String, List<String>> datasourceModels = new HashMap<>();

        if (config.hasPath(ebeanConfigKey)) {
            final Config ebeanConfig = config.getConfig(ebeanConfigKey);
            ebeanConfig.root().forEach((key, raw) -> {
                final List<String> models;
                if (raw.valueType() == ConfigValueType.STRING) {
                    // Support legacy comma separated string
                    models = Arrays.asList(((String) raw.unwrapped()).split(","));
                } else {
                    models = ebeanConfig.getStringList(key);
                }

                datasourceModels.put(key, models);
            });
        }
        return new EbeanParsedConfig(defaultDatasource, datasourceModels);
    }

    /**
     * Return default datasource name.
     *
     * @return Default datasource name
     * @since 15.04.30
     */
    public String getDefaultDatasource() {
        return this.defaultDatasource;
    }

    /**
     * Return all datasource models.
     *
     * @return All datasource models
     * @since 15.04.30
     */
    public Map<String, List<String>> getDatasourceModels() {
        return this.datasourceModels;
    }
}
