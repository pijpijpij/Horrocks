package com.example.android.architecture.blueprints.todoapp.addedittask;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;

/**
 * <p>Created on 14/01/2018.</p>
 *
 * @author PierreJean
 */
public interface Presenter extends BasePresenter<AddEditTaskContract.View> {

    void saveTask(String title, String description);

    void populateTask();

    boolean isDataMissing();
}
