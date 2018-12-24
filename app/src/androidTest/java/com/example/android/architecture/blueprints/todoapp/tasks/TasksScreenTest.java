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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.ToDoApplication;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.ui.TasksActivity;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.example.android.architecture.blueprints.todoapp.TestUtils.getCurrentActivity;
import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksScreenTest {

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<TasksActivity> mTasksActivityTestRule =
            new ActivityTestRule<TasksActivity>(TasksActivity.class) {

                /**
                 * To avoid a long list of tasks and the need to scroll through the list to find a
                 * task, we call {@link TasksDataSource#deleteAllTasks()} before each test.
                 */
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    // Doing this in @Before generates a race condition.
                    ((ToDoApplication) InstrumentationRegistry.getTargetContext()
                            .getApplicationContext()).getTasksRepository().deleteAllTasks();
                }
            };

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * `
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickAddTaskButton_opensAddTaskUi() {
        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click());

        // Check if the add task screen is displayed
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()));
    }

    @Test
    public void editTask() {
        // First add a task
        createTask("A TITLE", "THE DESCRIPTION");

        // Click on the task on the list
        onView(withText("A TITLE")).perform(click());

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click());

        String editTaskTitle = "ANOTHER TITLE";
        String editTaskDescription = "New Description";

        // Edit task title and description
        onView(withId(R.id.add_task_title))
                .perform(replaceText(editTaskTitle), closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(replaceText(editTaskDescription),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText(editTaskTitle)).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText("A TITLE")).check(doesNotExist());
    }

    @Test
    public void addTaskToTasksList() {
        createTask("A TITLE", "THE DESCRIPTION");

        // Verify task is displayed on screen
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
    }

    @Test
    public void markTaskAsComplete() {
        viewAllTasks();

        // Add active task
        createTask("A TITLE", "THE DESCRIPTION");

        // Mark the task as complete
        clickCheckBoxForTask("A TITLE");

        // Verify task is shown as complete
        viewAllTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
        viewActiveTasks();
        onView(withItemText("A TITLE")).check(matches(not(isDisplayed())));
        viewCompletedTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
    }

    @Test
    public void markTaskAsActive() {
        viewAllTasks();

        // Add completed task
        createTask("A TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("A TITLE");

        // Mark the task as active
        clickCheckBoxForTask("A TITLE");

        // Verify task is shown as active
        viewAllTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
        viewActiveTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
        viewCompletedTasks();
        onView(withItemText("A TITLE")).check(matches(not(isDisplayed())));
    }

    @Test
    public void showAllTasks() {
        // Add 2 active tasks
        createTask("A TITLE", "THE DESCRIPTION");
        createTask("ANOTHER TITLE", "THE DESCRIPTION");

        //Verify that all our tasks are shown
        viewAllTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
        onView(withItemText("ANOTHER TITLE")).check(matches(isDisplayed()));
    }

    @Test
    public void showActiveTasks() {
        // Add 2 active tasks
        createTask("A TITLE", "THE DESCRIPTION");
        createTask("ANOTHER TITLE", "THE DESCRIPTION");

        //Verify that all our tasks are shown
        viewActiveTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
        onView(withItemText("ANOTHER TITLE")).check(matches(isDisplayed()));
    }

    @Test
    public void showCompletedTasks() {
        // Add 2 completed tasks
        createTask("A TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("A TITLE");
        createTask("ANOTHER TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("ANOTHER TITLE");

        // Verify that all our tasks are shown
        viewCompletedTasks();
        onView(withItemText("A TITLE")).check(matches(isDisplayed()));
        onView(withItemText("ANOTHER TITLE")).check(matches(isDisplayed()));
    }

    @Test
    public void clearCompletedTasks() {
        viewAllTasks();

        // Add 2 complete tasks
        createTask("A TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("A TITLE");
        createTask("ANOTHER TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("ANOTHER TITLE");

        // Click clear completed in menu
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.menu_clear)).perform(click());

        //Verify that completed tasks are not shown
        onView(withItemText("A TITLE")).check(matches(not(isDisplayed())));
        onView(withItemText("ANOTHER TITLE")).check(matches(not(isDisplayed())));
    }

    @Test
    public void createOneTask_deleteTask() {
        viewAllTasks();

        // Add active task
        createTask("A TITLE", "THE DESCRIPTION");

        // Open it in details view
        onView(withText("A TITLE")).perform(click());

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify it was deleted
        viewAllTasks();
        onView(withText("A TITLE")).check(doesNotExist());
    }

    @Test
    public void createTwoTasks_deleteOneTask() {
        // Add 2 active tasks
        createTask("A TITLE", "THE DESCRIPTION");
        createTask("ANOTHER TITLE", "THE DESCRIPTION");

        // Open the second task in details view
        onView(withText("ANOTHER TITLE")).perform(click());

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify only one task was deleted
        viewAllTasks();
        onView(withText("A TITLE")).check(matches(isDisplayed()));
        onView(withText("ANOTHER TITLE")).check(doesNotExist());
    }

    @Test
    public void markTaskAsCompleteOnDetailScreen_taskIsCompleteInList() {
        viewAllTasks();

        // Add 1 active task
        createTask("A TITLE", "THE DESCRIPTION");

        // Click on the task on the list
        onView(withText("A TITLE")).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as completed
        onView(allOf(withId(R.id.complete),
                hasSibling(withText("A TITLE")))).check(matches(isChecked()));
    }

    @Test
    public void markTaskAsActiveOnDetailScreen_taskIsActiveInList() {
        viewAllTasks();

        // Add 1 completed task
        createTask("A TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("A TITLE");

        // Click on the task on the list
        onView(withText("A TITLE")).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText("A TITLE")))).check(matches(not(isChecked())));
    }

    @Test
    public void markTaskAsCompleteAndActiveOnDetailScreen_taskIsActiveInList() {
        viewAllTasks();

        // Add 1 active task
        createTask("A TITLE", "THE DESCRIPTION");

        // Click on the task on the list
        onView(withText("A TITLE")).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText("A TITLE")))).check(matches(not(isChecked())));
    }

    @Test
    public void markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() {
        viewAllTasks();

        // Add 1 completed task
        createTask("TITLE", "DESCRIPTION");
        clickCheckBoxForTask("TITLE");

        // Click on the task on the list
        onView(withText("TITLE")).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText("TITLE")))).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_FilterActivePersists() {

        // Add a completed task
        createTask("A TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("A TITLE");

        // when switching to active tasks
        viewActiveTasks();

        // then no tasks should appear
        onView(withText("A TITLE")).check(matches(not(isDisplayed())));

        // when rotating the screen
        TestUtils.rotateOrientation(mTasksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText("A TITLE")).check(doesNotExist());
    }

    @Test
    public void orientationChange_FilterCompletedPersists() {

        // Add a completed task
        createTask("A TITLE", "THE DESCRIPTION");
        clickCheckBoxForTask("A TITLE");

        // when switching to completed tasks
        viewCompletedTasks();

        // the completed task should be displayed
        onView(withText("A TITLE")).check(matches(isDisplayed()));

        // when rotating the screen
        TestUtils.rotateOrientation(mTasksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText("A TITLE")).check(matches(isDisplayed()));
        onView(withText(R.string.label_completed)).check(matches(isDisplayed()));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_ChangePersists() {
        // Add a completed task
        createTask("A TITLE", "THE DESCRIPTION");

        // Open the task in details view
        onView(withText("A TITLE")).perform(click());

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click());

        // Change task title (but don't save)
        onView(withId(R.id.add_task_title))
                .perform(replaceText("ANOTHER TITLE"), closeSoftKeyboard()); // Type new task title

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Verify task title is restored
        onView(withId(R.id.add_task_title)).check(matches(withText("ANOTHER TITLE")));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_NoDuplicate() throws IllegalStateException {
        // Add a completed task
        createTask("A TITLE", "THE DESCRIPTION");

        // Open the task in details view
        onView(withText("A TITLE")).perform(click());

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click());

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit task title and description
        onView(withId(R.id.add_task_title))
                .perform(replaceText("ANOTHER TITLE"), closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(replaceText("THE DESCRIPTION"),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText("ANOTHER TITLE")).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText("A TITLE")).check(doesNotExist());
    }

    private void viewAllTasks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_all)).perform(click());
    }

    private void viewActiveTasks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_active)).perform(click());
    }

    private void viewCompletedTasks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_completed)).perform(click());
    }

    private void createTask(String title, String description) {
        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click());

        // Add task title and description
        onView(withId(R.id.add_task_title)).perform(typeText(title),
                closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(typeText(description),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());
    }

    private void clickCheckBoxForTask(String title) {
        onView(allOf(withId(R.id.complete), hasSibling(withText(title)))).perform(click());
    }

    private String getText(int stringId) {
        return mTasksActivityTestRule.getActivity().getResources().getString(stringId);
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                mTasksActivityTestRule.getActivity(), R.id.toolbar);
    }
}
