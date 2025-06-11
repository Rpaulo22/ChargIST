package pt.ist.cmu.chargist.model.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSlotDao {
    @Query("SELECT * FROM charging_slot ORDER BY id DESC")
    fun getAllChargingSlots(): Flow<List<ChargingSlot>>

    @Insert(onConflict = REPLACE)
    suspend fun insertChargingSlot(chargingSlot: ChargingSlot)

    @Update
    suspend fun updateChargingSlot(chargingSlot: ChargingSlot)

    @Delete
    suspend fun deleteChargingSlot(chargingSlot: ChargingSlot)

    @Query("DELETE FROM charging_slot WHERE id = :id")
    suspend fun deleteChargingSlot(id: String)

    @Query("SELECT * FROM charging_slot WHERE id IN (:ids)")
    fun getSlots(ids: List<String>): Flow<List<ChargingSlot>>

    @Query("DELETE FROM charging_slot WHERE id IN (:ids)")
    suspend fun deleteChargingSlots(ids: List<String>)
}