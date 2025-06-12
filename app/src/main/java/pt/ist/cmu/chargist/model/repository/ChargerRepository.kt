package pt.ist.cmu.chargist.model.repository

import kotlinx.coroutines.flow.Flow
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargerDao
import pt.ist.cmu.chargist.model.data.ChargingSlot

class ChargerRepository(private val chargerDao: ChargerDao) {
    val allChargers: Flow<List<Charger>> = chargerDao.getAllChargers()

    suspend fun getChargerById(chargerId: String): Charger {
        return chargerDao.getChargerById(chargerId)
    }

    suspend fun insert(charger: Charger) {
        chargerDao.insertCharger(charger)
    }

    suspend fun update(charger: Charger) {
        chargerDao.updateCharger(charger)
    }

    suspend fun update(chargerId: String, name:String, slots:List<String>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                       lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow: Double) {
        chargerDao.updateCharger(chargerId, name, slots, creditCard, mbWay, cash, lat, lng, priceFast, priceMedium, priceSlow)
    }

    suspend fun delete(charger: Charger) {
        chargerDao.deleteCharger(charger)
    }

    suspend fun deleteRelevantChargers() {
        chargerDao.deleteRelevantChargers()
    }

    fun getChargerFlowById(id: String): Flow<Charger> {
        return chargerDao.getChargerFlow(id)
    }


}