package com.votmari.bloodfoundation.ui.screens.profile

import androidx.compose.foundation.layout.*
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
    var occupation by remember { mutableStateOf(user.occupation) }
    var address by remember { mutableStateOf(user.address) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("পেশা") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("ঠিকানা") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onSave(
                    user.copy(
                        fullName = fullName,
                        occupation = occupation,
                        address = address
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
    }
}
