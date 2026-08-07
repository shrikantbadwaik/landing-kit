package com.shrikantbadwaik.landingkit.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE trip_id = :tripId ORDER BY created_at DESC")
    fun observeAttachments(tripId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id LIMIT 1")
    suspend fun getAttachment(id: String): AttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteAttachment(id: String)
}
