package ro.ubbcluj.cs.ilazar.myandroid.todo.data.offline

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item
import todo.data.offline.OfflineItemRepository
import todo.data.ItemServerRepository

class OfflineWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun doWork(): Result {

        // perform long running operation
        var offlineActions:HashMap<String,OfflineOperation>  = OfflineItemRepository.getOfflineActions()
        if(offlineActions.entries.size>0) {
            offlineActions.entries.removeIf {
                when (it.value.type) {
                    "DELETED" -> {
                        //CoroutineScope(Dispatchers.Main).launch { ItemServerRepository.delete(it.key) }
                        //return@removeIf true
                    }
                    "UPDATED" -> {
                        CoroutineScope(Dispatchers.Main).launch { ItemServerRepository.update(it.value.recipe) }
                        return@removeIf true
                    }
                    "CREATED" -> {
                        val event = Item(it.value.recipe._id, it.value.recipe.description, it.value.recipe.title)
                        CoroutineScope(Dispatchers.Main).launch { ItemServerRepository.save(event) }
                        //ItemServerRepository.deleteOffline(it.key)
                        return@removeIf false
                    }
                }
                return@removeIf true
            }
        }
        return Result.success()
    }
}