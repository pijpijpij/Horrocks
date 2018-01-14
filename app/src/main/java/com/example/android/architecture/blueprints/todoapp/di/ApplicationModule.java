package com.example.android.architecture.blueprints.todoapp.di;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.pij.horrocks.Logger;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to bind our Application class as a Context in the AppComponent
 * By using Dagger Android we do not need to pass our Application instance to any module,
 * we simply need to expose our Application as Context.
 * One of the advantages of Dagger.Android is that your
 * Application & Activities are provided into your graph for you.
 * {@link
 * AppComponent}.
 */
@Module
abstract class ApplicationModule {
    @Provides
    static Logger provideLogger() {
        return new Logger() {
            @Override
            public <T> void print(@NonNull Class<T> javaClass, @NonNull String message) {
                Log.d(javaClass.getSimpleName(), message);
            }

            @Override
            public <T> void print(@NonNull Class<T> aClass, @NonNull String message, @NonNull Throwable e) {
                Log.d(aClass.getSimpleName(), message, e);

            }
        };
    }

    //expose Application as an injectable context
    @Binds
    abstract Context bindContext(Application application);
}

