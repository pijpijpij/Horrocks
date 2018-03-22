/*
 * Copyright 2018, Chiswick Forest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.di;

import android.app.Application;
import android.content.Context;

import com.pij.android.utils.AndroidDebugLogger;
import com.pij.utils.Logger;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

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
    @Reusable
    static Logger provideAndroidDebugLogger() {
        return new AndroidDebugLogger();
    }

    //expose Application as an injectable context
    @Binds
    abstract Context bindContext(Application application);
}

