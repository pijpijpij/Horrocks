package com.pij.horrocks;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */

public final class MultipleResultFeature<E, S> implements Feature<E, S> {
    private final Subject<E> event = PublishSubject.create();
    private final Function<E, Observable<Result<S>>> stateModifier;

    public MultipleResultFeature(@NonNull Function<E, Observable<Result<S>>> stateModifier) {
        this.stateModifier = stateModifier;
    }

    @Override
    public void trigger(@NonNull E event) {
        this.event.onNext(event);
    }

    @NonNull
    @Override
    public Observable<? extends Result<S>> result() {
        return event.flatMap(stateModifier::apply);
    }

}
