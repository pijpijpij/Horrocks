package com.pij.horrocks;

import android.support.annotation.NonNull;

/**
 * <p>Created on 17/11/2017.</p>
 *
 * @author PierreJean
 */
public interface Logger {
    <T> void print(@NonNull Class<T> javaClass, @NonNull String message);

    <T> void print(@NonNull Class<T> aClass, @NonNull String message, @NonNull Throwable e);
}