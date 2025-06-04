package pt.ist.cmu.chargist.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_slot")
data class ChargingSlot(
    @PrimaryKey val id:String = "",
    var speed:String,
    var type:String
)