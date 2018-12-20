package ebean;

import com.typesafe.config.Config;
import io.ebean.config.ServerConfig;
import play.Logger;
import play.db.ebean.orm.EbeanServerExtraConfig;

public class DemoEbeanServerExtraConfig implements EbeanServerExtraConfig {

    @Override
    public void applyExtraConfiguration(ServerConfig ebeanServerConfig, Config playConfig) {
        Logger.info("Applying ebean server extra configuration");
    }
}
