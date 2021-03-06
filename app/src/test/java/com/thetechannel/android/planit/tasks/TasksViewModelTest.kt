package com.thetechannel.android.planit.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.thetechannel.android.planit.MainCoroutineRule
import com.thetechannel.android.planit.TaskFilterType
import com.thetechannel.android.planit.data.source.FakeTestRepository
import com.thetechannel.android.planit.data.source.domain.Category
import com.thetechannel.android.planit.data.source.domain.Task
import com.thetechannel.android.planit.data.source.domain.TaskDetail
import com.thetechannel.android.planit.data.source.domain.TaskMethod
import com.thetechannel.android.planit.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URI
import java.sql.Time
import java.util.*

@ExperimentalCoroutinesApi
class TasksViewModelTest {

    private lateinit var viewModel: TasksViewModel
    private lateinit var category: Category
    private lateinit var method: TaskMethod
    private lateinit var task3: Task
    private lateinit var task2: Task
    private lateinit var task1: Task
    private lateinit var tasks: List<Task>
    private lateinit var repository: FakeTestRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() = runBlocking {
        repository = FakeTestRepository()
        category = Category(1, "Study")
        method = TaskMethod(1, "Pomodoro", Time(25 * 60000), Time(5 * 60000), URI("http://no-url"))
        task1 = Task(
            "task_1",
            Date(25L),
            Time(100),
            method.id,
            "Clean my room",
            category.id,
            true
        )
        task2 = Task(
            "task_2",
            Calendar.getInstance().time,
            Time(1500),
            method.id,
            "Maths Assignment",
            category.id,
            true
        )
        task3 = Task(
            "task_3",
            Calendar.getInstance().time,
            Time(5000),
            method.id,
            "Read Emails",
            category.id,
            false
        )

        repository.saveCategory(category)
        repository.saveTaskMethod(method)
        tasks = listOf(task1, task2, task3)
        tasks.forEach { repository.saveTask(it) }

        viewModel = TasksViewModel(repository)
    }

    @Test
    fun setFilteringAsAll_setsUpTaskDetailsFromAllInsertedTasks() {
        viewModel.setFiltering(TaskFilterType.ALL)
        val details: List<TaskDetail> = viewModel.observableTaskDetails.getOrAwaitValue()

        assertThat(details.size, `is`(3))
        details[0].assertBelongsTo(category, method, task1)
        details[1].assertBelongsTo(category, method, task2)
        details[2].assertBelongsTo(category, method, task3)
    }

    @Test
    fun setFilteringAsPending_setsUpTaskDetailsFromPendingTasks() {
        viewModel.setFiltering(TaskFilterType.PENDING)

        val loaded = viewModel.observableTaskDetails.getOrAwaitValue()

        assertThat(loaded.size, `is`(1))
        loaded[0].assertBelongsTo(category, method, task3)
    }

    @Test
    fun setFilteringAsCompleted_setsUpTaskDetailsFromCompletedTasks() {
        viewModel.setFiltering(TaskFilterType.COMPLETED)

        val loaded = viewModel.observableTaskDetails.getOrAwaitValue()

        assertThat(loaded.size, `is`(2))
        loaded[0].assertBelongsTo(category, method, task1)
        loaded[1].assertBelongsTo(category, method, task2)
    }

    @Test
    fun setFilteringAsCompletedToday_setsUpTaskDetailsFromCompletedTodayTasks() {
        viewModel.setFiltering(TaskFilterType.COMPLETED_TODAY)

        val loaded = viewModel.observableTaskDetails.getOrAwaitValue()

        assertThat(loaded.size, `is`(1))
        loaded[0].assertBelongsTo(category, method, task2)
    }

    private fun TaskDetail.assertBelongsTo(category: Category, method: TaskMethod, task: Task) {
        assertThat(id, `is`(task.id))
        assertThat(categoryName, `is`(category.name))
        assertThat(methodName, `is`(method.name))
        assertThat(methodIconUrl, `is`(method.iconUrl))
        assertThat(
            timeLapse,
            `is`(Time(method.workLapse.time + method.breakLapse.time))
        )
        assertThat(title, `is`(task.title))
        assertThat(workStart, `is`(task.startAt))
        assertThat(
            workEnd,
            `is`(Time(task.startAt.time + method.workLapse.time))
        )
        assertThat(
            breakStart,
            `is`(Time(task.startAt.time + method.workLapse.time))
        )
        assertThat(
            breakEnd,
            `is`(Time(task.startAt.time + method.workLapse.time + method.breakLapse.time))
        )
        assertThat(completed, `is`(task.completed))
    }

    @Test
    fun loadTasksWhenUnavailable_callErrorToDisplay() {
        repository.setReturnError(true)
        viewModel.refresh()

        assertThat(viewModel.error.getOrAwaitValue(), `is`(true))
        assertThat(viewModel.empty.getOrAwaitValue(), `is`(true))
    }
}