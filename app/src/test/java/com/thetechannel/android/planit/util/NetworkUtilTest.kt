package com.thetechannel.android.planit.util

import com.thetechannel.android.planit.data.source.network.NetworkCategory
import com.thetechannel.android.planit.data.source.network.NetworkTask
import com.thetechannel.android.planit.data.source.network.NetworkTaskMethod
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class NetworkUtilTest {
    @Test
    fun convertNetworkCategoryToDatabase_convertBack_returnsTrueIfBothAreTransferable() {
        val netCat = NetworkCategory(3, "Sport")

        val dbCat = netCat.asDatabaseEntity()

        assertThat(netCat.id, `is`(dbCat.id))
        assertThat(netCat.name, `is`(dbCat.name))
    }

    @Test
    fun convertNetworkTaskMethodToDatabase_convertBack_returnsTrueIfBothAreTransferable() {
        val netMethod = NetworkTaskMethod()

        val dbMethod = netMethod.asDatabaseEntity()

        assertThat(netMethod.id, `is`(dbMethod.id))
        assertThat(netMethod.name, `is`(dbMethod.name))
        assertThat(netMethod.breakLapse, `is`(dbMethod.breakLapse))
        assertThat(netMethod.workLapse, `is`(dbMethod.workLapse))
        assertThat(netMethod.iconUrl, `is`(dbMethod.iconUrl))
    }

    @Test
    fun convertNetworkTaskToDatabase_convertBack_returnsTrueIfBothAreTransferable() {
        val netTask = NetworkTask()

        val dbTask = netTask.asDatabaseEntity()

        assertThat(netTask.id, `is`(dbTask.id))
        assertThat(netTask.day, `is`(dbTask.day))
        assertThat(netTask.startAt, `is`(dbTask.startAt))
        assertThat(netTask.methodId, `is`(dbTask.methodId))
        assertThat(netTask.title, `is`(dbTask.title))
        assertThat(netTask.catId, `is`(dbTask.catId))
        assertThat(netTask.completed, `is`(dbTask.completed.toInt()))
    }
}