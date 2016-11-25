/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.avaje.ebean.config.ServerConfig;
import play.Configuration;
import play.Environment;
import play.db.DBApi;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

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
            final EbeanParsedConfig config = EbeanParsedConfig.parseFromConfig(this.configuration);
            final Map<String, ServerConfig> serverConfigs = new HashMap<>();

            for (final Map.Entry<String, List<String>> entry : config.getDatasourceModels().entrySet()) {
                final String key = entry.getKey();

                final ServerConfig serverConfig = new ServerConfig();
                serverConfig.setName(key);
                serverConfig.loadFromProperties();
                serverConfig.setH2ProductionMode(true);  // Since Ebean 9.1.1: Don't override Evolution

                this.setServerConfigDataSource(key, serverConfig);
                if (config.getDefaultDatasource().equals(key)) {
                    serverConfig.setDefaultServer(true);
                }

                final Set<String> classes = this.getModelClasses(entry);
                this.addModelClassesToServerConfig(key, serverConfig, classes);

                serverConfigs.put(key, serverConfig);
            }

            return new DefaultEbeanConfig(config.getDefaultDatasource(), serverConfigs);
        }

        /**
         * Set the database server configuration.
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
                    "ebean." + key,
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
                        "ebean." + key,
                        "Cannot register class [" + clazz + "] in Ebean server",
                        e
                    );
                }
            }
        }

        /**
         * Return all model classes from the given entry.
         *
         * @param entry The entry to scan
         * @return The model classes
         * @since 16.02.18
         */
        private Set<String> getModelClasses(final Map.Entry<String, List<String>> entry) {
            final Set<String> classes = new HashSet<>();
            entry
                .getValue()
                .forEach(load -> {
                    load = load.trim();
                    if (load.endsWith(".*")) {
                        classes.addAll(
                            play.libs.Classpath
                                .getTypes(
                                    this.environment,
                                    load.substring(
                                        0,
                                        load.length() - 2
                                    )
                                )
                        );
                    } else {
                        classes.add(load);
                    }
                });

            return classes;
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
