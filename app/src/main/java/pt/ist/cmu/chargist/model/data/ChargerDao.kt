package pt.ist.cmu.chargist.model.data

import android.icu.text.MessagePattern.ArgType.SELECT
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

    @Query("SELECT * FROM charger WHERE id = :chargerId LIMIT 1")
    suspend fun getChargerById(chargerId: String): Charger

    @Insert(onConflict = REPLACE)
    suspend fun insertCharger(charger: Charger)

    @Delete
    suspend fun deleteCharger(charger: Charger)

    @Update
    suspend fun updateCharger(charger: Charger)

    @Query("UPDATE charger " +
            "SET name = :name," +
            "chargingSlots = :slots," +
            "creditCard = :creditCard," +
            "mbWay = :mbWay," +
            "cash = :cash," +
            "latitude = :lat," +
            "longitude = :lng," +
            "priceFast = :priceFast," +
            "priceMedium = :priceMedium," +
            "priceSlow = :priceSlow " +
            "WHERE id = :chargerId")
    suspend fun updateCharger(chargerId: String, name:String, slots:List<String>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                              lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow: Double)

    @Query("DELETE FROM charger")
    suspend fun deleteRelevantChargers() // todo this function should only delete chargers in a certain radius

}