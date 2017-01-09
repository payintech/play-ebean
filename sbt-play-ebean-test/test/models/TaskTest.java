package models;

import org.junit.Test;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TaskTest extends WithApplication {

    protected Application provideApplication() {
        final Map<String, Object> appConfig = new HashMap<String, Object>() {{
            put("ebean.servers.default.enhancement", new ArrayList<String>(){{
                add("models.*");
            }});
        }};
        appConfig.putAll(Helpers.inMemoryDatabase());
        return Helpers.fakeApplication(appConfig);
    }

    @Test
    public void saveAndFind() {
        Task task = new Task();
        task.id = 10L;
        task.name = "Hello";
        task.done = false;
        task.save();

        Task saved = Task.find.byId(10L);
        assertEquals("Hello", saved.name);
        assertEquals("John", saved.whoCreated);
        assertEquals("John", saved.whoModified);
    }
}
