package ro.ubbcluj.cs.ilazar.myapp2.core

data class Notification(val type:String,val payload:Any) {
}
data class RemoveNotification(val _id:String)