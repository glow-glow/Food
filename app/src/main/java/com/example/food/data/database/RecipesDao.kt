package com.example.food.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipesDao {

    @Insert
    suspend fun insertRecipes(recipesEntity: RecipesEntity)

    @Query("SELECT * FROM recipes_table ORDER BY id ASC")
    //Flow – холодные потоки (можно создать, а активизировать по необходимости)
    fun readRecipes(): kotlinx.coroutines.flow.Flow<List<RecipesEntity>>

}