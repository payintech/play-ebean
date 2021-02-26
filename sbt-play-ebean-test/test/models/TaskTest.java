/*
 * Copyright (C) 2014 - 2021 PayinTech, SAS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package models;

import io.ebean.DB;
import org.junit.Assert;
import org.junit.Test;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * The type Task test.
 */
public class TaskTest extends WithApplication {

    protected Application provideApplication() {
        final Map<String, Object> appConfig = new HashMap<>();
        appConfig.put("ebean.servers.default.enhancement", new ArrayList<String>() {{
            add("models.*");
        }});
        appConfig.putAll(Helpers.inMemoryDatabase());
        return Helpers.fakeApplication(appConfig);
    }

    /**
     * Save and find.
     */
    @Test
    public void saveAndFind() {
        Task task = new Task();
        task.id = 10L;
        task.name = "Hello";
        task.done = false;
        task.save();

        Task saved = Task.find.byId(10L);
        Assert.assertNotNull(saved);
        Assert.assertEquals("Hello", saved.name);
    }

    /**
     * Default server.
     */
    @Test
    public void defaultServer() {
        assertEquals("default", DB.getDefault().getName());
    }
}
