package com.haumealabs.kmpbase.base.storage

expect class Storage() {

    fun saveInt(label: String, value: Int)
    fun loadInt(label: String) : Int

    fun saveString(label: String, value: String)
    fun loadString(label: String) : String

    fun saveBoolean(label: String, value: Boolean)
    fun loadBoolean(label: String, defaultValue: Boolean = false) : Boolean
}