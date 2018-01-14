package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.pij.horrocks.View;

/**
 * <p>Created on 14/01/2018.</p>
 *
 * @author PierreJean
 */
public interface Presenter extends BasePresenter<View<ViewModel>> {

    void editTask();

    void deleteTask();

    void completeTask();

    void activateTask();

}
