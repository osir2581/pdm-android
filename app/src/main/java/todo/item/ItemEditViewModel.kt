package ro.ubbcluj.cs.ilazar.myapp2.todo.item

import android.app.Application
import android.util.Log
import android.widget.ToggleButton
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.ilazar.myandroid.todo.data.local.TodoDatabase
import ro.ubbcluj.cs.ilazar.myapp2.core.Result
import ro.ubbcluj.cs.ilazar.myapp2.core.TAG
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.ItemRoomRepository
import todo.data.offline.OfflineItemRepository

class ItemEditViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted
    var connected:Boolean = true
    val itemRoomRepository: ItemRoomRepository

    init {
        val itemDao = TodoDatabase.getDatabase(application, viewModelScope).itemDao()
        itemRoomRepository = ItemRoomRepository(itemDao)
    }

    fun getItemById(itemId: String): LiveData<Item> {
        Log.v(TAG, "getItemById...")
        return itemRoomRepository.getById(itemId)
    }

    fun saveOrUpdateItem(item: Item) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdateItem...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Item>
            val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val randomString = (1..20)
                .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("");
            if (item._id.isNotEmpty()) {
                if(connected) {
                    Log.i("ParticipantEditViewMod","Connected - update")
                    result = itemRoomRepository.update(item)
                }
                else{
                    Log.i("ParticipantEditViewMod","Offline - update")
                    OfflineItemRepository.update(item)
                    itemRoomRepository.updateOffline(item)
                    result = Result.Success(item)
                }
            } else {
                if(connected) {
                    Log.i("ParticipantEditViewMod","Connected - create")
                    result = itemRoomRepository.save(item)
                }
                else{
                    item._id = randomString
                    Log.i("ParticipantEditViewMod","Offline - create")
                    OfflineItemRepository.add(item)
                    itemRoomRepository.saveOffline(item)
                    result = Result.Success(item)
                }
            }
            when(result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateItem succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }
}