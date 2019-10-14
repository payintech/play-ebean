/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean.orm;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigOrigin;
import io.ebean.EbeanServerFactory;
import io.ebean.config.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import play.Environment;
import play.api.PlayException;
import play.db.DBApi;
import scala.Option;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Class.forName;

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
        private final Config configuration;

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
        public EbeanConfigParser(final Config configuration, final Environment environment, final DBApi dbApi) {
            this.configuration = configuration;
            this.environment = environment;
            this.dbApi = dbApi;
        }

        @Override
        public EbeanConfig get() {
            return this.parse();
        }

        /**
         * Throw a "Configuration error" exception.
         *
         * @param origin  The configuration file containing the error
         * @param message The message to print on top of the page
         * @param t       The current exception
         * @since 17.07.06
         */
        private void throwConfigurationException(final Option<ConfigOrigin> origin, final String message, final Throwable t) {
            throw new PlayException.ExceptionSource(
                "Configuration error",
                message,
                t
            ) {
                @Override
                public Integer line() {
                    if (origin.nonEmpty()) {
                        return origin
                            .get()
                            .lineNumber();
                    }
                    return null;
                }

                @Override
                public Integer position() {
                    return null;
                }

                @Override
                public String input() {
                    if (origin.nonEmpty()) {
                        return origin.get().url().toString();
                    }
                    return null;
                }

                @Override
                public String sourceName() {
                    if (origin.nonEmpty()) {
                        return origin.get().filename();
                    }
                    return null;
                }
            };
        }

        /**
         * Reads the configuration and creates configuration for Ebean servers.
         *
         * @return A configuration for Ebean servers
         * @since 14.11.27
         */
        EbeanConfig parse() {
            final Map<String, ServerConfig> serverConfigs = new HashMap<>();

            if (this.configuration.hasPathOrNull("ebean.clustering")) {
                final Config playEbeanClusteringCfg = this.configuration.getConfig("ebean.clustering");
                if (playEbeanClusteringCfg.hasPath("isActive") && playEbeanClusteringCfg.getBoolean("isActive")) {
                    final ContainerConfig containerConfig = new ContainerConfig();
                    final Properties properties = new Properties();
                    if (playEbeanClusteringCfg.hasPath("currentNode")) {
                        properties.setProperty(
                            "ebean.cluster.localHostPort",
                            playEbeanClusteringCfg.getString("currentNode")
                        );
                    }
                    if (playEbeanClusteringCfg.hasPath("members")) {
                        properties.setProperty(
                            "ebean.cluster.members",
                            playEbeanClusteringCfg.getStringList("members")
                                .stream()
                                .map(String::trim)
                                .collect(Collectors.joining(","))
                        );
                    }
                    containerConfig.setActive(true);
                    containerConfig.setProperties(properties);
                    EbeanServerFactory.initialiseContainer(containerConfig);
                }
            }

            if (this.configuration.hasPathOrNull("ebean.servers")) {
                final Config playEbeanSrvCfg = this.configuration.getConfig("ebean.servers");
                playEbeanSrvCfg.root().keySet().forEach(serverName -> {
                    final Config ebeanServerConfig = playEbeanSrvCfg.getConfig(serverName);
                    final ServerConfig serverConfig = new ServerConfig();
                    serverConfig.setName(serverName);
                    serverConfig.loadFromProperties();
                    if (serverName.compareTo("default") == 0) {
                        serverConfig.setDefaultServer(true);
                    } else {
                        serverConfig.setDefaultServer(false);
                    }

                    if (ebeanServerConfig.hasPath("settings")) {
                        try {
                            final Config playEbeanSrvSettingsCfg = ebeanServerConfig.getConfig("settings");
                            if (playEbeanSrvSettingsCfg.hasPath("onlyUseDocStore") && ebeanServerConfig.hasPath("docstore")) {
                                if (playEbeanSrvSettingsCfg.getBoolean("onlyUseDocStore")) {
                                    serverConfig.setDocStoreOnly(true);
                                } else {
                                    serverConfig.setDocStoreOnly(false);
                                    this.setServerConfigDataSource(serverName, serverConfig);
                                }
                            } else {
                                this.setServerConfigDataSource(serverName, serverConfig);
                            }
                            if (playEbeanSrvSettingsCfg.hasPath("allQuotedIdentifiers")) {
                                serverConfig.setAllQuotedIdentifiers(
                                    playEbeanSrvSettingsCfg.getBoolean("allQuotedIdentifiers")
                                );
                            }
                            if (playEbeanSrvSettingsCfg.hasPath("encryptKeyManager")) {
                                final EncryptKeyManager encryptKeyManager = (EncryptKeyManager) serverConfig
                                    .getClassLoadConfig()
                                    .newInstance(playEbeanSrvSettingsCfg.getString("encryptKeyManager"));
                                serverConfig.setEncryptKeyManager(encryptKeyManager);
                            }
                            if (playEbeanSrvSettingsCfg.hasPath("currentUserProvider")) {
                                final CurrentUserProvider currentUserProvider = (CurrentUserProvider) serverConfig
                                    .getClassLoadConfig()
                                    .newInstance(playEbeanSrvSettingsCfg.getString("currentUserProvider"));
                                serverConfig.setCurrentUserProvider(currentUserProvider);
                            }
                            if (playEbeanSrvSettingsCfg.hasPath("currentTenantProvider")) {
                                final CurrentTenantProvider currentTenantProvider = (CurrentTenantProvider) serverConfig
                                    .getClassLoadConfig()
                                    .newInstance(playEbeanSrvSettingsCfg.getString("currentTenantProvider"));
                                serverConfig.setCurrentTenantProvider(currentTenantProvider);
                            }
                            if (playEbeanSrvSettingsCfg.hasPath("disableL2Cache")) {
                                serverConfig.setDisableL2Cache(
                                    playEbeanSrvSettingsCfg.getBoolean("disableL2Cache")
                                );
                            }
                        } catch (final Exception ex) {
                            final Option<ConfigOrigin> origin = this.configuration.hasPath("ebean.servers" + serverName + ".settings") ?
                                Option.apply(this.configuration.getValue("ebean.servers" + serverName + ".settings").origin()) :
                                Option.apply(this.configuration.root().origin());
                            throwConfigurationException(origin, ex.getMessage(), ex);
                        }
                    } else {
                        serverConfig.setDocStoreOnly(false);
                        this.setServerConfigDataSource(serverName, serverConfig);
                    }

                    if (ebeanServerConfig.hasPath("enhancement")) {
                        final Set<String> classes = new HashSet<>();
                        ebeanServerConfig.getStringList("enhancement").stream().map(String::trim).forEach(load -> {
                            if (load.endsWith(".*")) {
                                final String packageName = load.substring(0, load.length() - 2);
                                final Reflections reflections = new Reflections(
                                    new ConfigurationBuilder()
                                        .addUrls(ClasspathHelper.forPackage(packageName, environment.classLoader()))
                                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName + ".")))
                                        .setScanners(new TypeElementsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner())
                                );
                                classes.addAll(
                                    reflections
                                        .getStore()
                                        .get(TypeElementsScanner.class.getSimpleName())
                                        .keySet()
                                );
                            } else {
                                classes.add(load);
                            }
                        });
                        this.addModelClassesToServerConfig(serverName, serverConfig, classes);
                    }

                    if (ebeanServerConfig.hasPath("docstore")) {
                        try {
                            forName("io.ebeanservice.elastic.ElasticDocumentStore");
                        } catch (ClassNotFoundException ex) {
                            final Option<ConfigOrigin> origin = this.configuration.hasPath("ebean.servers" + serverName + ".docstore") ?
                                Option.apply(this.configuration.getValue("ebean.servers" + serverName + ".docstore").origin()) :
                                Option.apply(this.configuration.root().origin());
                            throwConfigurationException(origin, ex.getMessage(), ex);
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
                            if (playEbeanSrvDocStoreCfg.hasPath("allowAllCertificates")) {
                                docStoreConfig.setAllowAllCertificates(playEbeanSrvDocStoreCfg.getBoolean("allowAllCertificates"));
                            } else {
                                docStoreConfig.setAllowAllCertificates(false);
                            }
                            serverConfig.setDocStoreConfig(docStoreConfig);
                        } catch (final Exception ex) {
                            final Option<ConfigOrigin> origin = this.configuration.hasPath("ebean.servers" + serverName + ".docstore") ?
                                Option.apply(this.configuration.getValue("ebean.servers" + serverName + ".docstore").origin()) :
                                Option.apply(this.configuration.root().origin());
                            throwConfigurationException(origin, ex.getMessage(), ex);
                        }
                    }

                    if (ebeanServerConfig.hasPath("extra-config")) {
                        ebeanServerConfig.getStringList("extra-config").forEach(className -> {
                            try {
                                final EbeanServerExtraConfig esac = (EbeanServerExtraConfig) forName(
                                    className,
                                    true,
                                    this.environment.classLoader()
                                ).newInstance();
                                esac.applyExtraConfiguration(serverConfig, this.configuration);
                            } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                                ex.printStackTrace();
                            }
                        });
                    }

                    serverConfigs.put(serverName, serverConfig);
                });
            } else {
                throw new RuntimeException("Bad play-ebean configuration, check your configuration file");
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
            } catch (Exception ex) {
                final Option<ConfigOrigin> origin = this.configuration.hasPath("ebean.servers." + key) ?
                    Option.apply(this.configuration.getValue("ebean.servers." + key).origin()) :
                    Option.apply(this.configuration.root().origin());
                throwConfigurationException(origin, ex.getMessage(), ex);
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
        private void addModelClassesToServerConfig(final String key, final ServerConfig serverConfig,
                                                   final Set<String> classes) {
            for (final String clazz : classes) {
                try {
                    serverConfig.addClass(forName(clazz, true, this.environment.classLoader()));
                } catch (Throwable ex) {
                    final Option<ConfigOrigin> origin = this.configuration.hasPath("ebean.servers." + key) ?
                        Option.apply(this.configuration.getValue("ebean.servers." + key).origin()) :
                        Option.apply(this.configuration.root().origin());
                    throwConfigurationException(
                        origin,
                        "Cannot register class [" + clazz + "] in Ebean server",
                        ex
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
            WrappingDatasource(final javax.sql.DataSource wrapped) {
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
            java.sql.Connection wrap(final java.sql.Connection connection) throws java.sql.SQLException {
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
