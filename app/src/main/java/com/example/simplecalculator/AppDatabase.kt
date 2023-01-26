package com.example.simplecalculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.simplecalculator.dao.HistoryDao
import com.example.simplecalculator.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun historyDao(): HistoryDao
}