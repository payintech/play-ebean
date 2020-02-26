/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean.orm;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;
import play.api.db.evolutions.DynamicEvolutions;
import play.inject.Binding;
import play.inject.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Injection module with default Ebean components.
 *
 * @since 14.11.27
 */
public class EbeanModule extends Module {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<Binding<?>> bindings(final Environment environment, final Config config) {
        this.logger.trace("Loading module PlayEbean");
        final List<Binding<?>> bindings = new ArrayList<>();

        bindings.add(bindClass(DynamicEvolutions.class).to(EbeanDynamicEvolutions.class).eagerly());
        bindings.add(bindClass(EbeanConfig.class).toProvider(DefaultEbeanConfig.EbeanConfigParser.class).eagerly());
        return bindings;
    }
}
