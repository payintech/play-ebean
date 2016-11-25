/*
 * Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import play.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * EbeanParsedConfigTest.
 *
 * @since 15.04.30
 */
public class EbeanParsedConfigTest {

    /**
     * Parse the configuration.
     *
     * @param config The configuration to parse
     * @return A new {@code EbeanParsedConfig} instance
     * @since 15.04.30
     */
    private EbeanParsedConfig parse(final Map<String, ?> config) {
        return EbeanParsedConfig.parseFromConfig(
            new Configuration(
                ConfigFactory
                    .parseMap(config)
                    .withFallback(ConfigFactory.defaultReference())
            )
        );
    }

    /**
     * @since 15.04.30
     */
    @Test
    public void defaultConfig() {
        final EbeanParsedConfig config = parse(Collections.emptyMap());
        assertThat(config.getDefaultDatasource(), equalTo("default"));
        assertThat(config.getDatasourceModels().size(), equalTo(0));
    }

    /**
     * @since 15.04.30
     */
    @Test
    public void withDatasources() {
        final EbeanParsedConfig config = parse(ImmutableMap.of(
            "ebean.default", Arrays.asList("a", "b"),
            "ebean.other", Collections.singletonList("c")
        ));
        assertThat(config.getDatasourceModels().size(), equalTo(2));
        assertThat(config.getDatasourceModels().get("default"), hasItems("a", "b"));
        assertThat(config.getDatasourceModels().get("other"), hasItems("c"));
    }

    /**
     * @since 15.04.30
     */
    @Test
    public void commaSeparatedModels() {
        final EbeanParsedConfig config = parse(ImmutableMap.of(
            "ebean.default", "a,b"
        ));
        assertThat(config.getDatasourceModels().get("default"), hasItems("a", "b"));
    }

    /**
     * @since 15.04.30
     */
    @Test
    public void customDefault() {
        final EbeanParsedConfig config = parse(ImmutableMap.of(
            "play.ebean.defaultDatasource", "custom"
        ));
        assertThat(config.getDefaultDatasource(), equalTo("custom"));
    }

    /**
     * @since 15.04.30
     */
    @Test
    public void customConfig() {
        final EbeanParsedConfig config = parse(ImmutableMap.of(
            "play.ebean.config", "my.custom",
            "my.custom.default", Collections.singletonList("a")
        ));
        assertThat(config.getDatasourceModels().size(), equalTo(1));
        assertThat(config.getDatasourceModels().get("default"), hasItems("a"));
    }
}
