package todo.data.offline

import ro.ubbcluj.cs.ilazar.myandroid.todo.data.offline.OfflineOperation
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item

object OfflineItemRepository {
    private var offlineActions:HashMap<String,OfflineOperation> = HashMap()
    fun add(item:Item){
        offlineActions[item._id] = OfflineOperation("CREATED",
            Item(item._id,item.description, item.title))
    }
    fun delete(id:String){
        offlineActions[id] = OfflineOperation("DELETED",Item(id,"", ""))
    }
    fun update(item:Item){
        offlineActions[item._id] = OfflineOperation("UPDATED",
            Item(item._id,item.description, item.title))
    }
    fun removeAction(id:String){
        offlineActions.remove(id);
    }
    fun getOfflineActions():HashMap<String,OfflineOperation>{
        return offlineActions
    }

}