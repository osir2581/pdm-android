package ro.ubbcluj.cs.ilazar.myapp2.todo.items

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.ilazar.myandroid.todo.data.local.TodoDatabase
import ro.ubbcluj.cs.ilazar.myapp2.core.Result
import ro.ubbcluj.cs.ilazar.myapp2.core.TAG
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.ItemRoomRepository

class ItemListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Item>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val itemRoomRepository: ItemRoomRepository

    init {
        val itemDao = TodoDatabase.getDatabase(application, viewModelScope).itemDao()
        itemRoomRepository = ItemRoomRepository(itemDao)
        items = itemRoomRepository.items
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = itemRoomRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }
}