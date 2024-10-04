package com.heril.talkfusion.utils

import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel

object FirebaseViewModelProvider {
    val instance: FirebaseViewModel by lazy {
        FirebaseViewModel()
    }
}
