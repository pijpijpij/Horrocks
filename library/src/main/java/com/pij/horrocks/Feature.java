package com.pij.horrocks;

import android.support.annotation.NonNull;

import io.reactivex.Observable;

/**
 * <p>Created on 16/11/2017.</p>
 *
 * @author PierreJean
 */
public interface Feature<E, S> {

    void trigger(@NonNull E event);

    @NonNull
    Observable<? extends Result<S>> result();

}