package ebean;

import com.typesafe.config.Config;
import io.ebean.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.ebean.orm.EbeanServerExtraConfig;

public class DemoEbeanServerExtraConfig implements EbeanServerExtraConfig {

    private final Logger logger;

    public DemoEbeanServerExtraConfig() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void applyExtraConfiguration(final DatabaseConfig ebeanServerConfig, final Config playConfig) {
        this.logger.info("Applying ebean server extra configuration");
    }
}
