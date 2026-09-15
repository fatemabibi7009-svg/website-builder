package com.example.data.repository

import com.example.data.db.WebsiteDao
import com.example.data.model.DeploymentEntity
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WebsiteRepository(private val dao: WebsiteDao) {

    val allWebsites: Flow<List<WebsiteEntity>> = dao.getAllWebsites()
    val allDeployments: Flow<List<DeploymentEntity>> = dao.getAllDeployments()

    fun getWebsite(id: Long): Flow<WebsiteEntity?> = dao.getWebsiteById(id)

    suspend fun getWebsiteSync(id: Long): WebsiteEntity? = dao.getWebsiteByIdSync(id)

    suspend fun createWebsite(website: WebsiteEntity): Long = dao.insertWebsite(website)

    suspend fun updateWebsite(website: WebsiteEntity) = dao.updateWebsite(website)

    suspend fun deleteWebsite(website: WebsiteEntity) = dao.deleteWebsite(website)

    fun getBlocks(websiteId: Long): Flow<List<WebBlockEntity>> = dao.getBlocksForWebsite(websiteId)

    suspend fun getBlocksSync(websiteId: Long): List<WebBlockEntity> = dao.getBlocksForWebsiteSync(websiteId)

    suspend fun insertBlock(block: WebBlockEntity) = dao.insertBlock(block)

    suspend fun insertBlocks(blocks: List<WebBlockEntity>) = dao.insertBlocks(blocks)

    suspend fun updateBlock(block: WebBlockEntity) = dao.updateBlock(block)

    suspend fun deleteBlock(block: WebBlockEntity) = dao.deleteBlock(block)

    suspend fun replaceAllBlocks(websiteId: Long, blocks: List<WebBlockEntity>) {
        dao.deleteBlocksForWebsite(websiteId)
        val normalized = blocks.mapIndexed { index, b -> b.copy(websiteId = websiteId, orderIndex = index) }
        dao.insertBlocks(normalized)
    }

    suspend fun reorderBlocks(blocks: List<WebBlockEntity>) {
        val updated = blocks.mapIndexed { index, b -> b.copy(orderIndex = index) }
        dao.updateBlocks(updated)
    }

    suspend fun moveBlock(websiteId: Long, blockId: String, direction: Int) {
        val current = dao.getBlocksForWebsiteSync(websiteId).toMutableList()
        val index = current.indexOfFirst { it.id == blockId }
        if (index == -1) return
        val targetIndex = index + direction
        if (targetIndex in current.indices) {
            val item = current.removeAt(index)
            current.add(targetIndex, item)
            val updated = current.mapIndexed { i, b -> b.copy(orderIndex = i) }
            dao.updateBlocks(updated)
        }
    }

    suspend fun duplicateBlock(block: WebBlockEntity) {
        val newBlock = block.copy(
            id = UUID.randomUUID().toString(),
            orderIndex = block.orderIndex + 1,
            title = if (block.title.isNotBlank()) "${block.title} (Copy)" else ""
        )
        dao.insertBlock(newBlock)
    }

    fun getDeployments(websiteId: Long): Flow<List<DeploymentEntity>> = dao.getDeploymentsForWebsite(websiteId)

    suspend fun recordDeployment(deployment: DeploymentEntity): Long = dao.insertDeployment(deployment)
}
