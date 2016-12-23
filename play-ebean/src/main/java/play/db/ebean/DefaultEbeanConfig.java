/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import io.ebean.config.DocStoreConfig;
import io.ebean.config.EncryptKeyManager;
import io.ebean.config.ServerConfig;
import com.typesafe.config.Config;
import play.Configuration;
import play.Environment;
import play.db.DBApi;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ebean server configuration.
 *
 * @since 14.11.27
 */
@Singleton
public class DefaultEbeanConfig implements EbeanConfig {

    /**
     * Default server name.
     *
     * @since 14.11.27
     */
    private final String defaultServer;

    /**
     * Server configuration.
     *
     * @since 14.11.27
     */
    private final Map<String, ServerConfig> serverConfigs;

    /**
     * Build a pre-configured instance.
     *
     * @param defaultServer The server name (eg: "default")
     * @param serverConfigs The server configuration
     * @since 14.11.27
     */
    public DefaultEbeanConfig(final String defaultServer, final Map<String, ServerConfig> serverConfigs) {
        this.defaultServer = defaultServer;
        this.serverConfigs = serverConfigs;
    }

    @Override
    public String defaultServer() {
        return this.defaultServer;
    }

    @Override
    public Map<String, ServerConfig> serverConfigs() {
        return this.serverConfigs;
    }

    /**
     * EbeanConfigParser.
     *
     * @since 14.11.27
     */
    @Singleton
    public static class EbeanConfigParser implements Provider<EbeanConfig> {

        /**
         * @since 14.11.27
         */
        private final Configuration configuration;

        /**
         * @since 14.11.27
         */
        private final Environment environment;

        /**
         * @since 14.11.27
         */
        private final DBApi dbApi;

        /**
         * Build a pre-configured configuration parser.
         *
         * @param configuration The current Play configuration
         * @param environment   The current Play environment
         * @param dbApi         DB API for managing application databases
         * @since 14.11.27
         */
        @Inject
        public EbeanConfigParser(final Configuration configuration, final Environment environment, final DBApi dbApi) {
            this.configuration = configuration;
            this.environment = environment;
            this.dbApi = dbApi;
        }

        @Override
        public EbeanConfig get() {
            return this.parse();
        }

        /**
         * Reads the configuration and creates configuration for Ebean servers.
         *
         * @return A configuration for Ebean servers
         * @since 14.11.27
         */
        public EbeanConfig parse() {
            final Map<String, ServerConfig> serverConfigs = new HashMap<>();
            final Config underliedConfiguration = this.configuration.underlying();

            if (underliedConfiguration.hasPathOrNull("ebean.servers")) {
                final Config playEbeanSrvCfg = underliedConfiguration.getConfig("ebean.servers");
                playEbeanSrvCfg.root().keySet().forEach(serverName -> {
                    final Config ebeanServerConfig = playEbeanSrvCfg.getConfig(serverName);
                    final ServerConfig serverConfig = new ServerConfig();
                    serverConfig.setName(serverName);
                    serverConfig.loadFromProperties();
                    serverConfig.setH2ProductionMode(true);  // Since Ebean 9.1.1: Don't override Evolution
                    if (serverName.compareTo("default") == 0) {
                        serverConfig.setDefaultServer(true);
                    }

                    if (ebeanServerConfig.hasPath("settings")) {
                        try {
                            final Config playEbeanSrvSettingsCfg = ebeanServerConfig.getConfig("settings");
                            if (playEbeanSrvSettingsCfg.hasPath("onlyUseDocstore") && ebeanServerConfig.hasPath("docstore")) {
                                if (playEbeanSrvSettingsCfg.getBoolean("onlyUseDocStore")) {
                                    serverConfig.setDocStoreOnly(true);
                                } else {
                                    serverConfig.setDocStoreOnly(false);
                                    this.setServerConfigDataSource(serverName, serverConfig);
                                }
                            } else {
                                this.setServerConfigDataSource(serverName, serverConfig);
                            }
                            if (playEbeanSrvSettingsCfg.hasPath("encryptKeyManager")) {
                                final EncryptKeyManager encryptKeyManager = (EncryptKeyManager) serverConfig
                                    .getClassLoadConfig()
                                    .newInstance(playEbeanSrvSettingsCfg.getString("encryptKeyManager"));
                                serverConfig.setEncryptKeyManager(encryptKeyManager);
                            }
                        } catch (final Exception e) {
                            throw this.configuration.reportError(
                                "ebean.servers" + serverName + ".settings",
                                e.getMessage(),
                                e
                            );
                        }
                    } else {
                        serverConfig.setDocStoreOnly(false);
                        this.setServerConfigDataSource(serverName, serverConfig);
                    }

                    if (ebeanServerConfig.hasPath("enhancement")) {
                        final Set<String> classes = new HashSet<>();
                        ebeanServerConfig.getStringList("enhancement").stream().map(String::trim).forEach(load -> {
                            if (load.endsWith(".*")) {
                                classes.addAll(
                                    play.libs.Classpath
                                        .getTypes(
                                            this.environment,
                                            load.substring(0, load.length() - 2)
                                        )
                                );
                            } else {
                                classes.add(load);
                            }
                        });
                        this.addModelClassesToServerConfig(serverName, serverConfig, classes);
                    }

                    if (ebeanServerConfig.hasPath("docstore")) {
                        try {
                            Class.forName("io.ebeanservice.elastic.ElasticDocumentStore");
                        } catch (ClassNotFoundException e) {
                            throw this.configuration.reportError(
                                "ebean.servers" + serverName + ".docstore",
                                "The class \"ElasticDocumentStore\" was not found! Please add the following dependency in your project:" +
                                    "\n\n" +
                                    "\t\t\"io.ebean\" % \"ebean-elastic\" % \"2.1.1\"",
                                e
                            );
                        }
                        try {
                            final Config playEbeanSrvDocStoreCfg = ebeanServerConfig.getConfig("docstore");
                            final DocStoreConfig docStoreConfig = new DocStoreConfig();
                            if (playEbeanSrvDocStoreCfg.hasPath("url")) {
                                docStoreConfig.setUrl(playEbeanSrvDocStoreCfg.getString("url"));
                            } else {
                                docStoreConfig.setUrl("http://127.0.0.1:9200");
                            }
                            if (playEbeanSrvDocStoreCfg.hasPath("active")) {
                                docStoreConfig.setActive(playEbeanSrvDocStoreCfg.getBoolean("active"));
                            }
                            if (playEbeanSrvDocStoreCfg.hasPath("generateMapping")) {
                                docStoreConfig.setGenerateMapping(playEbeanSrvDocStoreCfg.getBoolean("generateMapping"));
                            }
                            if (playEbeanSrvDocStoreCfg.hasPath("dropCreate")) {
                                docStoreConfig.setDropCreate(playEbeanSrvDocStoreCfg.getBoolean("dropCreate"));
                            }
                            if (playEbeanSrvDocStoreCfg.hasPath("create")) {
                                docStoreConfig.setCreate(playEbeanSrvDocStoreCfg.getBoolean("create"));
                            }
                            if (playEbeanSrvDocStoreCfg.hasPath("pathToResources")) {
                                docStoreConfig.setPathToResources(playEbeanSrvDocStoreCfg.getString("pathToResources"));
                            } else {
                                docStoreConfig.setPathToResources("conf");
                            }
                            serverConfig.setDocStoreConfig(docStoreConfig);
                        } catch (final Exception e) {
                            throw this.configuration.reportError(
                                "ebean.servers" + serverName + ".docstore",
                                e.getMessage(),
                                e
                            );
                        }
                    }

                    serverConfigs.put(serverName, serverConfig);
                });
            } else {
                throw new RuntimeException("Bad play-ebean configuration, check your application.conf file");
            }

            return new DefaultEbeanConfig("default", serverConfigs);
        }

        /**
         * Set the datasource from DB API to the ebean server configuration.
         *
         * @param key          The server name
         * @param serverConfig The server configuration to apply
         * @since 14.11.27
         */
        private void setServerConfigDataSource(final String key, final ServerConfig serverConfig) {
            try {
                serverConfig.setDataSource(
                    new WrappingDatasource(
                        this.dbApi
                            .getDatabase(key)
                            .getDataSource()
                    )
                );
            } catch (Exception e) {
                throw this.configuration.reportError(
                    "ebean.servers." + key,
                    e.getMessage(),
                    e
                );
            }
        }

        /**
         * Add model classes to server configuration.
         *
         * @param key          The server name
         * @param serverConfig The server configuration
         * @param classes      The class to add
         * @since 14.11.27
         */
        private void addModelClassesToServerConfig(final String key, final ServerConfig serverConfig, final Set<String> classes) {
            for (final String clazz : classes) {
                try {
                    serverConfig.addClass(Class.forName(clazz, true, this.environment.classLoader()));
                } catch (Throwable e) {
                    throw this.configuration.reportError(
                        "ebean.servers." + key,
                        "Cannot register class [" + clazz + "] in Ebean server",
                        e
                    );
                }
            }
        }

        /**
         * <code>DataSource</code> wrapper to ensure that every retrieved
         * connection has auto-commit disabled.
         *
         * @since 14.11.27
         */
        static class WrappingDatasource implements javax.sql.DataSource {

            /**
             * @since 14.11.27
             */
            final javax.sql.DataSource wrapped;

            /**
             * Build a default instance.
             *
             * @param wrapped The {@code DataSource} object to wrap
             * @since 14.11.27
             */
            public WrappingDatasource(final javax.sql.DataSource wrapped) {
                this.wrapped = wrapped;
            }

            /**
             * Wrap the connection. This method ensure that the given
             * connection have 'auto-commit' set to {@code false}.
             *
             * @param connection The SQL connection
             * @return The SQL connection with 'auto-commit' set to {@code false}
             * @throws java.sql.SQLException If a database connection error occurs
             * @since 14.11.27
             */
            public java.sql.Connection wrap(final java.sql.Connection connection) throws java.sql.SQLException {
                connection.setAutoCommit(false);
                return connection;
            }

            @Override
            public java.sql.Connection getConnection() throws java.sql.SQLException {
                return this.wrap(this.wrapped.getConnection());
            }

            @Override
            public java.sql.Connection getConnection(final String username, final String password) throws java.sql.SQLException {
                return this.wrap(this.wrapped.getConnection(username, password));
            }

            @Override
            public int getLoginTimeout() throws java.sql.SQLException {
                return this.wrapped.getLoginTimeout();
            }

            @Override
            public void setLoginTimeout(final int seconds) throws java.sql.SQLException {
                this.wrapped.setLoginTimeout(seconds);
            }

            @Override
            public java.io.PrintWriter getLogWriter() throws java.sql.SQLException {
                return this.wrapped.getLogWriter();
            }

            @Override
            public void setLogWriter(final java.io.PrintWriter out) throws java.sql.SQLException {
                this.wrapped.setLogWriter(out);
            }

            @Override
            public boolean isWrapperFor(final Class<?> iface) throws java.sql.SQLException {
                return this.wrapped.isWrapperFor(iface);
            }

            @Override
            public <T> T unwrap(final Class<T> iface) throws java.sql.SQLException {
                return this.wrapped.unwrap(iface);
            }

            @Override
            public java.util.logging.Logger getParentLogger() {
                return null;
            }
        }
    }
}
