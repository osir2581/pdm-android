package ro.ubbcluj.cs.ilazar.myapp2.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*

fun getJson(): Gson {
    return  GsonBuilder().create()
}
fun uniqueId():String = UUID.randomUUID().toString()