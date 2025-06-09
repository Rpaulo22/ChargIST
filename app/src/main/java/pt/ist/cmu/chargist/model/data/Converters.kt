package pt.ist.cmu.chargist.model.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split(",")
    }

    @TypeConverter
    fun fromListMap(listMap: Map<String, Double>?): String {
        return Gson().toJson(listMap)
    }

    @TypeConverter
    fun toListMap(data: String): Map<String, Double>? {
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return Gson().fromJson(data, type)
    }
}
