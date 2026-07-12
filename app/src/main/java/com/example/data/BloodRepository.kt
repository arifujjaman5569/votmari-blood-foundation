package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BloodRepository(private val dao: BloodFoundationDao) {

    // --- Flow streams ---
    val allDonors: Flow<List<DonorEntity>> = dao.getAllDonors()
    val leaderboard: Flow<List<DonorEntity>> = dao.getLeaderboard()
    val allBloodRequests: Flow<List<BloodRequestEntity>> = dao.getAllBloodRequests()
    val approvedBloodRequests: Flow<List<BloodRequestEntity>> = dao.getApprovedBloodRequests()
    val allNotices: Flow<List<NoticeEntity>> = dao.getAllNotices()
    val allEvents: Flow<List<EventEntity>> = dao.getAllEvents()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    // --- Query methods ---
    suspend fun getDonorByMobile(mobile: String): DonorEntity? = dao.getDonorByMobile(mobile)

    fun getDonationHistoryForDonor(mobile: String): Flow<List<DonationHistoryEntity>> =
        dao.getDonationHistoryForDonor(mobile)

    fun getAllDonationHistory(): Flow<List<DonationHistoryEntity>> = dao.getAllDonationHistory()

    // --- Write actions ---
    suspend fun registerDonor(donor: DonorEntity) = dao.insertDonor(donor)
    suspend fun updateDonor(donor: DonorEntity) = dao.updateDonor(donor)
    suspend fun approveDonor(mobile: String, approved: Boolean) = dao.approveDonor(mobile, approved)
    suspend fun updateDonorRole(mobile: String, role: String) = dao.updateDonorRole(mobile, role)
    suspend fun deleteDonor(mobile: String) = dao.deleteDonor(mobile)

    suspend fun submitBloodRequest(request: BloodRequestEntity) = dao.insertBloodRequest(request)
    suspend fun updateRequestStatus(id: Int, approved: Boolean, status: String) =
        dao.updateRequestStatus(id, approved, status)

    suspend fun addDonationHistory(donation: DonationHistoryEntity) {
        dao.insertDonationHistory(donation)
        // Update donor stats
        val donor = dao.getDonorByMobile(donation.donorMobile)
        if (donor != null) {
            val updated = donor.copy(
                totalBloodDonationCount = donor.totalBloodDonationCount + 1,
                lastBloodDonationDate = donation.donationDate
            )
            dao.insertDonor(updated)
        }
    }

    suspend fun publishNotice(notice: NoticeEntity) = dao.insertNotice(notice)
    suspend fun deleteNotice(id: Int) = dao.deleteNotice(id)

    suspend fun createEvent(event: EventEntity) = dao.insertEvent(event)
    suspend fun deleteEvent(id: Int) = dao.deleteEvent(id)

    suspend fun sendChatMessage(message: ChatMessageEntity) = dao.insertChatMessage(message)

    // --- Mock Data Initializer ---
    suspend fun initializeMockDataIfNeeded() {
        val notices = dao.getAllNotices().firstOrNull()
        if (notices.isNullOrEmpty()) {
            // Seed notices
            dao.insertNotice(
                NoticeEntity(
                    title = "জরুরী রক্তদান শিবির ২০২৬",
                    content = "ভোটমারী হাই স্কুল প্রাঙ্গণে আগামী শুক্রবার সকাল ৯টা থেকে একটি বড় রক্তদান শিবিরের আয়োজন করা হয়েছে। সকল স্বেচ্ছাসেবী ও দাতাদের উপস্থিত থাকার অনুরোধ করা হচ্ছে।",
                    date = "Jul 01, 2026",
                    publishedBy = "Super Admin",
                    isEmergency = false
                )
            )
            dao.insertNotice(
                NoticeEntity(
                    title = "এমারজেন্সি O+ রক্ত প্রয়োজন",
                    content = "লালমনিরহাট সদর হাসপাতালে একজন থ্যালাসেমিয়া রোগীর জন্য জরুরী ২ ব্যাগ O+ রক্ত প্রয়োজন। অতিসত্বর যোগাযোগের অনুরোধ করা হচ্ছে।",
                    date = "Jul 01, 2026",
                    publishedBy = "Admin",
                    isEmergency = true
                )
            )
            dao.insertNotice(
                NoticeEntity(
                    title = "ভোটমারী ব্লাড ফাউন্ডেশনের পক্ষ থেকে অভিনন্দন",
                    content = "আমাদের নতুন মোবাইল অ্যাপ্লিকেশন সফলভাবে চালু হয়েছে। এখন থেকে আপনারা খুব সহজেই ডোনার সার্চ এবং রক্তদান সার্টিফিকেট ডাউনলোড করতে পারবেন।",
                    date = "Jun 30, 2026",
                    publishedBy = "Super Admin",
                    isEmergency = false
                )
            )

            // Seed events
            dao.insertEvent(
                EventEntity(
                    title = "রক্তদান ও সচেতনতা মূলক সভা",
                    date = "Friday, Jul 10, 2026",
                    time = "10:00 AM",
                    location = "ভোটমারী ইউনিয়ন পরিষদ মিলনায়তন",
                    type = "Awareness Program",
                    description = "গ্রামাঞ্চলে রক্তদানের গুরুত্ব ও ভ্রান্ত ধারণা দূর করতে সচেতনতা বৃদ্ধি সভা।"
                )
            )
            dao.insertEvent(
                EventEntity(
                    title = "স্বেচ্ছাসেবী সমন্বয় সভা ২০২৬",
                    date = "Sunday, Jul 15, 2026",
                    time = "04:00 PM",
                    location = "ফাউন্ডেশন অফিস, ভোটমারী স্টেশন রোড",
                    type = "Volunteer Meeting",
                    description = "মাসিক সমন্বয় সভা এবং ভবিষ্যৎ কার্যক্রম নির্ধারণ।"
                )
            )
            dao.insertEvent(
                EventEntity(
                    title = "ফ্রি ব্লাড গ্রুপিং ও ক্যাম্পেইন",
                    date = "Wednesday, Jul 22, 2026",
                    time = "09:00 AM",
                    location = "ভোটমারী বাজার সংলগ্ন মাঠ",
                    type = "Blood Donation Camp",
                    description = "বিনা মূল্যে রক্তের গ্রুপ পরীক্ষা এবং রক্তদানে উদ্বুদ্ধকরণ।"
                )
            )

            // Seed donors with different roles
            val seedDonors = listOf(
                DonorEntity(
                    mobileNumber = "01700000001",
                    fullName = "Sharif Ahmed (শরীফ আহমেদ)",
                    fatherName = "Abdur Rahman",
                    motherName = "Amena Begum",
                    whatsAppNumber = "01700000001",
                    bloodGroup = "A+",
                    dateOfBirth = "1990-05-12",
                    gender = "Male",
                    occupation = "Service",
                    nationalIdNumber = "1234567890",
                    address = "Votmari, Kaliganj, Lalmonirhat",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Kaliganj",
                    village = "Votmari",
                    profilePictureUrl = "avatar1",
                    lastBloodDonationDate = "2026-04-10",
                    totalBloodDonationCount = 12,
                    weight = 72.5,
                    emergencyContactNumber = "01700000000",
                    email = "admin@votmariblood.org",
                    role = "Super Admin",
                    isApproved = true
                ),
                DonorEntity(
                    mobileNumber = "01700000002",
                    fullName = "Mahbub Alam (মাহবুব আলম)",
                    fatherName = "Anwar Hossain",
                    motherName = "Momena Khatun",
                    whatsAppNumber = "01700000002",
                    bloodGroup = "B+",
                    dateOfBirth = "1994-08-22",
                    gender = "Male",
                    occupation = "Business",
                    nationalIdNumber = "1234567891",
                    address = "Votmari, Kaliganj, Lalmonirhat",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Kaliganj",
                    village = "Votmari",
                    profilePictureUrl = "avatar2",
                    lastBloodDonationDate = "2026-05-15",
                    totalBloodDonationCount = 8,
                    weight = 68.0,
                    emergencyContactNumber = "01711111111",
                    email = "mahbub@gmail.com",
                    role = "Admin",
                    isApproved = true
                ),
                DonorEntity(
                    mobileNumber = "01700000003",
                    fullName = "Nasrin Sultana (নাসরিন সুলতানা)",
                    fatherName = "Fazlul Haque",
                    motherName = "Sufia Begum",
                    whatsAppNumber = "01700000003",
                    bloodGroup = "O+",
                    dateOfBirth = "1997-03-15",
                    gender = "Female",
                    occupation = "Teacher",
                    nationalIdNumber = "1234567892",
                    address = "Tushbhandar, Kaliganj, Lalmonirhat",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Kaliganj",
                    village = "Tushbhandar",
                    profilePictureUrl = "avatar3",
                    lastBloodDonationDate = "2026-06-01",
                    totalBloodDonationCount = 5,
                    weight = 55.0,
                    emergencyContactNumber = "01722222222",
                    email = "nasrin@votmari.org",
                    role = "Moderator",
                    isApproved = true
                ),
                DonorEntity(
                    mobileNumber = "01700000004",
                    fullName = "Imran Khan (ইমরান খান)",
                    fatherName = "Nurul Islam",
                    motherName = "Jahanara Begum",
                    whatsAppNumber = "01700000004",
                    bloodGroup = "AB+",
                    dateOfBirth = "1999-11-30",
                    gender = "Male",
                    occupation = "Student",
                    nationalIdNumber = "1234567893",
                    address = "Hatibandha, Lalmonirhat",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Hatibandha",
                    village = "Hatibandha",
                    profilePictureUrl = "avatar4",
                    lastBloodDonationDate = "2026-03-25",
                    totalBloodDonationCount = 9,
                    weight = 65.0,
                    emergencyContactNumber = "01733333333",
                    email = "imran@gmail.com",
                    role = "Volunteer",
                    isApproved = true
                ),
                DonorEntity(
                    mobileNumber = "01755555551",
                    fullName = "Arif Hasan (আরিফ হাসান)",
                    fatherName = "Mokhlesur Rahman",
                    motherName = "Rasheda Begum",
                    whatsAppNumber = "01755555551",
                    bloodGroup = "O-",
                    dateOfBirth = "1992-01-10",
                    gender = "Male",
                    occupation = "Engineer",
                    nationalIdNumber = "1234567894",
                    address = "Votmari Station Para, Kaliganj",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Kaliganj",
                    village = "Votmari",
                    profilePictureUrl = "avatar5",
                    lastBloodDonationDate = "2026-05-10",
                    totalBloodDonationCount = 14,
                    weight = 75.0,
                    emergencyContactNumber = "01744444444",
                    email = "arif@gmail.com",
                    role = "Donor",
                    isApproved = true
                ),
                DonorEntity(
                    mobileNumber = "01755555552",
                    fullName = "Sadia Islam (সাদিয়া ইসলাম)",
                    fatherName = "Zahurul Islam",
                    motherName = "Sajeda Begum",
                    whatsAppNumber = "01755555552",
                    bloodGroup = "A-",
                    dateOfBirth = "1995-10-05",
                    gender = "Female",
                    occupation = "Banker",
                    nationalIdNumber = "1234567895",
                    address = "Patgram, Lalmonirhat",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Patgram",
                    village = "Patgram",
                    profilePictureUrl = "avatar6",
                    lastBloodDonationDate = "2026-02-18",
                    totalBloodDonationCount = 6,
                    weight = 58.0,
                    emergencyContactNumber = "01755555550",
                    email = "sadia@gmail.com",
                    role = "Donor",
                    isApproved = true
                ),
                DonorEntity(
                    mobileNumber = "01755555553",
                    fullName = "Kamil Hasan (কামিল হাসান)",
                    fatherName = "Abdul Halim",
                    motherName = "Roxana Begum",
                    whatsAppNumber = "01755555553",
                    bloodGroup = "B-",
                    dateOfBirth = "1988-12-14",
                    gender = "Male",
                    occupation = "Farmer",
                    nationalIdNumber = "1234567896",
                    address = "Aditmari, Lalmonirhat",
                    division = "Rangpur",
                    district = "Lalmonirhat",
                    upazila = "Aditmari",
                    village = "Sarpukur",
                    profilePictureUrl = "avatar7",
                    lastBloodDonationDate = "2026-04-01",
                    totalBloodDonationCount = 18,
                    weight = 70.0,
                    emergencyContactNumber = "01766666666",
                    email = "kamil@gmail.com",
                    role = "Donor",
                    isApproved = true
                )
            )

            for (donor in seedDonors) {
                dao.insertDonor(donor)
            }

            // Seed initial donation histories for Top Donors
            dao.insertDonationHistory(
                DonationHistoryEntity(
                    donorMobile = "01755555553",
                    donationDate = "2026-04-01",
                    hospitalName = "Rangpur Medical College",
                    patientName = "Rahima Khatun",
                    bloodGroup = "B-",
                    quantity = "1 Bag",
                    verifiedBy = "Imran Khan (Volunteer)"
                )
            )
            dao.insertDonationHistory(
                DonationHistoryEntity(
                    donorMobile = "01755555551",
                    donationDate = "2026-05-10",
                    hospitalName = "Lalmonirhat Sadar Hospital",
                    patientName = "Jashim Uddin",
                    bloodGroup = "O-",
                    quantity = "1 Bag",
                    verifiedBy = "Nasrin Sultana (Moderator)"
                )
            )
            dao.insertDonationHistory(
                DonationHistoryEntity(
                    donorMobile = "01700000001",
                    donationDate = "2026-04-10",
                    hospitalName = "Kaliganj Upazila Health Complex",
                    patientName = "Milon Mia",
                    bloodGroup = "A+",
                    quantity = "1 Bag",
                    verifiedBy = "Self"
                )
            )

            // Seed initial blood requests
            dao.insertBloodRequest(
                BloodRequestEntity(
                    patientName = "সুলতান মাহমুদ",
                    bloodGroup = "A+",
                    bloodQuantity = "2 Bags",
                    hospitalName = "রংপুর মেডিকেল কলেজ হাসপাতাল",
                    doctorName = "ডা. আর. কে. বর্মন",
                    contactPerson = "আরিফুল ইসলাম (ভাই)",
                    mobileNumber = "01799999991",
                    district = "Lalmonirhat",
                    upazila = "Kaliganj",
                    address = "ভোটমারী, কালীগঞ্জ",
                    requiredDate = "Jul 05, 2026",
                    urgencyLevel = "Emergency",
                    description = "জরুরী বাইপাস সার্জারির জন্য ২ ব্যাগ A+ রক্ত প্রয়োজন। রক্তদাতার যাতায়াত ভাড়া প্রদান করা হবে।",
                    isApproved = true,
                    status = "Approved"
                )
            )
            dao.insertBloodRequest(
                BloodRequestEntity(
                    patientName = "ফাতেমা বেগম",
                    bloodGroup = "O-",
                    bloodQuantity = "1 Bag",
                    hospitalName = "লালমনিরহাট সদর হাসপাতাল",
                    doctorName = "ডা. সুফিয়া খাতুন",
                    contactPerson = "আবুল কালাম (স্বামী)",
                    mobileNumber = "01799999992",
                    district = "Lalmonirhat",
                    upazila = "Kaliganj",
                    address = "ভোটমারী বাজার",
                    requiredDate = "Jul 08, 2026",
                    urgencyLevel = "Urgent",
                    description = "সিজারিয়ান অপারেশনের জন্য ১ ব্যাগ ও নেগেটিভ রক্ত প্রয়োজন। অতিসত্বর যোগাযোগ করুন।",
                    isApproved = false,
                    status = "Pending"
                )
            )

            // Seed initial chat messages
            dao.insertChatMessage(ChatMessageEntity(senderName = "Arif Hasan", messageText = "আসসালামু আলাইকুম, আমি ভোটমারী স্টেশন রোডে থাকি। আমার রক্তের গ্রুপ O-। কোনো প্রয়োজন হলে জানাবেন।", timestamp = System.currentTimeMillis() - 1000000, isAdminMessage = false))
            dao.insertChatMessage(ChatMessageEntity(senderName = "Imran Khan (Volunteer)", messageText = "ওয়ালাইকুম আসসালাম ভাই, ধন্যবাদ আপনার আগ্রহের জন্য। আমরা আপনার তথ্য সংরক্ষিত রেখেছি।", timestamp = System.currentTimeMillis() - 500000, isAdminMessage = true))
        }
    }
}
