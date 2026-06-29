package com.pv.transport.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.ui.theme.appFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPickGalleryClick: (() -> Unit)? = null // Gallery ကိုပါ သုံးချင်ရင် သုံးလို့ရအောင် Optional ပေးထားတာပါ
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ၁။ ကင်မရာ ရိုက်မည့်ခလုတ်
                PickerItem(text = "📷 Take Photo") {
                    onTakePhotoClick()
                }

                // ၂။ Gallery ခလုတ် (အကယ်၍ Parameter ပါးလိုက်မှသာ UI မှာ ပေါ်လာမှာဖြစ်ပါတယ်)
                if (onPickGalleryClick != null) {
                    PickerItem(text = "🖼 Pick from Gallery") {
                        onPickGalleryClick()
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 16.sp, fontFamily = appFontFamily )
    }
}