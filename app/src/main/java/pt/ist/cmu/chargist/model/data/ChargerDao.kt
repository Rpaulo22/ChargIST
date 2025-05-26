package pt.ist.cmu.chargist.model.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargerDao {
    @Query("SELECT * FROM charger ORDER BY id DESC")
    fun getAllChargers(): Flow<List<Charger>>

    @Insert
    suspend fun insertCharger(charger: Charger)

    @Delete
    suspend fun deleteCharger(charger: Charger)
}