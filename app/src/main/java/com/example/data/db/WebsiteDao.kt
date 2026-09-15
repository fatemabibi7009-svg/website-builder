package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DeploymentEntity
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteDao {
    @Query("SELECT * FROM websites ORDER BY updatedAt DESC")
    fun getAllWebsites(): Flow<List<WebsiteEntity>>

    @Query("SELECT * FROM websites WHERE id = :id LIMIT 1")
    fun getWebsiteById(id: Long): Flow<WebsiteEntity?>

    @Query("SELECT * FROM websites WHERE id = :id LIMIT 1")
    suspend fun getWebsiteByIdSync(id: Long): WebsiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsite(website: WebsiteEntity): Long

    @Update
    suspend fun updateWebsite(website: WebsiteEntity)

    @Delete
    suspend fun deleteWebsite(website: WebsiteEntity)

    @Query("SELECT * FROM web_blocks WHERE websiteId = :websiteId ORDER BY orderIndex ASC")
    fun getBlocksForWebsite(websiteId: Long): Flow<List<WebBlockEntity>>

    @Query("SELECT * FROM web_blocks WHERE websiteId = :websiteId ORDER BY orderIndex ASC")
    suspend fun getBlocksForWebsiteSync(websiteId: Long): List<WebBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: WebBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<WebBlockEntity>)

    @Update
    suspend fun updateBlock(block: WebBlockEntity)

    @Update
    suspend fun updateBlocks(blocks: List<WebBlockEntity>)

    @Delete
    suspend fun deleteBlock(block: WebBlockEntity)

    @Query("DELETE FROM web_blocks WHERE websiteId = :websiteId")
    suspend fun deleteBlocksForWebsite(websiteId: Long)

    @Query("SELECT * FROM deployments WHERE websiteId = :websiteId ORDER BY timestamp DESC")
    fun getDeploymentsForWebsite(websiteId: Long): Flow<List<DeploymentEntity>>

    @Query("SELECT * FROM deployments ORDER BY timestamp DESC")
    fun getAllDeployments(): Flow<List<DeploymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeployment(deployment: DeploymentEntity): Long
}
