package pt.ist.cmu.chargist.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: String = "",
    val email: String,
    val name: String,
    val phoneNumber: String,
    val favoriteChargers: MutableList<String>,
)