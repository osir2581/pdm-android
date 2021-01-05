package todo.data

import android.os.Build
import android.util.Log
import ro.ubbcluj.cs.ilazar.myapp2.core.RemoveNotification
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item

import ro.ubbcluj.cs.ilazar.myapp2.core.Result
import ro.ubbcluj.cs.ilazar.myapp2.core.WebSocketNotifications
import ro.ubbcluj.cs.ilazar.myapp2.core.getJson
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote.ItemApi
import ro.ubbcluj.cs.ilazar.myapp2.todo.items.ItemListFragment

object ItemServerRepository {
    private var cachedItems: MutableList<Item>? = null;

    suspend fun loadAll(): Result<List<Item>> {
        if (cachedItems != null) {
            return Result.Success(cachedItems as List<Item>);
        }
        try {
            val participants = ItemApi.service.find()
            cachedItems = mutableListOf()
            cachedItems?.addAll(participants)
            return Result.Success(cachedItems as List<Item>)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun load(participantId: String): Result<Item> {
        val item = cachedItems?.find { it._id == participantId }
        if (item != null) {
            return Result.Success(item)
        }
        try {
            return Result.Success(ItemApi.service.read(participantId))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun save(participant: Item): Result<Item> {
        try {
            val createdarticipant = ItemApi.service.create(participant)
            cachedItems?.add(createdarticipant)
            return Result.Success(createdarticipant)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(participant: Item): Result<Item> {
        try {
            val updatedParticipant = ItemApi.service.update(participant._id, participant)
            val index = cachedItems?.indexOfFirst { it._id == participant._id }
            if (index != null) {
                cachedItems?.set(index, updatedParticipant)
            }
            return Result.Success(updatedParticipant)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun receiveNotifications() {
        while (ItemListFragment.listenForNotifications) {
            val notification = WebSocketNotifications.channel.receive();
            Log.d("EventsListFragment", "Notification received in list view model")
            when (notification.type) {
                "deleted" -> {
                    Log.d("delete", "delete");
                    val removeNotif = getJson().fromJson(
                        getJson().toJson(notification.payload),
                        RemoveNotification::class.java
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cachedItems?.removeIf { it._id == removeNotif._id }
                    }
                }
                "updated" -> {
                    Log.d("Update", "Update")
                    val updatedEvent: Item =
                        getJson().fromJson(getJson().toJson(notification.payload), Item::class.java)
                    val index = cachedItems?.indexOfFirst { it._id == updatedEvent._id }

                    if (index != null)
                        cachedItems?.set(index, updatedEvent)
                    Log.d("Update Index", "Index:$index")
                }
                "created" -> {
                    val newEvent =
                        getJson().fromJson(getJson().toJson(notification.payload), Item::class.java)
                    cachedItems?.add(newEvent)
                }
            }
        }
    }
}