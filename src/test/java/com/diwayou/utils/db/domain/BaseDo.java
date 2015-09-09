package com.diwayou.utils.db.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by cn40387 on 15/9/9.
 */
public class BaseDo {

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
