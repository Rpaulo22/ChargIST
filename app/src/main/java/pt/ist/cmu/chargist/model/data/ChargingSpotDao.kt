package pt.ist.cmu.chargist.model.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSpotDao {
    @Query("SELECT * FROM charging_spot ORDER BY id DESC")
    fun getAllChargingSpots(): Flow<List<ChargingSpot>>

    @Insert(onConflict = REPLACE)
    suspend fun insertChargingSpot(chargingSpot: ChargingSpot)

    @Update
    suspend fun updateChargingSpot(chargingSpot: ChargingSpot)

    @Delete
    suspend fun deleteChargingSpot(chargingSpot: ChargingSpot)
}