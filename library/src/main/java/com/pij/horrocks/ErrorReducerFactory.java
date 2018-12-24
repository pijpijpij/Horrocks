package com.pij.horrocks;

public interface ErrorReducerFactory<S> {

    Reducer<S> create(Throwable error);
}
