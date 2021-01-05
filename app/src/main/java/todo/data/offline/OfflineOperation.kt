package ro.ubbcluj.cs.ilazar.myandroid.todo.data.offline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item

class OfflineOperation (
    var type: String,
    var recipe: Item,
)