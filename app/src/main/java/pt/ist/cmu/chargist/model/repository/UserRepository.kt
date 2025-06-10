package pt.ist.cmu.chargist.model.repository

import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.User
import pt.ist.cmu.chargist.model.data.UserDao

class UserRepository(private val userDao: UserDao) {
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    suspend fun insert(user: User) {
        userDao.insertUser(user)
    }

    suspend fun update(user: User) {
        userDao.updateUser(user)
    }

    suspend fun delete(user: User) {
        userDao.deleteUser(user)
    }
}