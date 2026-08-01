package com.votmari.bloodfoundation.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.votmari.bloodfoundation.auth.FirebasePhoneAuth
import com.votmari.bloodfoundation.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BloodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BloodRepository
    private val auth = FirebaseAuth.getInstance()

    private val prefs =
        application.getSharedPreferences(
            "login_session",
            Context.MODE_PRIVATE
        )

    init {
    val database = AppDatabase.getDatabase(application)
    repository = BloodRepository(database.dao())

    val savedRole = prefs.getString("role", null)

    if (prefs.getBoolean("logged_in", false) && savedRole != null) {
        _activeRole.value = savedRole
        _currentScreen.value =
            if (savedRole == "Super Admin")
                "dashboard"
            else
                "home"
    }
}

    // ----------------------------
    // Repository Flows
    // ----------------------------

    val allDonors =
        repository.allDonors.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val leaderboard =
        repository.leaderboard.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allBloodRequests =
        repository.allBloodRequests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val approvedBloodRequests =
        repository.approvedBloodRequests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allNotices =
        repository.allNotices.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allEvents =
        repository.allEvents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val chatMessages =
        repository.chatMessages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ----------------------------
    // Session
    // ----------------------------

    private val _currentUser =
        MutableStateFlow<DonorEntity?>(null)

    val currentUser =
        _currentUser.asStateFlow()

    private val _activeRole =
        MutableStateFlow("Donor")

    val activeRole =
        _activeRole.asStateFlow()

    private val _currentScreen =
        MutableStateFlow("onboarding")

    val currentScreen =
        _currentScreen.asStateFlow()


    // ----------------------------
    // Search Filters
    // ----------------------------

    private val _searchBloodGroup = MutableStateFlow("All")
    val searchBloodGroup = _searchBloodGroup.asStateFlow()

    private val _searchDivision = MutableStateFlow("Rangpur")
    val searchDivision = _searchDivision.asStateFlow()

    private val _searchDistrict = MutableStateFlow("Lalmonirhat")
    val searchDistrict = _searchDistrict.asStateFlow()

    private val _searchUpazila = MutableStateFlow("All")
    val searchUpazila = _searchUpazila.asStateFlow()

    val filteredDonors =
        combine(
            allDonors,
            _searchBloodGroup,
            _searchDivision,
            _searchDistrict,
            _searchUpazila
        ) { donors, bg, div, dist, upz ->

            donors.filter { donor ->
                donor.isApproved &&
                (bg == "All" || donor.bloodGroup == bg) &&
                (div == "All" || donor.division.equals(div, true)) &&
                (dist == "All" || donor.district.equals(dist, true)) &&
                (upz == "All" || donor.upazila.equals(upz, true))
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ----------------------------
    // Toast
    // ----------------------------

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // ----------------------------
    // Navigation
    // ----------------------------

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun selectSearchFilters(
        bg: String,
        div: String,
        dist: String,
        upz: String
    ) {
        _searchBloodGroup.value = bg
        _searchDivision.value = div
        _searchDistrict.value = dist
        _searchUpazila.value = upz
    }

    // ----------------------------
    // Login (Mobile)
    // ----------------------------

    fun login(
        mobileNumber: String,
        passwordOrOtp: String = ""
    ) {

        viewModelScope.launch {

            val user =
                repository.getDonorByMobile(mobileNumber)

            if (user != null) {

                _currentUser.value = user
                _activeRole.value = user.role
                _currentScreen.value = "home"

                prefs.edit()
                    .putBoolean("logged_in", true)
                    .putString("role", user.role)
                    .apply()

                showToast("স্বাগতম, ${user.fullName}")

            } else {

                showToast("এই নম্বরে কোনো একাউন্ট পাওয়া যায়নি")

            }

        }
    }

    fun logout() {

        prefs.edit().clear().apply()

        auth.signOut()

        _currentUser.value = null
        _activeRole.value = "Donor"
        _currentScreen.value = "onboarding"

        showToast("লগআউট সফল হয়েছে")
    }

    // ----------------------------
    // OTP Login
    // ----------------------------

    fun sendOtp(
        activity: android.app.Activity,
        phone: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        FirebasePhoneAuth.sendOtp(
            activity = activity,
            phone = phone,
            callbacks = callbacks
        )
    }

    fun verifyOtp(
        verificationId: String,
        otp: String,
        onSuccess: () -> Unit = {}
    ) {

        val credential =
            FirebasePhoneAuth.getCredential(
                verificationId,
                otp
            )

        FirebasePhoneAuth.signIn(
            credential = credential,

            onSuccess = {

                prefs.edit()
                    .putBoolean("logged_in", true)
                    .putString("role", "Donor")
                    .apply()

                _activeRole.value = "Donor"
                _currentScreen.value = "home"

                showToast("লগইন সফল হয়েছে")

                onSuccess()
            },

            onError = {
                showToast(it)
            }
        )
    }

    // ----------------------------
    // Email Login (Super Admin)
    // ----------------------------

    fun loginWithEmail(
        email: String,
        password: String
    ) {

        auth.signInWithEmailAndPassword(
            email,
            password
        )

            .addOnSuccessListener {

                prefs.edit()
                    .putBoolean("logged_in", true)
                    .putString("role", "Super Admin")
                    .apply()

                _activeRole.value = "Super Admin"
                _currentScreen.value = "dashboard"

                showToast("সুপার অ্যাডমিন লগইন সফল হয়েছে")
            }

            .addOnFailureListener {

                showToast(
                    it.message ?: "ইমেইল বা পাসওয়ার্ড ভুল"
                )
            }
    }

    // ----------------------------
    // Profile
    // ----------------------------

    fun saveProfile(updatedUser: DonorEntity) {
        viewModelScope.launch {
            repository.updateDonor(updatedUser)
            _currentUser.value = updatedUser
            showToast("প্রোফাইল সফলভাবে আপডেট হয়েছে")
            _currentScreen.value = "profile"
        }
    }

    // ----------------------------
    // Register
    // ----------------------------

    fun register(donor: DonorEntity) {
        viewModelScope.launch {

            val existing =
                repository.getDonorByMobile(donor.mobileNumber)

            if (existing != null) {
                showToast("এই মোবাইল নম্বরে ইতিমধ্যে একটি একাউন্ট রয়েছে")
                return@launch
            }

            repository.registerDonor(donor)

            _currentUser.value = donor
            _activeRole.value = donor.role
            _currentScreen.value = "home"

            prefs.edit()
                .putBoolean("logged_in", true)
                .putString("role", donor.role)
                .apply()

            showToast("রেজিস্ট্রেশন সফল হয়েছে")
        }
    }

    // ----------------------------
    // Repository Helper
    // ----------------------------

    fun approveDonor(mobile: String) =
        viewModelScope.launch {
            repository.approveDonor(mobile, true)
        }

    fun rejectDonor(mobile: String) =
        viewModelScope.launch {
            repository.deleteDonor(mobile)
        }

    fun changeUserRole(
        mobile: String,
        role: String
    ) = viewModelScope.launch {
        repository.updateDonorRole(mobile, role)
    }

    fun approveBloodRequest(id: Int) =
        viewModelScope.launch {
            repository.updateRequestStatus(id, true, "Approved")
        }

    fun rejectBloodRequest(id: Int) =
        viewModelScope.launch {
            repository.updateRequestStatus(id, false, "Cancelled")
        }

    fun completeBloodRequest(id: Int) =
        viewModelScope.launch {
            repository.updateRequestStatus(id, true, "Completed")
        }

    fun createBloodRequest(request: BloodRequestEntity) =
        viewModelScope.launch {
            repository.submitBloodRequest(request)
        }

    fun getDonationHistoryForDonor(mobile: String) =
        repository.getDonationHistoryForDonor(mobile)

    fun sendChat(message: String) {

        if (message.isBlank()) return

        viewModelScope.launch {

            repository.sendChatMessage(
                ChatMessageEntity(
                    senderName = _currentUser.value?.fullName ?: "Guest",
                    messageText = message,
                    isAdminMessage = _activeRole.value != "Donor"
                )
            )
        }
    }
}
