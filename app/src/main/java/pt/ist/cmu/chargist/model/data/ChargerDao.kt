package pt.ist.cmu.chargist.model.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargerDao {
    @Query("SELECT * FROM charger ORDER BY id DESC")
    fun getAllChargers(): Flow<List<Charger>>

    @Insert(onConflict = REPLACE)
    suspend fun insertCharger(charger: Charger)

    @Update
    suspend fun updateCharger(charger: Charger)

    @Delete
    suspend fun deleteCharger(charger: Charger)

    @Query("DELETE FROM charger")
    suspend fun deleteRelevantChargers() // todo this function should only delete chargers in a certain radius
}