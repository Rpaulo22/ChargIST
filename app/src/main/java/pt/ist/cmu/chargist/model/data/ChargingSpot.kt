package pt.ist.cmu.chargist.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_spot")
data class ChargingSpot(
    @PrimaryKey val id:String = "",
    var speed:String,
    var type:String
)