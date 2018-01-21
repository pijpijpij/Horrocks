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

package com.example.android.architecture.blueprints.todoapp.statistics.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.statistics.Presenter;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModel;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

/**
 * Main UI for the statistics screen.
 */
@ActivityScoped
public class StatisticsFragment extends DaggerFragment {

    @Inject
    Presenter mPresenter;
    private TextView mStatisticsTV;

    @Inject
    public StatisticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics_frag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mStatisticsTV = view.findViewById(R.id.statistics);
        mPresenter.takeView(this::display);
    }

    @Override
    public void onDestroyView() {
        mPresenter.dropView();
        super.onDestroyView();
    }

    private void setProgressIndicator(boolean active) {
        if (active) {
            mStatisticsTV.setText(getString(R.string.loading));
        } else {
            mStatisticsTV.setText("");
        }
    }

    private void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
        if (numberOfCompletedTasks == 0 && numberOfIncompleteTasks == 0) {
            mStatisticsTV.setText(getResources().getString(R.string.statistics_no_tasks));
        } else {
            String displayString = getResources().getString(R.string.statistics_active_tasks) + " "
                    + numberOfIncompleteTasks + "\n" + getResources().getString(
                    R.string.statistics_completed_tasks) + " " + numberOfCompletedTasks;
            mStatisticsTV.setText(displayString);
        }
    }

    private void showLoadingStatisticsError() {
        mStatisticsTV.setText(getResources().getString(R.string.statistics_error));
    }

    private void display(@NonNull StatisticsModel model) {
        setProgressIndicator(model.progressIndicator());
        if (model.showLoadingStatisticsError()) showLoadingStatisticsError();
        StatisticsModel.Numbers numbers = model.showStatistics();
        if (numbers != null) showStatistics(numbers.incompleteTasks(), numbers.completedTasks());
    }
}
