package com.heril.talkfusion.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.heril.talkfusion.R
import com.heril.talkfusion.data.UserData
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel

@Composable
fun editBioScreen(
    userData: UserData,
    firebaseViewModel: FirebaseViewModel,
    navController: NavController
){
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            IconButton(
                onClick = {
                    navController.navigateUp()
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.backicon),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "Bio",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 35.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            IconButton(
                onClick = {
                    firebaseViewModel.addBio()
                    Toast.makeText(
                        ctx,
                        "Bio Updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigateUp()
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(20.dp))
        TextField(
            value = firebaseViewModel.Bio,
            onValueChange = {
                    newBio -> firebaseViewModel.Bio = newBio
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "You can add a few lines about yourself",
            color = Color.Gray
        )
    }
}