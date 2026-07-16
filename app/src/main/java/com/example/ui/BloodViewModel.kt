package com.votmari.bloodfoundation.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.votmari.bloodfoundation.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BloodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BloodRepository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = BloodRepository(database.dao())
        viewModelScope.launch {
            repository.initializeMockDataIfNeeded()
        }
    }

    // --- Active State flows ---
    val allDonors: StateFlow<List<DonorEntity>> = repository.allDonors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<DonorEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBloodRequests: StateFlow<List<BloodRequestEntity>> = repository.allBloodRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedBloodRequests: StateFlow<List<BloodRequestEntity>> = repository.approvedBloodRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication & Session States ---
    private val _currentUser = MutableStateFlow<DonorEntity?>(null)
    val currentUser: StateFlow<DonorEntity?> = _currentUser.asStateFlow()

    private val _activeRole = MutableStateFlow("Donor")
    val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    // --- Navigation ---
    private val _currentScreen = MutableStateFlow("onboarding")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // --- Search Filters ---
    private val _searchBloodGroup = MutableStateFlow("All")
    val searchBloodGroup = _searchBloodGroup.asStateFlow()

    private val _searchDivision = MutableStateFlow("Rangpur")
    val searchDivision = _searchDivision.asStateFlow()

    private val _searchDistrict = MutableStateFlow("Lalmonirhat")
    val searchDistrict = _searchDistrict.asStateFlow()

    private val _searchUpazila = MutableStateFlow("All")
    val searchUpazila = _searchUpazila.asStateFlow()

    val filteredDonors: StateFlow<List<DonorEntity>> = combine(
        allDonors, _searchBloodGroup, _searchDivision, _searchDistrict, _searchUpazila
    ) { donors, bg, div, dist, upz ->
        donors.filter { donor ->
            donor.isApproved &&
            (bg == "All" || donor.bloodGroup == bg) &&
            (div == "All" || donor.division.equals(div, ignoreCase = true)) &&
            (dist == "All" || donor.district.equals(dist, ignoreCase = true)) &&
            (upz == "All" || donor.upazila.equals(upz, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Toast / Notification Helper State ---
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // --- Actions ---

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun selectSearchFilters(bg: String, div: String, dist: String, upz: String) {
        _searchBloodGroup.value = bg
        _searchDivision.value = div
        _searchDistrict.value = dist
        _searchUpazila.value = upz
    }

    // --- Authenticaton Actions ---
    fun login(mobileNumber: String, passwordOrOtp: String = "") {
        viewModelScope.launch {
            val user = repository.getDonorByMobile(mobileNumber)
            if (user != null) {
                _currentUser.value = user
                _activeRole.value = user.role
                _currentScreen.value = "home"
                showToast("স্বাগতম, ${user.fullName}! আপনি ${user.role} হিসেবে লগইন করেছেন।")
            } else {
                showToast("এই নম্বরে কোনো একাউন্ট পাওয়া যায়নি! অনুগ্রহ করে রেজিস্ট্রেশন করুন।")
            }
        }
    }

    fun loginAsDemoRole(role: String) {
        viewModelScope.launch {
            // Find a seeded user of this role, or login with temporary mock
            val demoMobile = when (role) {
                "Super Admin" -> "01700000001"
                "Admin" -> "01700000002"
                "Moderator" -> "01700000003"
                "Volunteer" -> "01700000004"
                else -> "01755555551"
            }
            val user = repository.getDonorByMobile(demoMobile)
            if (user != null) {
                _currentUser.value = user
                _activeRole.value = user.role
                _currentScreen.value = "home"
                showToast("ডেমো লগইন: ${user.fullName} (${user.role})")
            }
        }
    }

    fun overrideRoleForTesting(newRole: String) {
        _activeRole.value = newRole
        showToast("টেস্টিং মোড: রোল পরিবর্তন করে '$newRole' করা হয়েছে।")
    }

    fun logout() {
        _currentUser.value = null
        _activeRole.value = "Donor"
        _currentScreen.value = "onboarding"
        showToast("সফলভাবে লগআউট করা হয়েছে।")
    }

    fun register(donor: DonorEntity) {
        viewModelScope.launch {
            val existing = repository.getDonorByMobile(donor.mobileNumber)
            if (existing != null) {
                showToast("এই মোবাইল নাম্বার দিয়ে ইতিমধ্যে একটি একাউন্ট খোলা রয়েছে!")
                return@launch
            }
            repository.registerDonor(donor)
            _currentUser.value = donor
            _activeRole.value = donor.role
            _currentScreen.value = "home"
            showToast("রেজিস্ট্রেশন সফল হয়েছে! অ্যাডমিন অ্যাপ্রুভালের জন্য অপেক্ষা করুন।")
        }
    }

    // --- Admin Dashboard Actions ---
    fun approveDonor(mobile: String) {
        viewModelScope.launch {
            repository.approveDonor(mobile, true)
            showToast("ডোনার প্রোফাইল ভেরিফাই এবং অ্যাপ্রুভ করা হয়েছে।")
        }
    }

    fun rejectDonor(mobile: String) {
        viewModelScope.launch {
            repository.deleteDonor(mobile)
            showToast("ডোনার প্রোফাইল রিজেক্ট এবং রিমুভ করা হয়েছে।")
        }
    }

    fun changeUserRole(mobile: String, role: String) {
        viewModelScope.launch {
            repository.updateDonorRole(mobile, role)
            showToast("ইউজারের রোল পরিবর্তন করে '$role' করা হয়েছে।")
        }
    }

    fun approveBloodRequest(id: Int) {
        viewModelScope.launch {
            repository.updateRequestStatus(id, true, "Approved")
            showToast("রক্তের আবেদন অ্যাপ্রুভ করা হয়েছে।")
        }
    }

    fun rejectBloodRequest(id: Int) {
        viewModelScope.launch {
            repository.updateRequestStatus(id, false, "Cancelled")
            showToast("রক্তের আবেদন বাতিল করা হয়েছে।")
        }
    }

    fun completeBloodRequest(id: Int) {
        viewModelScope.launch {
            repository.updateRequestStatus(id, true, "Completed")
            showToast("রক্তের আবেদন সম্পন্ন হিসেবে চিহ্নিত করা হয়েছে।")
        }
    }

    // --- Blood Request Action ---
    fun createBloodRequest(request: BloodRequestEntity) {
        viewModelScope.launch {
            repository.submitBloodRequest(request)
            showToast("রক্তের আবেদন সাবমিট করা হয়েছে! অ্যাডমিন অ্যাপ্রুভালের পর এটি তালিকাভুক্ত হবে।")
            _currentScreen.value = "request"
        }
    }

    // --- Notice and Event Actions ---
    fun publishNotice(title: String, content: String, isEmergency: Boolean) {
        viewModelScope.launch {
            val notice = NoticeEntity(
                title = title,
                content = content,
                date = "Jul 01, 2026",
                publishedBy = _currentUser.value?.fullName ?: "Admin",
                isEmergency = isEmergency
            )
            repository.publishNotice(notice)
            showToast("নতুন নোটিশ প্রকাশ করা হয়েছে!")
            if (isEmergency) {
                showToast("🚨 পুশ নোটিফিকেশন পাঠানো হয়েছে সকল ম্যাচিং ডোনারদের কাছে!")
            }
        }
    }

    fun deleteNotice(id: Int) {
        viewModelScope.launch {
            repository.deleteNotice(id)
            showToast("নোটিশ মুছে ফেলা হয়েছে।")
        }
    }

    fun createEvent(title: String, date: String, time: String, location: String, type: String, description: String) {
        viewModelScope.launch {
            val event = EventEntity(
                title = title,
                date = date,
                time = time,
                location = location,
                type = type,
                description = description
            )
            repository.createEvent(event)
            showToast("নতুন ইভেন্ট তৈরি করা হয়েছে!")
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEvent(id)
            showToast("ইভেন্ট মুছে ফেলা হয়েছে।")
        }
    }

    // --- Chat Action ---
    fun sendChat(messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val sender = _currentUser.value?.fullName ?: "Guest User"
            val isAdmin = _activeRole.value != "Donor"
            repository.sendChatMessage(
                ChatMessageEntity(
                    senderName = sender,
                    messageText = messageText,
                    isAdminMessage = isAdmin
                )
            )
        }
    }

    // --- Record Donation Action ---
    fun recordDonation(donorMobile: String, date: String, hospital: String, patientName: String, bloodGroup: String) {
        viewModelScope.launch {
            val verifier = "${_currentUser.value?.fullName ?: "Volunteer"} (${_activeRole.value})"
            repository.addDonationHistory(
                DonationHistoryEntity(
                    donorMobile = donorMobile,
                    donationDate = date,
                    hospitalName = hospital,
                    patientName = patientName,
                    bloodGroup = bloodGroup,
                    quantity = "1 Bag",
                    verifiedBy = verifier
                )
            )
            showToast("রক্তদানের তথ্য সফলভাবে সংরক্ষণ করা হয়েছে। ডোনারের পয়েন্ট বৃদ্ধি পেয়েছে!")
        }
    }

    // --- Extra Tools State & Calculation ---
    fun getDonationHistoryForDonor(mobile: String): Flow<List<DonationHistoryEntity>> {
        return repository.getDonationHistoryForDonor(mobile)
    }

    // 1. BMI Calculation
    fun calculateBMI(weightKg: Double, heightCm: Double): Pair<Double, String> {
        if (weightKg <= 0 || heightCm <= 0) return Pair(0.0, "Invalid Inputs")
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        val category = when {
            bmi < 18.5 -> "Underweight (কম ওজন)"
            bmi < 25.0 -> "Healthy Weight (স্বাভাবিক ওজন)"
            bmi < 30.0 -> "Overweight (অতিরিক্ত ওজন)"
            else -> "Obese (স্থূলতা)"
        }
        return Pair(bmi, category)
    }

    // 2. Blood Donation Eligibility Checker
    fun checkDonationEligibility(age: Int, weightKg: Double, monthsSinceLastDonation: Double, hasDisease: Boolean): Pair<Boolean, String> {
        if (age < 18 || age > 65) return Pair(false, "বয়স অবশ্যই ১৮ থেকে ৬৫ বছরের মধ্যে হতে হবে।")
        if (weightKg < 45.0) return Pair(false, "ওজন কমপক্ষে ৪৫ কেজি বা তার বেশি হতে হবে।")
        if (monthsSinceLastDonation < 4.0 && monthsSinceLastDonation >= 0) return Pair(false, "পূর্ববর্তী রক্তদানের পর কমপক্ষে ৪ মাস সময় অতিবাহিত হতে হবে।")
        if (hasDisease) return Pair(false, "কোনো দীর্ঘমেয়াদী সংক্রামক বা জটিল রোগ থাকলে রক্তদান করা যাবে না।")
        return Pair(true, "অভিনন্দন! আপনি রক্তদানের জন্য সম্পূর্ণ উপযুক্ত।")
    }

    // 3. SOS Trigger
    fun triggerSOS(bloodGroup: String) {
        showToast("🚨 SOS সতর্কবার্তা পাঠানো হয়েছে! ভোটমারী ও আশেপাশের এলাকায় থাকা সকল $bloodGroup রক্তদাতার কাছে জরুরী মেসেজ পাঠানো হয়েছে।")
    }
}
