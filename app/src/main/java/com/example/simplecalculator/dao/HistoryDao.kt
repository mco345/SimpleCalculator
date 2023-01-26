package com.example.simplecalculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.simplecalculator.model.History

@Dao
interface HistoryDao {

    // 모든 기록 불러오기
    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    // 조건에 부합하는 기록 불러오기
    @Query("SELECT * FROM history WHERE result LIKE :result ")
    fun findByResult(result: String): History

    // 삽입
    @Insert
    fun insertHistory(history: History)

    // 모든 기록 삭제
    @Query("DELETE FROM history")
    fun deleteAll()

    // 선택 기록 삭제
    @Delete
    fun delete(history: History)


}