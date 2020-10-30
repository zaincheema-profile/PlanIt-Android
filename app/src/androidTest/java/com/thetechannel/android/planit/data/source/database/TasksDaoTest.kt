package com.thetechannel.android.planit.data.source.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TasksDaoTest {
    private val STUDY = 1
    private val BUSINESS = 2
    private val SPORT = 3

    private lateinit var taskMethod: DbTaskMethod
    private lateinit var studyCategory: DbCategory
    private lateinit var businessCategory: DbCategory
    private lateinit var sportCategory: DbCategory

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PlanItDatabase

    @Before
    fun initDb() = runBlockingTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PlanItDatabase::class.java
        )
            .build()

        taskMethod = DbTaskMethod(1, "pomodoro", 0L, 0L, "https://www.google.com")
        database.taskMethodsDao.insert(taskMethod)

        studyCategory = DbCategory(STUDY, "Study")
        businessCategory = DbCategory(BUSINESS, "Business")
        sportCategory = DbCategory(SPORT, "Sport")
        database.categoriesDao.insertAll(studyCategory, businessCategory, sportCategory)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTask_getById() = runBlockingTest {
        val task = DbTask(
            "TASK1",
            System.currentTimeMillis(),
            0,
            1,
            "Maths Assignment",
            STUDY,
            false
        )
        database.tasksDao.insert(task)

        val loadedTask = database.tasksDao.getById(task.id)

        assertThat<DbTask>(loadedTask as DbTask, CoreMatchers.notNullValue())
        assertThat(loadedTask.id, `is`(task.id))
        assertThat(loadedTask.day, `is`(task.day))
        assertThat(loadedTask.startAt, `is`(task.startAt))
        assertThat(loadedTask.methodId, `is`(task.methodId))
        assertThat(loadedTask.catId, `is`(task.catId))
        assertThat(loadedTask.title, `is`(task.title))
        assertThat(loadedTask.completed, `is`(task.completed))
    }

    @Test
    fun insertAndDeleteTask_getTaskById_returnsNull() = runBlockingTest {
        val task = DbTask(
            "TASK1",
            System.currentTimeMillis(),
            0,
            1,
            "Maths Assignment",
            STUDY,
            false
        )
        database.tasksDao.insert(task)
        database.tasksDao.delete(task)

        val loadedTask = database.tasksDao.getById(task.id)
        assertThat(loadedTask, CoreMatchers.nullValue())
    }

    @Test
    fun insertTaskAndTaskType_returnTaskDetails() = runBlockingTest {
        val task =
            DbTask("TASK1", System.currentTimeMillis(), 0, 1, "Maths Assignment", 1, false)
        database.tasksDao.insert(task)

        val taskDetail = database.tasksDao.getTaskDetailsByTaskId(task.id)

        assertThat<DbTaskDetail>(taskDetail, CoreMatchers.notNullValue())
        assertThat(taskDetail.id, `is`(task.id))
        assertThat(taskDetail.method, `is`(taskMethod.name))
        assertThat(taskDetail.methodIconUrl, `is`(taskMethod.iconUrl))
        assertThat(
            taskDetail.timeLapse,
            `is`(taskMethod.workLapse + taskMethod.breakLapse)
        )
        assertThat(taskDetail.title, `is`(task.title))
        assertThat(taskDetail.workStart, `is`(task.startAt))
        assertThat(
            taskDetail.workEnd,
            `is`(task.startAt + taskMethod.workLapse)
        )
        assertThat(
            taskDetail.breakStart,
            `is`(task.startAt + taskMethod.workLapse)
        )
        assertThat(
            taskDetail.breakEnd, `is`(
                task.startAt + taskMethod.workLapse + taskMethod.breakLapse
            )
        )
    }

    @Test
    fun insertedIncompleted_updateTaskCompletedAndFetchById_returnsCompletedTask() =
        runBlockingTest {
            val task = DbTask(
                "TASK1",
                System.currentTimeMillis(),
                0,
                1,
                "Maths Assignment",
                STUDY,
                false
            )
            database.tasksDao.insert(task)

            database.tasksDao.updateCompleted(task.id, true)
            val loaded = database.tasksDao.getById(task.id)
            assertThat<DbTask>(loaded as DbTask, `is`(CoreMatchers.notNullValue()))

            assertThat(loaded.completed, `is`(true))
        }

    @Test
    fun insertTasks_getTasksOverview_returnsOverviewOfInsertedTasks() = runBlockingTest {
        val tasks = getVersatileTasks()
        database.tasksDao.insertAll(*tasks)

        val views = database.tasksDao.getTasksOverView()
        assertThat(views.completedTasks, `is`(2))
        assertThat(views.pendingTasks, `is`(4))
        assertThat(views.tasksCompletedToday, `is`(1))
    }

    private fun getVersatileTasks() = arrayOf(
        DbTask(
            "task_1",
            System.currentTimeMillis(),
            1L,
            taskMethod.id,
            "Maths Assignment",
            studyCategory.id,
            false
        ),
        DbTask(
            "task_2",
            45000L,
            1L,
            taskMethod.id,
            "Prepare for Algo Quiz",
            studyCategory.id,
            true
        ),
        DbTask(
            "task_3",
            System.currentTimeMillis(),
            1L,
            taskMethod.id,
            "Prepare Slides",
            studyCategory.id,
            false
        ),
        DbTask(
            "task_4",
            System.currentTimeMillis(),
            1L,
            taskMethod.id,
            "View unread emails",
            businessCategory.id,
            true
        ),
        DbTask(
            "task_5",
            System.currentTimeMillis(),
            1L,
            taskMethod.id,
            "Plan next week layout",
            businessCategory.id,
            false
        ),
        DbTask(
            "task_6",
            System.currentTimeMillis(),
            1L,
            taskMethod.id,
            "Two brisks around the colony",
            sportCategory.id,
            false
        )
    )
}