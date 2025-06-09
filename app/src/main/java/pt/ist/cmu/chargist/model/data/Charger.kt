package pt.ist.cmu.chargist.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charger")
data class Charger(
    @PrimaryKey val id: String = "",
    val name: String,
    val ownerId: String,
    var chargingSlots: List<String> = mutableListOf<String>(),
    var creditCard: Boolean = false,
    var cash: Boolean = false,
    var mbWay: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    var priceFast: Double,
    var priceMedium: Double,
    var priceSlow: Double,
    var ratings: Map<String, Double> = mutableMapOf<String, Double>(),
    var ratingsMean: Double,
)