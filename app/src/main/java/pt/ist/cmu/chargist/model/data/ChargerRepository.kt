package pt.ist.cmu.chargist.model.data

import kotlinx.coroutines.flow.Flow


class ChargerRepository(private val chargerDao: ChargerDao) {
    val allChargers: Flow<List<Charger>> = chargerDao.getAllChargers()

    suspend fun insert(charger: Charger) {
        chargerDao.insertCharger(charger)
    }

    suspend fun delete(charger: Charger) {
        chargerDao.deleteCharger(charger)
    }
}