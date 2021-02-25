/*
 * Copyright (C) 2014 - 2021 PayinTech, SAS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package play.db.ebean.orm.actions;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wraps the annotated action in an Ebean transaction.
 *
 * @since 14.11.27
 */
@With(TransactionalAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
}
