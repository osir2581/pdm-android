package ro.ubbcluj.cs.ilazar.myapp2.todo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "items")
data class Item(
    @PrimaryKey @ColumnInfo(name = "_id") var _id: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "title") var title : String
) {
    override fun toString(): String = description
}