package pt.ist.cmu.chargist.model.repository

import kotlinx.coroutines.flow.Flow
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargerDao

class ChargerRepository(private val chargerDao: ChargerDao) {
    val allChargers: Flow<List<Charger>> = chargerDao.getAllChargers()

    suspend fun insert(charger: Charger) {
        chargerDao.insertCharger(charger)
    }

    suspend fun update(charger: Charger) {
        chargerDao.updateCharger(charger)
    }

    suspend fun delete(charger: Charger) {
        chargerDao.deleteCharger(charger)
    }
}