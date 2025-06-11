package pt.ist.cmu.chargist.model.repository

import kotlinx.coroutines.flow.Flow
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.data.ChargingSlotDao

class ChargingSlotRepository(private val chargingSlotDao: ChargingSlotDao) {
    val allChargingSlots: Flow<List<ChargingSlot>> = chargingSlotDao.getAllChargingSlots()

    suspend fun insert(chargingSlot: ChargingSlot) {
        chargingSlotDao.insertChargingSlot(chargingSlot)
    }

    suspend fun update(chargingSlot: ChargingSlot) {
        chargingSlotDao.updateChargingSlot(chargingSlot)
    }

    suspend fun delete(chargingSlot: ChargingSlot) {
        chargingSlotDao.deleteChargingSlot(chargingSlot)
    }

    suspend fun delete(id: String) {
        chargingSlotDao.deleteChargingSlot(id)
    }

    fun getSlots(ids: List<String>): Flow<List<ChargingSlot>> {
        return chargingSlotDao.getSlots(ids)
    }

    suspend fun delete(ids: List<String>) {
        chargingSlotDao.deleteChargingSlots(ids)
    }
}