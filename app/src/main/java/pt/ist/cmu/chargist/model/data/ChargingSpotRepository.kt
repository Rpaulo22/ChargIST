package pt.ist.cmu.chargist.model.data

import kotlinx.coroutines.flow.Flow


class ChargingSpotRepository(private val chargingSpotDao: ChargingSpotDao) {
    val allChargingSpots: Flow<List<ChargingSpot>> = chargingSpotDao.getAllChargingSpots()

    suspend fun insert(chargingSpot: ChargingSpot) {
        chargingSpotDao.insertChargingSpot(chargingSpot)
    }

    suspend fun update(chargingSpot: ChargingSpot) {
        chargingSpotDao.updateChargingSpot(chargingSpot)
    }

    suspend fun delete(chargingSpot: ChargingSpot) {
        chargingSpotDao.deleteChargingSpot(chargingSpot)
    }
}