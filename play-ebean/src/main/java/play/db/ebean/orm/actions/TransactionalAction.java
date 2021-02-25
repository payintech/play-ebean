/*
 * Copyright (C) 2014 - 2021 PayinTech, SAS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package play.db.ebean.orm.actions;

import io.ebean.DB;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Wraps an action in an Ebean transaction.
 *
 * @since 14.11.27
 */
public class TransactionalAction extends Action<Transactional> {

    @Override
    public CompletionStage<Result> call(final Http.Request req) {
        return DB.executeCall(() -> this.delegate.call(req));
    }
}
