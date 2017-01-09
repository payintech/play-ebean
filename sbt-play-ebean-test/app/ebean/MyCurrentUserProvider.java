package ebean;

import io.ebean.config.CurrentUserProvider;

public class MyCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Object currentUser() {
        return "John";
    }
}
