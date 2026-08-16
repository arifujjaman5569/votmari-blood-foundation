package com.votmari.bloodfoundation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.votmari.bloodfoundation.data.DonorEntity

@Composable
fun EditProfileScreen(
    user: DonorEntity,
    onSave: (DonorEntity) -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var fatherName by remember { mutableStateOf(user.fatherName) }
    var motherName by remember { mutableStateOf(user.motherName) }
    var whatsAppNumber by remember { mutableStateOf(user.whatsAppNumber) }
    var dateOfBirth by remember { mutableStateOf(user.dateOfBirth) }
    var gender by remember { mutableStateOf(user.gender) }
    var occupation by remember { mutableStateOf(user.occupation) }
    var nationalIdNumber by remember { mutableStateOf(user.nationalIdNumber) }
    var address by remember { mutableStateOf(user.address) }
    var division by remember { mutableStateOf(user.division) }
    var district by remember { mutableStateOf(user.district) }
    var upazila by remember { mutableStateOf(user.upazila) }
    var village by remember { mutableStateOf(user.village) }
    var weight by remember { mutableStateOf(user.weight.toString()) }
    var emergencyContactNumber by remember {
        mutableStateOf(user.emergencyContactNumber)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "প্রোফাইল সম্পাদনা",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("পূর্ণ নাম") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fatherName,
            onValueChange = { fatherName = it },
            label = { Text("পিতার নাম") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = motherName,
            onValueChange = { motherName = it },
            label = { Text("মাতার নাম") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = whatsAppNumber,
            onValueChange = { whatsAppNumber = it },
            label = { Text("হোয়াটসঅ্যাপ নম্বর") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            label = { Text("জন্ম তারিখ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("লিঙ্গ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("পেশা") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nationalIdNumber,
            onValueChange = { nationalIdNumber = it },
            label = { Text("NID নম্বর") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("ঠিকানা") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = division,
            onValueChange = { division = it },
            label = { Text("বিভাগ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = district,
            onValueChange = { district = it },
            label = { Text("জেলা") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = upazila,
            onValueChange = { upazila = it },
            label = { Text("উপজেলা") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = village,
            onValueChange = { village = it },
            label = { Text("গ্রাম/মহল্লা") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("ওজন (Kg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = emergencyContactNumber,
            onValueChange = { emergencyContactNumber = it },
            label = { Text("জরুরি যোগাযোগ নম্বর") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val updatedWeight = weight.toDoubleOrNull() ?: user.weight

                onSave(
                    user.copy(
                        fullName = fullName,
                        fatherName = fatherName,
                        motherName = motherName,
                        whatsAppNumber = whatsAppNumber,
                        dateOfBirth = dateOfBirth,
                        gender = gender,
                        occupation = occupation,
                        nationalIdNumber = nationalIdNumber,
                        address = address,
                        division = division,
                        district = district,
                        upazila = upazila,
                        village = village,
                        weight = updatedWeight,
                        emergencyContactNumber = emergencyContactNumber
                    )
                )
            }
        ) {
            Text("সংরক্ষণ করুন")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("ফিরে যান")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
