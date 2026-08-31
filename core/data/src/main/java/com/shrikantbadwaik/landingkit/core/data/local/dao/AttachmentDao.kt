package com.shrikantbadwaik.landingkit.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments ORDER BY created_at ASC")
    suspend fun getAllAttachments(): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE trip_id = :tripId ORDER BY created_at DESC")
    fun observeAttachments(tripId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id LIMIT 1")
    suspend fun getAttachment(id: String): AttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Query(
        """UPDATE attachments
            SET is_quick_access = CASE WHEN id = :attachmentId THEN 1 ELSE 0 END
            WHERE trip_id = :tripId""",
    )
    suspend fun setQuickAccessAttachment(tripId: String, attachmentId: String?)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteAttachment(id: String)

    @Query("DELETE FROM attachments WHERE trip_id = :tripId")
    suspend fun deleteAttachments(tripId: String): Int
}
