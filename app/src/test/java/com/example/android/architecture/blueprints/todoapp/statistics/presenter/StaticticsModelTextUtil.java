package com.example.android.architecture.blueprints.todoapp.statistics.presenter;

import com.example.android.architecture.blueprints.todoapp.statistics.ViewModel;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public abstract class StaticticsModelTextUtil {

    public static ViewModel defaultState() {
        return ViewModel.builder()
                .progressIndicator(false)
                .showLoadingStatisticsError(false)
                .build();
    }

}