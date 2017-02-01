/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean.orm;

import io.ebean.Ebean;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Wraps an action in an Ebean transaction.
 *
 * @since 14.11.27
 */
public class TransactionalAction extends Action<Transactional> {

    @Override
    public CompletionStage<Result> call(final Context ctx) {
        return Ebean.execute(() -> delegate.call(ctx));
    }
}
