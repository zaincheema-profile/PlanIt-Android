package com.thetechannel.android.planit.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.github.mikephil.charting.data.PieEntry
import com.thetechannel.android.planit.data.Result
import com.thetechannel.android.planit.data.source.domain.Category
import com.thetechannel.android.planit.data.source.domain.Task
import com.thetechannel.android.planit.data.source.domain.TaskDetail
import com.thetechannel.android.planit.data.source.domain.TaskMethod
import com.thetechannel.android.planit.util.isToday
import kotlinx.coroutines.runBlocking
import java.lang.Error
import java.sql.Time
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FakeTestRepository : AppRepository {

    private var shouldReturnError: Boolean = false

    private val observableCategories: MutableLiveData<Result<List<Category>>> = MutableLiveData()
    private val observableTaskMethods: MutableLiveData<Result<List<TaskMethod>>> = MutableLiveData()
    private val observableTasks: MutableLiveData<Result<List<Task>>> = MutableLiveData()

    var categoriesServiceData: HashMap<Int, Category> = HashMap()
    var taskMethodsServiceData: HashMap<Int, TaskMethod> = HashMap()
    var tasksServiceData: HashMap<String, Task> = HashMap()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun observeCategories(): LiveData<Result<List<Category>>> {
        runBlocking { refreshCategories() }
        return observableCategories
    }

    override fun observeCategory(id: Int): LiveData<Result<Category>> {
        runBlocking { refreshCategories() }
        return observableCategories.map { categories ->
            when (categories) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(categories.exception)
                is Result.Success -> {
                    val category = categories.data.firstOrNull { it.id == id }
                        ?: return@map Result.Error(Exception("Not found"))
                    Result.Success(category)
                }
            }
        }
    }

    override fun observeTaskMethods(): LiveData<Result<List<TaskMethod>>> {
        runBlocking { refreshTaskMethods() }
        return observableTaskMethods
    }

    override fun observeTaskMethod(id: Int): LiveData<Result<TaskMethod>> {
        runBlocking { refreshTaskMethods() }
        return observableTaskMethods.map { methods ->
            when (methods) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(methods.exception)
                is Result.Success -> {
                    val method = methods.data.firstOrNull { it.id == id }
                        ?: return@map Result.Error(Exception("Not found"))
                    Result.Success(method)
                }
            }
        }
    }

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        runBlocking { refreshTasks() }
        return observableTasks
    }

    override fun observeTasks(day: Date): LiveData<Result<List<Task>>> {
        return observableTasks.map { tasks ->
            when (tasks) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(tasks.exception)
                is Result.Success -> {
                    val requiredTasks = tasks.data.filter { it.day.time == day.time }
                    if (requiredTasks.isEmpty()) Result.Error(Exception("Not found"))
                    else Result.Success(requiredTasks)
                }
            }
        }
    }

    override fun observeTask(id: String): LiveData<Result<Task>> {
        runBlocking { refreshTasks() }
        return observableTasks.map { tasks ->
            when (tasks) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(tasks.exception)
                is Result.Success -> {
                    val task = tasks.data.firstOrNull { it.id == id }
                        ?: return@map Result.Error(Exception("Not found"))
                    Result.Success(task)
                }
            }
        }
    }

    override fun observeTaskDetails(): LiveData<Result<List<TaskDetail>>> {

        runBlocking {
            refreshCategories()
            refreshTaskMethods()
            refreshTasks()
        }
        return observableTasks.map { tasks ->
            getTaskDetails(tasks)
        }
    }

    private fun getTaskDetails(tasks: Result<List<Task>>?) =
        when (tasks) {
            is Result.Loading -> Result.Loading
            is Result.Error -> Result.Error(tasks.exception)
            is Result.Success -> {
                val details = mutableListOf<TaskDetail>()
                tasks.data.sortedWith(Comparator { o1, o2 ->
                    if (o1.day.equals(o2.day)) o1.startAt.compareTo(o2.startAt)
                    else o1.day.compareTo(o2.day)
                }).forEach { task ->
                    var error = false;
                    runBlocking {
                        val category = getCategory(task.catId, true)
                        val method = getTaskMethod(task.methodId, true)

                        if (category is Result.Success && method is Result.Success) {
                            details.add(
                                com.thetechannel.android.planit.getTaskDetail(
                                    category.data,
                                    method.data,
                                    task
                                )
                            )
                        } else {
                            error = true
                        }
                    }
                    if (error) return Result.Error(Exception("invalid details"))
                }
                Result.Success(details)
            }
            else -> Result.Error(Exception("empty tasks"))
        }

    override fun observeTaskDetail(id: String): LiveData<Result<TaskDetail>> {
        TODO("Not implemented yet")
    }

    override suspend fun getCategories(forceUpdate: Boolean): Result<List<Category>> {
        return Result.Success(categoriesServiceData.values.toList())
    }

    override suspend fun getCategory(id: Int, forceUpdate: Boolean): Result<Category> {
        categoriesServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Could not find category"))
    }

    override suspend fun getTaskMethods(forceUpdate: Boolean): Result<List<TaskMethod>> {
        return Result.Success(taskMethodsServiceData.values.toList())
    }

    override suspend fun getTaskMethod(id: Int, forceUpdate: Boolean): Result<TaskMethod> {
        taskMethodsServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Could not find task method"))
    }

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        if (shouldReturnError) return Result.Error(Exception("error loading data"))
        return Result.Success(ArrayList(tasksServiceData.values))
    }

    override suspend fun getTasks(day: Date, forceUpdate: Boolean): Result<List<Task>> {
        val tasks = tasksServiceData.values.filter {
            it.day == day
        }
        if (tasks.isEmpty()) return Result.Error(Exception("Could not find tasks"))
        return Result.Success(tasks)
    }

    override suspend fun getTask(id: String, forceUpdate: Boolean): Result<Task> {
        tasksServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Could not find task"))
    }

    override suspend fun getTaskDetails(forceUpdate: Boolean): Result<List<TaskDetail>> {
        TODO("Not implemented yet")
    }

    override suspend fun getTaskDetail(id: String, forceUpdate: Boolean): Result<TaskDetail> {
        val task = tasksServiceData[id]
        val category = categoriesServiceData.get(task?.catId)
        val method = taskMethodsServiceData.get(task?.methodId)

        if (task == null || category == null || method == null) {
            return Result.Error(Exception("Could not find task detail"))
        }

        val detail = com.thetechannel.android.planit.getTaskDetail(category, method, task)
        return Result.Success(detail)
    }

    private fun getTodayPieEntries(tasks: List<Task>): List<PieEntry> {
        val map = mutableMapOf<Int, Int>()

        for (t in tasks.filter { it.day.isToday() }) {
            map[t.catId] = map.getOrDefault(t.catId, 0) + 1
        }

        val entries = mutableListOf<PieEntry>()
        for (node in map) {
            entries.add(PieEntry(node.value.toFloat(), categoriesServiceData[node.key]?.name))
        }

        return entries
    }

    override suspend fun saveCategory(category: Category) {
        categoriesServiceData[category.id] = category
        refreshCategories()
    }

    override suspend fun saveCategories(vararg categories: Category) {
        for (c in categories) categoriesServiceData[c.id] = c
        refreshCategories()
    }

    override suspend fun saveTaskMethod(taskMethod: TaskMethod) {
        taskMethodsServiceData[taskMethod.id] = taskMethod
        refreshTaskMethods()
    }

    override suspend fun saveTaskMethods(vararg taskMethods: TaskMethod) {
        for (m in taskMethods) taskMethodsServiceData[m.id] = m
        refreshTaskMethods()
    }

    override suspend fun saveTask(task: Task) {
        tasksServiceData[task.id] = task
        refreshTasks()
    }

    override suspend fun saveTasks(vararg tasks: Task) {
        for (t in tasks) tasksServiceData[t.id] = t
    }

    override suspend fun refreshCategories() {
        observableCategories.value = getCategories(false)
    }

    override suspend fun refreshTaskMethods() {
        observableTaskMethods.value = getTaskMethods(false)
    }

    override suspend fun refreshTasks() {
        observableTasks.value = getTasks(false)
    }

    override suspend fun completeTask(task: Task) {
        tasksServiceData[task.id]?.completed = true
        refreshTasks()
    }

    override suspend fun completeTask(id: String) {
        tasksServiceData[id]?.completed = true
        refreshTasks()
    }

    override suspend fun deleteCategory(category: Category) {
        categoriesServiceData.remove(category.id)
        refreshCategories()
    }

    override suspend fun deleteTaskMethod(taskMethod: TaskMethod) {
        taskMethodsServiceData.remove(taskMethod.id)
        refreshTaskMethods()
    }

    override suspend fun deleteTask(task: Task) {
        tasksServiceData.remove(task.id)
        refreshTasks()
    }
}