package pt.ist.cmu.chargist.model.data

data class Charger(val id:String,
                   val name: String,
                   var chargingSpots: List<ChargingSpot>,
                   var creditCard: Boolean = false,
                   var money: Boolean = false,
                   var mbWay: Boolean = false,
                   val latitude: Double,
                   val longitude: Double,
                   var priceFast: Double,
                   var priceMedium: Double,
                   var priceSlow: Double)