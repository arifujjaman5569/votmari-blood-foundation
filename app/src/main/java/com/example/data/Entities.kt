package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey val mobileNumber: String, // Mobile number acts as unique ID / Login key
    val fullName: String,
    val fatherName: String,
    val motherName: String,
    val whatsAppNumber: String,
    val bloodGroup: String,
    val dateOfBirth: String,
    val gender: String,
    val occupation: String,
    val nationalIdNumber: String,
    val address: String,
    val division: String,
    val district: String,
    val upazila: String,
    val village: String,
    val profilePictureUrl: String = "",
    val lastBloodDonationDate: String = "",
    val totalBloodDonationCount: Int = 0,
    val weight: Double = 0.0,
    val emergencyContactNumber: String,
    val email: String = "",
    val role: String = "Donor", // "Super Admin", "Admin", "Moderator", "Volunteer", "Donor"
    val isApproved: Boolean = false,
    val registeredTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blood_requests")
data class BloodRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val bloodGroup: String,
    val bloodQuantity: String,
    val hospitalName: String,
    val doctorName: String,
    val contactPerson: String,
    val mobileNumber: String,
    val district: String,
    val upazila: String,
    val address: String,
    val requiredDate: String,
    val urgencyLevel: String, // "Emergency", "Urgent", "Normal"
    val description: String,
    val isApproved: Boolean = false,
    val status: String = "Pending", // "Pending", "Approved", "Completed", "Cancelled"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "donation_history")
data class DonationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val donorMobile: String,
    val donationDate: String,
    val hospitalName: String,
    val patientName: String,
    val bloodGroup: String,
    val quantity: String,
    val verifiedBy: String
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val publishedBy: String,
    val isEmergency: Boolean = false
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val type: String, // "Blood Donation Camp", "Awareness Program", "Volunteer Meeting"
    val description: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAdminMessage: Boolean = false
)
