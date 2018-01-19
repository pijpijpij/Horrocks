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

package com.example.android.architecture.blueprints.todoapp.statistics.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.Presenter;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModule;
import com.example.android.architecture.blueprints.todoapp.statistics.ViewModel;
import com.example.android.architecture.blueprints.todoapp.statistics.ui.StatisticsFragment;
import com.pij.horrocks.Configuration;
import com.pij.horrocks.Engine;
import com.pij.horrocks.Feature;
import com.pij.horrocks.Logger;
import com.pij.horrocks.MemoryStore;
import com.pij.horrocks.MultipleResultFeature;
import com.pij.horrocks.View;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

import static java.util.Collections.singletonList;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 * <p/>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the FeaturedPresenter (if it fails, it emits a compiler error). It uses
 * {@link StatisticsModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 * <p>
 * Horrocks note: It's _probably_ overkill with a single feature, but hey, that's to demo where it's useful and where it's not.
 **/
public final class FeaturedPresenter implements Presenter {

    private final Logger logger;
    private final CompositeDisposable subscription = new CompositeDisposable();
    private final Engine<ViewModel, ViewModel> engine;
    private final Feature<Object, ViewModel> loadStatistics;
    private final Configuration<ViewModel, ViewModel> engineConfiguration;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    public FeaturedPresenter(TasksRepository tasksRepository, Logger logger, Engine<ViewModel, ViewModel> engine) {
        this.logger = logger;

        loadStatistics = new MultipleResultFeature<>(new LoadStatisticsFeature(logger, tasksRepository));
        this.engine = engine;
        engineConfiguration = Configuration.<ViewModel, ViewModel>builder()
                .store(new MemoryStore<>(initialState()))
                .transientResetter(this::resetTransientState)
                .stateToModel(state -> state)
                .features(singletonList(loadStatistics))
                .build();
    }

    @NonNull
    private ViewModel resetTransientState(@NonNull ViewModel state) {
        return state.toBuilder()
                .showLoadingStatisticsError(false)
                .build();
    }

    @NonNull
    private ViewModel initialState() {
        return ViewModel.builder()
                .showLoadingStatisticsError(false)
                .progressIndicator(false)
                .showStatistics(null)
                .build();
    }


    @Override
    public void takeView(View<ViewModel> view) {
        subscription.add(
                engine.runWith(engineConfiguration).subscribe(
                        view::display,
                        e -> logger.print(getClass(), "Terminal Damage!!!", e),
                        () -> logger.print(getClass(), "takeView completed!!!"))
        );

        loadStatistics.trigger(new Object());
    }

    @Override
    public void dropView() {
        subscription.clear();
    }
}
