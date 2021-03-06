package com.thetechannel.android.planit.data.source

import androidx.lifecycle.LiveData
import com.thetechannel.android.planit.data.Result
import com.thetechannel.android.planit.data.source.domain.Category
import com.thetechannel.android.planit.data.source.domain.Task
import com.thetechannel.android.planit.data.source.domain.TaskDetail
import com.thetechannel.android.planit.data.source.domain.TaskMethod
import java.util.*

interface AppDataSource {
    fun observeCategories(): LiveData<Result<List<Category>>>

    fun observeCategory(id: Int): LiveData<Result<Category>>

    fun observeTaskMethods(): LiveData<Result<List<TaskMethod>>>

    fun observeTaskMethod(id: Int): LiveData<Result<TaskMethod>>

    fun observeTasks(): LiveData<Result<List<Task>>>

    fun observeTasks(day: Date): LiveData<Result<List<Task>>>

    fun observeTask(id: String): LiveData<Result<Task>>

    fun observeTaskDetails(): LiveData<Result<List<TaskDetail>>>

    fun observeTaskDetail(id: String): LiveData<Result<TaskDetail>>

    suspend fun getCategories(): Result<List<Category>>

    suspend fun getCategory(id: Int): Result<Category>

    suspend fun getTaskMethods(): Result<List<TaskMethod>>

    suspend fun getTaskMethod(id: Int): Result<TaskMethod>

    suspend fun getTasks(): Result<List<Task>>

    suspend fun getTasks(day: Date): Result<List<Task>>

    suspend fun getTask(id: String): Result<Task>

    suspend fun getTaskDetails(): Result<List<TaskDetail>>

    suspend fun getTaskDetail(id: String): Result<TaskDetail>

    suspend fun saveCategory(category: Category)

    suspend fun saveTaskMethod(taskMethod: TaskMethod)

    suspend fun saveTask(task: Task)

    suspend fun completeTask(task: Task)

    suspend fun completeTask(id: String)

    suspend fun deleteCategory(category: Category)

    suspend fun deleteAllCategories()

    suspend fun deleteTaskMethod(taskMethod: TaskMethod)

    suspend fun deleteAllTaskMethods()

    suspend fun deleteTask(task: Task)

    suspend fun deleteAllTasks()
}