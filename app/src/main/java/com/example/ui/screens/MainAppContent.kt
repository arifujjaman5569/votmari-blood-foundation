package com.votmari.bloodfoundation.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.votmari.bloodfoundation.R
import com.votmari.bloodfoundation.data.*
import com.votmari.bloodfoundation.ui.BloodViewModel
import com.votmari.bloodfoundation.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: BloodViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Listen for toast messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != "onboarding") {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ভোটমারী ব্লাড ফাউন্ডেশন",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Votmari Blood Foundation",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setScreen("home") }) {
                            Icon(Icons.Filled.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        // Display active role badge and click to simulate other roles!
                        var showRolePicker by remember { mutableStateOf(false) }
                        AssistChip(
                            onClick = { showRolePicker = true },
                            label = { Text(activeRole, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )

                        if (showRolePicker) {
                            AlertDialog(
                                onDismissRequest = { showRolePicker = false },
                                title = { Text("রোল টেস্টিং মোড (Select Role)", fontSize = 16.sp) },
                                text = {
                                    Column {
                                        Text("অ্যাপের ৪ ধরনের ইউজার রোল সরাসরি টেস্ট করতে নিচে যেকোনো একটি নির্বাচন করুন:", fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                                        val roles = listOf("Super Admin", "Admin", "Moderator", "Volunteer", "Donor")
                                        roles.forEach { role ->
                                            Button(
                                                onClick = {
                                                    viewModel.overrideRoleForTesting(role)
                                                    showRolePicker = false
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (activeRole == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    contentColor = if (activeRole == role) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            ) {
                                                Text(role)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showRolePicker = false }) {
                                        Text("বন্ধ করুন")
                                    }
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != "onboarding") {
                NavigationBar(
                    modifier = Modifier.border(width = 1.dp, color = Color(0xFFF1F5F9)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(
                        Triple("home", Icons.Outlined.Home, Icons.Filled.Home to "হোম"),
                        Triple("search", Icons.Outlined.Search, Icons.Filled.Search to "ডোনার খুঁজুন"),
                        Triple("request", Icons.Outlined.LocalHospital, Icons.Filled.LocalHospital to "আবেদন"),
                        Triple("leaderboard", Icons.Outlined.Leaderboard, Icons.Filled.Leaderboard to "লিডারবোর্ড"),
                        Triple("profile", Icons.Outlined.Person, Icons.Filled.Person to "প্রোফাইল"),
                        Triple("extras", Icons.Outlined.Dashboard, Icons.Filled.Dashboard to "টুলস")
                    )

                    items.forEach { (route, outlineIcon, filledAndLabel) ->
                        val isSelected = currentScreen == route
                        val icon = if (isSelected) filledAndLabel.first else outlineIcon
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setScreen(route) },
                            icon = { Icon(icon, contentDescription = filledAndLabel.second) },
                            label = { Text(filledAndLabel.second, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Quick action to request blood
            if (currentScreen == "home") {
                ExtendedFloatingActionButton(
                    text = { Text("রক্তের আবেদন", color = Color.White, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Request Blood", tint = Color.White) },
                    onClick = { viewModel.setScreen("request") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("fab_request_blood")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "onboarding" -> OnboardingScreen(viewModel)
                "home" -> HomeScreen(viewModel)
                "search" -> SearchDonorScreen(viewModel)
                "request" -> RequestBloodScreen(viewModel)
                "leaderboard" -> LeaderboardScreen(viewModel)
                "profile" -> ProfileScreen(viewModel)
                "extras" -> ExtraToolsScreen(viewModel)
                "dashboard" -> AdminDashboardScreen(viewModel)
            }
        }
    }
}

// --- ONBOARDING & WELCOME FLOW ---
@Composable
fun OnboardingScreen(viewModel: BloodViewModel) {
    var isLoginMode by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Header
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .shadow(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "VBF Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ভোটমারী ব্লাড ফাউন্ডেশন",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Votmari Blood Foundation",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large Custom Hero Illustration Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "Blood Donation Hero banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLoginMode && !isRegisterMode) {
            // Welcome Greeting
            Text(
                text = "“রক্তের বন্ধনে কাটুক শঙ্কা, আমরা লড়বো করব জয়”",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ভোটমারী ইউনিয়ন তথা কালীগঞ্জ ও সমগ্র লালমনিরহাট জেলায় জরুরী রক্ত সংগ্রহ ও মানবতার সেবায় নিয়োজিত একটি সামাজিক স্বেচ্ছাসেবী প্রতিষ্ঠান।",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = { isLoginMode = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_onboard_login"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Login, contentDescription = "Login", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("একাউন্টে লগইন করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { isRegisterMode = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_onboard_register"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AppRegistration, contentDescription = "Register", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("রক্তদাতা হিসেবে রেজিস্ট্রেশন", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

        } else if (isLoginMode) {
            LoginWidget(
                onBack = { isLoginMode = false },
                onLoginSubmit = { mobile ->
                    viewModel.login(mobile)
                },
                viewModel = viewModel
            )
        } else {
            RegisterWidget(
                onBack = { isRegisterMode = false },
                onRegisterSubmit = { donor ->
                    viewModel.register(donor)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginWidget(onBack: () -> Unit, onLoginSubmit: (String) -> Unit, viewModel: BloodViewModel) {
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMethod by remember { mutableStateOf("Mobile OTP") } // "Mobile OTP", "Email", "Google"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("একাউন্টে প্রবেশ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Method Tabs
            TabRow(
                selectedTabIndex = when(loginMethod) {
                    "Mobile OTP" -> 0
                    "Email" -> 1
                    else -> 2
                },
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = loginMethod == "Mobile OTP",
                    onClick = { loginMethod = "Mobile OTP" },
                    text = { Text("মোবাইল OTP", fontSize = 12.sp) }
                )
                Tab(
                    selected = loginMethod == "Email",
                    onClick = { loginMethod = "Email" },
                    text = { Text("ইমেইল", fontSize = 12.sp) }
                )
                Tab(
                    selected = loginMethod == "Google",
                    onClick = { loginMethod = "Google" },
                    text = { Text("গুগল", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (loginMethod) {
                "Mobile OTP" -> {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("মোবাইল নম্বর (যেমন: 01755555551)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                        modifier = Modifier.fillMaxWidth().testTag("login_mobile_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("OTP কোড (যেকোনো কিছু লিখুন)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "OTP") },
                        modifier = Modifier.fillMaxWidth().testTag("login_otp_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                "Email" -> {
                    var email by remember { mutableStateOf("") }
                    var emailPass by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ইমেইল ঠিকানা") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailPass,
                        onValueChange = { emailPass = it },
                        label = { Text("পাসওয়ার্ড") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    TextButton(
                        onClick = { viewModel.showToast("পাসওয়ার্ড পুনরুদ্ধারের লিঙ্ক ইমেইলে পাঠানো হয়েছে।") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("পাসওয়ার্ড ভুলে গেছেন?")
                    }
                }
                "Google" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("আপনার গুগল একাউন্ট ব্যবহার করে ঝটপট সাইন ইন করুন।", fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.showToast("Google Login শীঘ্রই আসছে")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Google", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("গুগল একাউন্ট চয়ন করুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loginMethod != "Google") {
                Button(
                    onClick = {
                        if (mobileNumber.isBlank() && loginMethod == "Mobile OTP") {
                            viewModel.showToast("অনুগ্রহ করে মোবাইল নম্বর প্রদান করুন!")
                        } else {
                            onLoginSubmit(mobileNumber)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("লগইন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RegisterWidget(onBack: () -> Unit, onRegisterSubmit: (DonorEntity) -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var whatsAppNumber by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("A+") }
    var dob by remember { mutableStateOf("1998-05-15") }
    var gender by remember { mutableStateOf("Male") }
    var occupation by remember { mutableStateOf("Student") }
    var nid by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("Rangpur") }
    var district by remember { mutableStateOf("Lalmonirhat") }
    var upazila by remember { mutableStateOf("Kaliganj") }
    var village by remember { mutableStateOf("Votmari") }
    var weight by remember { mutableStateOf("65.0") }
    var emergencyContact by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("নতুন রক্তদাতা রেজিস্ট্রেশন", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable fields container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("পূর্ণ নাম (Full Name)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = fatherName, onValueChange = { fatherName = it }, label = { Text("পিতার নাম (Father's Name)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = motherName, onValueChange = { motherName = it }, label = { Text("মাতার নাম (Mother's Name)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = { Text("মোবাইল নম্বর (লগইন আইডি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = whatsAppNumber, onValueChange = { whatsAppNumber = it }, label = { Text("হোয়াটসঅ্যাপ নম্বর (WhatsApp Number)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                // Blood Group Selection Spinner Simulated
                Text("রক্তের গ্রুপ (Blood Group):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                val groups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                    groups.forEach { bg ->
                        val isSel = bloodGroup == bg
                        FilterChip(
                            selected = isSel,
                            onClick = { bloodGroup = bg },
                            label = { Text(bg) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("জন্ম তারিখ (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                Text("লিঙ্গ (Gender):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row {
                    listOf("Male", "Female", "Other").forEach { g ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                            RadioButton(selected = gender == g, onClick = { gender = g })
                            Text(g)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("পেশা (Occupation)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nid, onValueChange = { nid = it }, label = { Text("জাতীয় পরিচয়পত্র (NID Number)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                Text("ঠিকানা (Address):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(value = division, onValueChange = { division = it }, label = { Text("বিভাগ") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("জেলা") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = upazila, onValueChange = { upazila = it }, label = { Text("উপজেলা (যেমন: Kaliganj)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("গ্রাম/ইউনিয়ন") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("বিস্তারিত ঠিকানা (Address)") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("ওজন (কেজি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("জরুরি যোগাযোগ নম্বর") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল (ঐচ্ছিক)") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || mobileNumber.isBlank() || emergencyContact.isBlank()) {
                        // validation
                    } else {
                        val donor = DonorEntity(
                            mobileNumber = mobileNumber,
                            fullName = fullName,
                            fatherName = fatherName,
                            motherName = motherName,
                            whatsAppNumber = whatsAppNumber,
                            bloodGroup = bloodGroup,
                            dateOfBirth = dob,
                            gender = gender,
                            occupation = occupation,
                            nationalIdNumber = nid,
                            address = address,
                            division = division,
                            district = district,
                            upazila = upazila,
                            village = village,
                            weight = weight.toDoubleOrNull() ?: 60.0,
                            emergencyContactNumber = emergencyContact,
                            email = email,
                            role = "Donor",
                            isApproved = false // Pending approval
                        )
                        onRegisterSubmit(donor)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("তথ্য জমা দিন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// --- SCREEN 1: HOME PAGE ---
@Composable
fun HomeScreen(viewModel: BloodViewModel) {
    val notices by viewModel.allNotices.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcoming & Urgent Blood Request Card (Glass Effect matching HTML)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), clip = false)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE11D48), // Rose 600
                                Color(0xFFF43F5E)  // Rose 500
                            )
                        )
                    )
            ) {
                // Background white/10 decorative blurry light blobs
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .offset(x = 110.dp, y = (-40).dp)
                        .background(Color.White.copy(alpha = 0.12f), shape = CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "জরুরী আবেদন (URGENT REQUEST)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text("২ মিনিট আগে", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "O+ পজিটিভ রক্তের প্রয়োজন",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "রংপুর মেডিকেল কলেজ হাসপাতাল (RMCH)",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectSearchFilters("O+", "Rangpur", "Lalmonirhat", "All")
                                viewModel.setScreen("search")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFE11D48)
                            )
                        ) {
                            Text("ডোনেট করুন (Donate Now)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Glass heart action button
                        IconButton(
                            onClick = { viewModel.showToast("আবেদনটি প্রিয় তালিকায় যুক্ত করা হয়েছে।") },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "Favorite",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Fast Blood Group Grid Selection (matching HTML grid-cols-4)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "গ্রুপ অনুযায়ী রক্তদাতা খুঁজুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Slate900
                )
                Text(
                    text = "সব দেখুন (View All)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11D48),
                    modifier = Modifier.clickable {
                        viewModel.setScreen("search")
                    }
                )
            }

            val bgs = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bgs.take(4).forEach { bg ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .border(width = 1.dp, color = Color(0xFFF1F5F9), shape = RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.selectSearchFilters(bg, "Rangpur", "Lalmonirhat", "All")
                                    viewModel.setScreen("search")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bg,
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                // Second Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bgs.drop(4).forEach { bg ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .border(width = 1.dp, color = Color(0xFFF1F5F9), shape = RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.selectSearchFilters(bg, "Rangpur", "Lalmonirhat", "All")
                                    viewModel.setScreen("search")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bg,
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Statistics / Quick Insight Section (matching HTML stats row)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Donors
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                        .background(Color(0xFFFFF1F2), shape = RoundedCornerShape(24.dp)) // bg-rose-50
                        .border(width = 1.dp, color = Color(0xFFFFE4E6), shape = RoundedCornerShape(24.dp)) // border-rose-100
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "১,২৪৮",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE11D48) // text-rose-600
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "সক্রিয় রক্তদাতা",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFB7185) // text-rose-400
                    )
                    Text(
                        text = "ACTIVE DONORS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFB7185).copy(alpha = 0.8f)
                    )
                }

                // Success Lives
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                        .background(Color(0x80F1F5F9), shape = RoundedCornerShape(24.dp)) // bg-slate-100/50 (glassy)
                        .border(width = 1.dp, color = Color(0xFFF1F5F9).copy(alpha = 0.5f), shape = RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "৩৫২+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF334155) // text-slate-700
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "সফল জীবন রক্ষা",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8) // text-slate-400
                    )
                    Text(
                        text = "SUCCESS LIVES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8).copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Administrative shortcut button
        if (activeRole != "Donor") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { viewModel.setScreen("dashboard") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = "Dashboard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("অ্যাডমিন ড্যাশবোর্ড (Admin Control)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Text("নতুন ডোনার যাচাই, রক্তের আবেদন এবং নোটিশ প্রকাশ করুন।", fontSize = 11.sp, color = OnLightBackground.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // Notices Board Section (Emergency notices highlighted)
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সর্বশেষ নোটিশ বোর্ড",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "সব দেখুন",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }
        }

        val emergencyNotices = notices.filter { it.isEmergency }
        val regularNotices = notices.filter { !it.isEmergency }

        if (notices.isEmpty()) {
            item {
                Text("কোনো নোটিশ নেই।", fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            // Pulse effect for emergency notice
            items(emergencyNotices) { notice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = "Emergency", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("জরুরী নোটিশ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(notice.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.content, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            items(regularNotices) { notice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row {
                            Text(notice.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.weight(1f))
                            Text("প্রকাশক: ${notice.publishedBy}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.content, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Events Board Section
        item {
            Text(
                text = "আসন্ন ইভেন্ট ও ক্যাম্পেইন",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        if (events.isEmpty()) {
            item {
                Text("কোনো আসন্ন ইভেন্ট পাওয়া যায়নি।", fontSize = 12.sp)
            }
        } else {
            items(events) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColor = when (event.type) {
                                "Blood Donation Camp" -> MaterialTheme.colorScheme.primary
                                "Awareness Program" -> SuccessGreen
                                else -> PendingOrange
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(event.type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.showToast("ইভেন্ট ক্যালেন্ডারে যোগ করা হয়েছে।") }) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = "Add Calendar", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Time", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${event.date} • ${event.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Filled.Place, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(event.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.showToast("আপনি এই ইভেন্টে সফলভাবে RSVP করেছেন!") },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("অংশগ্রহণ নিশ্চিত করুন (RSVP)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Blood Donation Tips Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 রক্তদানের সুস্থতার টিপস", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• রক্তদানের পূর্বে পর্যাপ্ত জল বা জুস পান করুন।\n• খালি পেটে রক্তদান করা থেকে বিরত থাকুন।\n• রক্তদানের পর অন্তত ১০-১৫ মিনিট বিশ্রাম নিন।\n• পরবর্তী ২৪ ঘণ্টা ভারী কাজ করা থেকে বিরত থাকুন।", fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}


// --- SCREEN 2: SEARCH DONOR SCREEN ---
@Composable
fun SearchDonorScreen(viewModel: BloodViewModel) {
    val searchBg by viewModel.searchBloodGroup.collectAsState()
    val searchDiv by viewModel.searchDivision.collectAsState()
    val searchDist by viewModel.searchDistrict.collectAsState()
    val searchUpz by viewModel.searchUpazila.collectAsState()
    val donorsList by viewModel.filteredDonors.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("রক্তদাতার খোঁজ করুন (Search Donor)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        // Advanced Search Form Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Blood group filter
                Text("রক্তের গ্রুপ (Blood Group)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                val groups = listOf("All", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                    groups.forEach { bg ->
                        FilterChip(
                            selected = searchBg == bg,
                            onClick = { viewModel.selectSearchFilters(bg, searchDiv, searchDist, searchUpz) },
                            label = { Text(bg) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Upazila selector (All, Kaliganj, Hatibandha, Patgram, Aditmari)
                Text("উপজেলা (Lalmonirhat Upazila)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                val upazilas = listOf("All", "Kaliganj", "Hatibandha", "Patgram", "Aditmari")
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                    upazilas.forEach { upz ->
                        FilterChip(
                            selected = searchUpz == upz,
                            onClick = { viewModel.selectSearchFilters(searchBg, searchDiv, searchDist, upz) },
                            label = { Text(upz) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "মোট রক্তদাতা পাওয়া গেছে: ${donorsList.size} জন",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (donorsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.SearchOff, contentDescription = "Not found", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("এই এলাকায় বা গ্রুপে কোনো ডোনার পাওয়া যায়নি!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("অন্যান্য উপজেলা বা গ্রুপ নির্বাচন করে পুনরায় চেষ্টা করুন।", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(donorsList) { donor ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("donor_card_${donor.mobileNumber}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Blood Group Avatar Bubble
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(donor.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(donor.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${donor.village}, ${donor.upazila}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                                    Icon(Icons.Filled.Bloodtype, contentDescription = "Donations", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("মোট রক্তদান: ${donor.totalBloodDonationCount} বার", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                if (donor.lastBloodDonationDate.isNotBlank()) {
                                    Text("সর্বশেষ রক্তদান: ${donor.lastBloodDonationDate}", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                                } else {
                                    Text("সর্বশেষ রক্তদান: এখনও রক্তদান করেননি", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Dynamic communication buttons
                            Column(horizontalAlignment = Alignment.End) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.mobileNumber}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = "Call Donor", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val url = "https://api.whatsapp.com/send?phone=${donor.whatsAppNumber}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Filled.Chat, contentDescription = "WhatsApp Chat", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SCREEN 3: BLOOD REQUEST SUBMIT & ADMIN APPROVAL SCREEN ---
@Composable
fun RequestBloodScreen(viewModel: BloodViewModel) {
    val requestsList by viewModel.allBloodRequests.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("A+") }
    var quantity by remember { mutableStateOf("1 Bag") }
    var hospital by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var contactMobile by remember { mutableStateOf("") }
    var requiredDate by remember { mutableStateOf("As soon as possible") }
    var urgency by remember { mutableStateOf("Emergency") }
    var description by remember { mutableStateOf("") }

    var isSubmittingForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isSubmittingForm) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("রক্তের আবেদন তালিকা (Requests)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Button(
                        onClick = { isSubmittingForm = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("নতুন আবেদন", fontSize = 12.sp)
                    }
                }
            }

            // Display Pending / Approved Requests
            val visibleRequests = if (activeRole == "Super Admin" || activeRole == "Admin" || activeRole == "Moderator") {
                requestsList // Admins see all pending/completed requests
            } else {
                requestsList.filter { it.isApproved || it.status == "Completed" } // Donors only see approved
            }

            if (visibleRequests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                        Text("বর্তমানে কোনো রক্তের আবেদন নেই।")
                    }
                }
            } else {
                items(visibleRequests) { req ->
                    val isPending = req.status == "Pending"
                    val isCompleted = req.status == "Completed"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("request_item_${req.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (req.urgencyLevel == "Emergency" && !isCompleted) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            } else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (req.urgencyLevel == "Emergency" && !isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Blood group red bubble
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(req.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("রোগী: ${req.patientName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("প্রয়োজন: ${req.bloodQuantity}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.weight(1f))

                                // Status label
                                val statusColor = when (req.status) {
                                    "Pending" -> PendingOrange
                                    "Completed" -> SuccessGreen
                                    "Approved" -> MaterialTheme.colorScheme.primary
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(req.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("🏥 হাসপাতাল: ${req.hospitalName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("📅 তারিখ: ${req.requiredDate}", fontSize = 12.sp)
                            Text("📞 যোগাযোগ: ${req.contactPerson} (${req.mobileNumber})", fontSize = 12.sp)
                            if (req.description.isNotBlank()) {
                                Text("📝 বিবরণ: ${req.description}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons based on Role
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (activeRole == "Super Admin" || activeRole == "Admin" || activeRole == "Moderator") {
                                    if (isPending) {
                                        TextButton(onClick = { viewModel.rejectBloodRequest(req.id) }) {
                                            Text("বাতিল করুন", color = MaterialTheme.colorScheme.primary)
                                        }
                                        Button(onClick = { viewModel.approveBloodRequest(req.id) }) {
                                            Text("অনুমোদন")
                                        }
                                    } else if (req.status == "Approved") {
                                        Button(
                                            onClick = { viewModel.completeBloodRequest(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Text("সম্পন্ন করুন")
                                        }
                                    }
                                }

                                if (req.status == "Approved") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val url = "https://api.whatsapp.com/send?text=জরুরি রক্ত প্রয়োজন! রোগীর নাম: ${req.patientName}, গ্রুপ: ${req.bloodGroup}, হাসপাতাল: ${req.hospitalName}, যোগাযোগের নম্বর: ${req.mobileNumber}। ভোটমারী ব্লাড ফাউন্ডেশনের সাথে যোগাযোগ করুন।"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("শেয়ার করুন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Blood request submit form
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSubmittingForm = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text("রক্তের জন্য আবেদন ফরম", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("রোগীর নাম (Patient Name)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("প্রয়োজনীয় রক্তের গ্রুপ:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        val groups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                            groups.forEach { bg ->
                                FilterChip(
                                    selected = bloodGroup == bg,
                                    onClick = { bloodGroup = bg },
                                    label = { Text(bg) },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("রক্তের পরিমাণ (যেমন: ২ ব্যাগ)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = hospital, onValueChange = { hospital = it }, label = { Text("হাসপাতাল এবং ঠিকানা") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = { Text("যোগাযোগকারী ব্যক্তি (Contact Person)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = contactMobile, onValueChange = { contactMobile = it }, label = { Text("মোবাইল নম্বর") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = requiredDate, onValueChange = { requiredDate = it }, label = { Text("রক্তদানের তারিখ ও সময়") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("জরুরী অবস্থা (Urgency):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row {
                            listOf("Emergency", "Urgent", "Normal").forEach { u ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                                    RadioButton(selected = urgency == u, onClick = { urgency = u })
                                    Text(u)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("অন্যান্য তথ্য বা বিবরণ") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (patientName.isBlank() || hospital.isBlank() || contactMobile.isBlank()) {
                                    viewModel.showToast("অনুগ্রহ করে আবশ্যকীয় তথ্য পূরণ করুন!")
                                } else {
                                    val req = BloodRequestEntity(
                                        patientName = patientName,
                                        bloodGroup = bloodGroup,
                                        bloodQuantity = quantity,
                                        hospitalName = hospital,
                                        doctorName = "Duty Doctor",
                                        contactPerson = contactPerson,
                                        mobileNumber = contactMobile,
                                        district = "Lalmonirhat",
                                        upazila = "Kaliganj",
                                        address = hospital,
                                        requiredDate = requiredDate,
                                        urgencyLevel = urgency,
                                        description = description,
                                        isApproved = false,
                                        status = "Pending"
                                    )
                                    viewModel.createBloodRequest(req)
                                    isSubmittingForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("আবেদন জমা দিন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// --- SCREEN 4: TOP DONOR LEADERBOARD SCREEN ---
@Composable
fun LeaderboardScreen(viewModel: BloodViewModel) {
    val leaderboardList by viewModel.leaderboard.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("সম্মানিত রক্তদাতা লিডারবোর্ড", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Text("সর্বোচ্চ রক্তদানের ভিত্তিতে ডোনারদের তালিকা।", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        // Top 3 Podium Visuals
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd Place (Silver)
            if (leaderboardList.size > 1) {
                PodiumCard(leaderboardList[1], 2, SilverColor)
            }
            // 1st Place (Gold)
            if (leaderboardList.isNotEmpty()) {
                PodiumCard(leaderboardList[0], 1, GoldColor)
            }
            // 3rd Place (Bronze)
            if (leaderboardList.size > 2) {
                PodiumCard(leaderboardList[2], 3, BronzeColor)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable list for 4th onwards
        Text("র‍্যাংকিং তালিকা", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val remainDonors = if (leaderboardList.size > 3) leaderboardList.drop(3) else emptyList()
            if (remainDonors.isEmpty() && leaderboardList.size <= 3) {
                item {
                    Text("বর্তমানে কোনো অতিরিক্ত তালিকাভুক্ত রক্তদাতা নেই।", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                itemsIndexed(remainDonors) { idx, donor ->
                    val rank = idx + 4
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#$rank",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.width(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(donor.bloodGroup, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(donor.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("উপজেলা: ${donor.upazila}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${donor.totalBloodDonationCount} বার", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Text("রক্তদান", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCard(donor: DonorEntity, rank: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            // Medal icon
            Icon(
                Icons.Filled.Stars,
                contentDescription = "Medal",
                tint = color,
                modifier = Modifier.size(24.dp).offset(y = (-14).dp)
            )
            // Profile Bubble
            Box(
                modifier = Modifier
                    .size(if (rank == 1) 70.dp else 56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(BorderStroke(2.dp, color), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(donor.bloodGroup, fontWeight = FontWeight.Bold, fontSize = if (rank == 1) 20.sp else 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(donor.fullName.split(" ").firstOrNull() ?: "", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${donor.totalBloodDonationCount} বার", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Rank #$rank", fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
    }
}


// --- SCREEN 5: DONOR PROFILE & CERTIFICATE SCREEN ---
@Composable
fun ProfileScreen(viewModel: BloodViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    val historyList by if (currentUser != null) {
        viewModel.getDonationHistoryForDonor(currentUser!!.mobileNumber).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<DonationHistoryEntity>()) }
    }

    var showCertificateDialog by remember { mutableStateOf(false) }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("প্রোফাইল দেখতে লগইন করুন")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.setScreen("onboarding") }) {
                    Text("লগইন পেইজ")
                }
            }
        }
        return
    }

    val user = currentUser!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper card: profile overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.bloodGroup, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("মোবাইল: ${user.mobileNumber}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("মোট রক্তদান", "${user.totalBloodDonationCount} বার")
                        StatItem("রোল (Role)", user.role)
                        StatItem("ওজন (Weight)", "${user.weight} Kg")
                    }
                }
            }
        }

        // Action: View Certificate
        item {
            Button(
                onClick = { showCertificateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Filled.CardMembership, contentDescription = "Certificate", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("রক্তদান সার্টিফিকেট জেনারেট করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // QR Code Profile ID Simulation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ডিজিটাল ডোনার আইডি কার্ড", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Verify URL: key_vbf_${user.mobileNumber}", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("এই QR কোডটি স্ক্যান করে যেকোনো ভলান্টিয়ার আপনার রক্তদানের পূর্ববর্তী ইতিহাস ও আইডি তথ্য লাইভ যাচাই করতে পারবে।", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Visual QR simulation drawing via Canvas
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White)
                            .border(BorderStroke(1.dp, Color.Black))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw nice mock QR patterns
                            val size = size.width
                            val steps = 7
                            val step = size / steps
                            for (i in 0 until steps) {
                                for (j in 0 until steps) {
                                    if ((i + j) % 2 == 0 || (i == 0 && j == 0) || (i == steps - 1 && j == 0) || (i == 0 && j == steps - 1)) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = androidx.compose.ui.geometry.Offset(i * step, j * step),
                                            size = androidx.compose.ui.geometry.Size(step, step)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed donor registration info list
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ব্যক্তিগত বিবরণ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow("পিতার নাম", user.fatherName)
                    ProfileInfoRow("মাতার নাম", user.motherName)
                    ProfileInfoRow("লিঙ্গ (Gender)", user.gender)
                    ProfileInfoRow("জন্ম তারিখ", user.dateOfBirth)
                    ProfileInfoRow("পেশা", user.occupation)
                    ProfileInfoRow("NID নম্বর", user.nationalIdNumber)
                    ProfileInfoRow("জেলা ও উপজেলা", "${user.district}, ${user.upazila}")
                    ProfileInfoRow("গ্রাম/মহল্লা", user.village)
                    ProfileInfoRow("হোয়াটসঅ্যাপ নম্বর", user.whatsAppNumber)
                    ProfileInfoRow("জরুরি যোগাযোগ", user.emergencyContactNumber)
                }
            }
        }

        // Donation History list
        item {
            Text(
                text = "রক্তদানের ইতিহাস",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Start
            )
        }

        if (historyList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো পূর্ববর্তী রক্তদানের রেকর্ড পাওয়া যায়নি।", fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(historyList) { history ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ভেরিফাইড রক্তদান", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(history.donationDate, fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🏥 হাসপাতাল: ${history.hospitalName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("👤 রোগী: ${history.patientName} (${history.bloodGroup})", fontSize = 12.sp)
                        Text("🔑 ভেরিফায়ার: ${history.verifiedBy}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        item {
            // Logout
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("লগআউট (Logout)")
            }
        }
    }

    // Auto Generated Blood Donation Certificate Dialog Box
    if (showCertificateDialog) {
        Dialog(onDismissRequest = { showCertificateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(BorderStroke(2.dp, GoldColor), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Border Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Header
                            Text(
                                "ভোটমারী ব্লাড ফাউন্ডেশন",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "রক্তদান স্বীকৃতি পত্র",
                                color = Color.DarkGray,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("এই মর্মে অত্যন্ত আনন্দের সাথে স্বীকৃতি দেওয়া যাচ্ছে যে,", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                user.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "যিনি সফলভাবে মানবতার সেবায় ও অন্যের জীবন বাঁচাতে '${user.bloodGroup}' রক্তের এক ব্যাগ রক্ত দিয়ে সাহায্য করেছেন।",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("সার্টিফিকেট নম্বর:", fontSize = 9.sp, color = Color.Gray)
                                    val safeMobile = user.mobileNumber.takeLast(5).toIntOrNull() ?: 12345
                                    Text("VBF-${100000 + safeMobile}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Simulated Sign
                                    Text("শরীফ আহমেদ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                                    Text("________________", fontSize = 8.sp, color = Color.Gray)
                                    Text("পরিচালক, ভোটমারী ব্লাড ফাউন্ডেশন", fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = {
                                viewModel.showToast("সার্টিফিকেট ডাউনলোড সফল হয়েছে (PDF Downloaded)!")
                                showCertificateDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Download")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF ডাউনলোড করুন", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { showCertificateDialog = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}


// --- SCREEN 6: EXTRA TOOLS AND DIAGNOSTIC UTILITIES ---
@Composable
fun ExtraToolsScreen(viewModel: BloodViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    var selectedTool by remember { mutableStateOf("home") } // "home", "bmi", "eligibility", "nearby", "chat"

    when (selectedTool) {
        "home" -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("ডিজিটাল কুইক টুলস ও অন্যান্য সুবিধা", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text("ভোটমারী ব্লাড ফাউন্ডেশনের বিশেষ ডিরেক্টরি ও ক্যালকুলেটরসমূহ।", fontSize = 11.sp, color = Color.Gray)
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolCard("BMI ক্যালকুলেটর", Icons.Filled.Calculate, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                            selectedTool = "bmi"
                        }
                        ToolCard("যোগ্যতা যাচাই", Icons.Filled.VerifiedUser, SuccessGreen, Modifier.weight(1f)) {
                            selectedTool = "eligibility"
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolCard("আশেপাশের ডোনার", Icons.Filled.MyLocation, PendingOrange, Modifier.weight(1f)) {
                            selectedTool = "nearby"
                        }
                        ToolCard("লাইভ চ্যাট সাহায্য", Icons.Filled.Forum, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                            selectedTool = "chat"
                        }
                    }
                }

                // SOS Button Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🚨 জরুরী সংকেত (SOS Alert Button)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("মুহূর্তেই রক্তদাতার কাছে জরুরী পুশ এলার্ট পাঠাতে ক্লিক করুন।", fontSize = 11.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))

                            var selectedSOSGroup by remember { mutableStateOf("O+") }
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                                    FilterChip(
                                        selected = selectedSOSGroup == bg,
                                        onClick = { selectedSOSGroup = bg },
                                        label = { Text(bg) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.triggerSOS(selectedSOSGroup) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.NotificationImportant, contentDescription = "SOS", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SOS জরুরী এলার্ট পাঠান", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // General Contact Board
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("যোগাযোগ ও সামাজিক মাধ্যম", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            ContactRow(Icons.Filled.Call, "পরিচালক (হটলাইন)", "01700000001")
                            ContactRow(Icons.Filled.Chat, "হোয়াটসঅ্যাপ হেল্পলাইন", "01700000001")
                            ContactRow(Icons.Filled.Email, "অফিসিয়াল জিমেইল", "contact@votmariblood.org")
                            ContactRow(Icons.Filled.Language, "ওয়েবসাইট লিংক", "www.votmariblood.org")
                        }
                    }
                }
            }
        }
        "bmi" -> {
            BMICalculatorWidget(viewModel) { selectedTool = "home" }
        }
        "eligibility" -> {
            EligibilityCheckerWidget(viewModel) { selectedTool = "home" }
        }
        "nearby" -> {
            NearbyDonorsWidget(viewModel) { selectedTool = "home" }
        }
        "chat" -> {
            LiveChatWidget(viewModel, messages) { selectedTool = "home" }
        }
    }
}

@Composable
fun ToolCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tintColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = tintColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun BMICalculatorWidget(viewModel: BloodViewModel, onBack: () -> Unit) {
    var weight by remember { mutableStateOf("65") }
    var height by remember { mutableStateOf("170") }
    var bmiResult by remember { mutableStateOf<Pair<Double, String>?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("BMI ওজন ক্যালকুলেটর", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("ওজন (কেজি)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("উচ্চতা (সেমি)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val w = weight.toDoubleOrNull() ?: 0.0
                        val h = height.toDoubleOrNull() ?: 0.0
                        bmiResult = viewModel.calculateBMI(w, h)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("হিসাব করুন")
                }
            }
        }

        bmiResult?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("আপনার বডি মাস ইনডেক্স (BMI):", fontSize = 13.sp)
                    Text(String.format("%.2f", res.first), fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                    Text(res.second, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun EligibilityCheckerWidget(viewModel: BloodViewModel, onBack: () -> Unit) {
    var age by remember { mutableStateOf("25") }
    var weight by remember { mutableStateOf("60") }
    var lastDonationMonths by remember { mutableStateOf("5") }
    var hasDisease by remember { mutableStateOf(false) }
    var eligibilityResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("রক্তদানের যোগ্যতা যাচাই", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("আপনার বয়স") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("ওজন (কেজি)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = lastDonationMonths, onValueChange = { lastDonationMonths = it }, label = { Text("সর্বশেষ রক্তদানের পর অতিবাহিত মাস (না দিলে -1 লিখুন)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasDisease, onCheckedChange = { hasDisease = it })
                    Text("আমার জটিল রোগ বা সুঁই গ্রহণের ইতিহাস আছে", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        eligibilityResult = viewModel.checkDonationEligibility(
                            age.toIntOrNull() ?: 0,
                            weight.toDoubleOrNull() ?: 0.0,
                            lastDonationMonths.toDoubleOrNull() ?: -1.0,
                            hasDisease
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("যোগ্যতা যাচাই করুন")
                }
            }
        }

        eligibilityResult?.let { res ->
            val color = if (res.first) SuccessGreen else MaterialTheme.colorScheme.primary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, color)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (res.first) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = "Status",
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(res.second, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun NearbyDonorsWidget(viewModel: BloodViewModel, onBack: () -> Unit) {
    val donorsList by viewModel.allDonors.collectAsState()
    val nearbyList = donorsList.filter { it.isApproved }.take(4)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("আশেপাশের রক্তদাতা (Nearby Donors)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("ভোটমারী ও কালীগঞ্জ এলাকার কাছাকাছি থাকা রক্তদাতাদের আনুমানিক দূরত্ব:", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(nearbyList) { idx, donor ->
                val dist = when (idx) {
                    0 -> "0.5 km (ভোটমারী রেল স্টেশন রোড)"
                    1 -> "1.2 km (ভোটমারী বাজার)"
                    2 -> "2.8 km (কালীগঞ্জ উপজেলা স্বাস্থ্য কমপ্লেক্স)"
                    else -> "4.5 km (তুষভান্ডার)"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text(donor.bloodGroup, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(donor.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsWalk, contentDescription = "Distance", modifier = Modifier.size(12.dp), tint = PendingOrange)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(dist, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveChatWidget(viewModel: BloodViewModel, messages: List<ChatMessageEntity>, onBack: () -> Unit) {
    var chatText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("ভোটমারী ব্লাড চ্যাট ও সাহায্য", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Chats container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                messages.forEach { msg ->
                    val isMe = !msg.isAdminMessage
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .widthIn(max = 240.dp),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isMe) 12.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 12.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primary else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    msg.senderName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(msg.messageText, fontSize = 12.sp, color = if (isMe) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message input bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = chatText,
                onValueChange = { chatText = it },
                label = { Text("বার্তা লিখুন...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (chatText.isNotBlank()) {
                        viewModel.sendChat(chatText)
                        val textSent = chatText
                        chatText = ""
                        // Simulated automatic volunteer reply to keep it lively
                        coroutineScope.launch {
                            delay(2000)
                            viewModel.sendChat("স্বাগতম! আপনার বার্তাটি আমরা পেয়েছি। আমাদের ভোটমারী টিমের একজন ভলান্টিয়ার অতিসত্বর যোগাযোগ করবে।")
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(24.dp), tint = Color.White)
            }
        }
    }
}


// --- SCREEN 7: ADMIN CONTROL DASHBOARD ---
@Composable
fun AdminDashboardScreen(viewModel: BloodViewModel) {
    val donorsList by viewModel.allDonors.collectAsState()
    val requestsList by viewModel.allBloodRequests.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    var activeTab by remember { mutableStateOf("Stats") } // "Stats", "Verify Donors", "NoticePublish", "CreateEvent"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.setScreen("home") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("অ্যাডমিন ড্যাশবোর্ড ($activeRole View)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Subtabs selection row
        ScrollableTabRow(
            selectedTabIndex = when (activeTab) {
                "Stats" -> 0
                "Verify Donors" -> 1
                "NoticePublish" -> 2
                else -> 3
            },
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(selected = activeTab == "Stats", onClick = { activeTab = "Stats" }, text = { Text("স্ট্যাটস") })
            Tab(selected = activeTab == "Verify Donors", onClick = { activeTab = "Verify Donors" }, text = { Text("ডোনার অনুমোদন") })
            Tab(selected = activeTab == "NoticePublish", onClick = { activeTab = "NoticePublish" }, text = { Text("নোটিশ") })
            Tab(selected = activeTab == "CreateEvent", onClick = { activeTab = "CreateEvent" }, text = { Text("ইভেন্ট যোগ") })
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeTab) {
            "Stats" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        // Total count grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard("মোট ডোনার", "${donorsList.size}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                            StatCard("মোট আবেদন", "${requestsList.size}", SuccessGreen, Modifier.weight(1f))
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val activeRequests = requestsList.filter { it.status == "Approved" }.size
                            StatCard("সক্রিয় রিকোয়েস্ট", "$activeRequests", PendingOrange, Modifier.weight(1f))
                            val completedRequests = requestsList.filter { it.status == "Completed" }.size
                            StatCard("সম্পন্ন রক্তদান", "$completedRequests", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        }
                    }

                    // Simulated Live Canvas Graph representing Blood Group Distribution
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("রক্তের গ্রুপভিত্তিক ডোনার বিন্যাস (Graph)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Simple custom canvas bar chart
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                ) {
                                    val groups = listOf("A+", "O+", "B+", "AB+", "O-", "A-", "B-")
                                    val counts = listOf(4, 5, 3, 2, 1, 2, 1)
                                    val maxVal = 6
                                    val barWidth = size.width / (groups.size * 2)
                                    val space = barWidth

                                    for (i in groups.indices) {
                                        val count = counts[i]
                                        val barHeight = (count.toFloat() / maxVal) * size.height
                                        val x = i * (barWidth + space) + space / 2
                                        val y = size.height - barHeight

                                        // Draw bar
                                        drawRect(
                                            color = Color(0xFFD32F2F),
                                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("A+", "O+", "B+", "AB+", "O-", "A-", "B-").forEach { bg ->
                                        Text(bg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Verify Donors" -> {
                val pendingDonors = donorsList.filter { !it.isApproved }
                if (pendingDonors.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("বর্তমানে কোনো অপেক্ষমান রক্তদাতা নেই।")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pendingDonors) { donor ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(donor.bloodGroup, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(donor.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("মোবাইল: ${donor.mobileNumber}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("গ্রাম: ${donor.village}, উপজেলা: ${donor.upazila}", fontSize = 12.sp)
                                    Text("NID নম্বর: ${donor.nationalIdNumber}", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { viewModel.rejectDonor(donor.mobileNumber) }) {
                                            Text("রিজেক্ট", color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = { viewModel.approveDonor(donor.mobileNumber) }) {
                                            Text("অ্যাপ্রুভ")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "NoticePublish" -> {
                var title by remember { mutableStateOf("") }
                var content by remember { mutableStateOf("") }
                var isEmergency by remember { mutableStateOf(false) }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("নোটিশের শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("নোটিশের মূল বিষয়বস্তু") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                            Text("🚨 এটি একটি জরুরী রক্ত রিকোয়েস্ট বা সতর্কবার্তা (SOS)", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (title.isBlank() || content.isBlank()) {
                                    viewModel.showToast("শিরোনাম ও বিষয়বস্তু আবশ্যক!")
                                } else {
                                    viewModel.publishNotice(title, content, isEmergency)
                                    title = ""
                                    content = ""
                                    isEmergency = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("নোটিশ পাবলিশ করুন")
                        }
                    }
                }
            }
            "CreateEvent" -> {
                var eventTitle by remember { mutableStateOf("") }
                var eventDate by remember { mutableStateOf("") }
                var eventTime by remember { mutableStateOf("") }
                var eventLoc by remember { mutableStateOf("") }
                var eventType by remember { mutableStateOf("Blood Donation Camp") }
                var eventDesc by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(value = eventTitle, onValueChange = { eventTitle = it }, label = { Text("ইভেন্টের নাম (Title)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("তারিখ (যেমন: Friday, Jul 10, 2026)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = eventTime, onValueChange = { eventTime = it }, label = { Text("সময় (যেমন: 10:00 AM)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = eventLoc, onValueChange = { eventLoc = it }, label = { Text("স্থান (Location)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("ইভেন্টের ধরন:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            val types = listOf("Blood Donation Camp", "Awareness Program", "Volunteer Meeting")
                            Column {
                                types.forEach { t ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = eventType == t, onClick = { eventType = t })
                                        Text(t)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(value = eventDesc, onValueChange = { eventDesc = it }, label = { Text("বিস্তারিত বিবরণ") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (eventTitle.isBlank() || eventDate.isBlank() || eventLoc.isBlank()) {
                                        viewModel.showToast("অনুগ্রহ করে সকল আবশ্যকীয় ঘর পূরণ করুন!")
                                    } else {
                                        viewModel.createEvent(eventTitle, eventDate, eventTime, eventLoc, eventType, eventDesc)
                                        eventTitle = ""
                                        eventDate = ""
                                        eventTime = ""
                                        eventLoc = ""
                                        eventDesc = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ইভেন্ট তৈরি করুন")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, tintColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = tintColor)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
