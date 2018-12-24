package com.pij.horrocks;

interface ErrorReducerFactory<S> {

    Reducer<S> create(Throwable error);
}
