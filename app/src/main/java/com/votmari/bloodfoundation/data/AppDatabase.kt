package com.votmari.bloodfoundation.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodFoundationDao {
    // --- Donors ---
    @Query("SELECT * FROM donors ORDER BY registeredTimestamp DESC")
    fun getAllDonors(): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getDonorByMobile(mobile: String): DonorEntity?

    @Query("SELECT * FROM donors WHERE isApproved = 1 ORDER BY totalBloodDonationCount DESC")
    fun getLeaderboard(): Flow<List<DonorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity)

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Query("UPDATE donors SET isApproved = :approved WHERE mobileNumber = :mobile")
    suspend fun approveDonor(mobile: String, approved: Boolean)

    @Query("UPDATE donors SET role = :role WHERE mobileNumber = :mobile")
    suspend fun updateDonorRole(mobile: String, role: String)

    @Query("DELETE FROM donors WHERE mobileNumber = :mobile")
    suspend fun deleteDonor(mobile: String)

    // --- Blood Requests ---
    @Query("SELECT * FROM blood_requests ORDER BY timestamp DESC")
    fun getAllBloodRequests(): Flow<List<BloodRequestEntity>>

    @Query("SELECT * FROM blood_requests WHERE isApproved = 1 ORDER BY timestamp DESC")
    fun getApprovedBloodRequests(): Flow<List<BloodRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodRequest(request: BloodRequestEntity)

    @Query("UPDATE blood_requests SET isApproved = :approved, status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Int, approved: Boolean, status: String)

    // --- Donation History ---
    @Query("SELECT * FROM donation_history ORDER BY id DESC")
    fun getAllDonationHistory(): Flow<List<DonationHistoryEntity>>

    @Query("SELECT * FROM donation_history WHERE donorMobile = :mobile ORDER BY id DESC")
    fun getDonationHistoryForDonor(mobile: String): Flow<List<DonationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationHistory(donation: DonationHistoryEntity)

    // --- Notices ---
    @Query("SELECT * FROM notices ORDER BY id DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Int)

    // --- Events ---
    @Query("SELECT * FROM events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: Int)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)
}

@Database(
    entities = [
        DonorEntity::class,
        BloodRequestEntity::class,
        DonationHistoryEntity::class,
        NoticeEntity::class,
        EventEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): BloodFoundationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blood_foundation_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
