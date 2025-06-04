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
}