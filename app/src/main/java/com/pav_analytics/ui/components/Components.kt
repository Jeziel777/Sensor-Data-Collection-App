package com.pav_analytics.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pav_analytics.R

import com.pav_analytics.ui.theme.GrayColor
import com.pav_analytics.ui.theme.PrimaryColor
import com.pav_analytics.ui.theme.SecondaryColor

@Composable
fun UnderlinedTextComponent(value: String, onClick: () -> Unit, color: Color) {
    Text(
        text = value,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 12.dp)
            .clickable { onClick() },
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline
    )
}

@Composable
fun NormalTextComponent(value: String, color: Color) {
    Text(
        text = value,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun TermsTextComponent(value: String, color: Color) {
    Text(
        text = value,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Justify
    )
}

@Composable
fun HeadingTextComponent(value: String, color: Color) {
    Text(
        text = value,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ClickableTextComponent(value: String, onTextSelected: (String) -> Unit, textColor: Color) {
    val text1 = "By continuing you accept our "
    val privacyPolicyText = "Privacy Policy "
    val text2 = "and "
    val termsAndConditionsText = "Terms of Use"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = textColor)) {
            append(text1)
        }
        withStyle(style = SpanStyle(color = PrimaryColor)) {
            pushStringAnnotation(tag = privacyPolicyText, annotation = privacyPolicyText)
            append(privacyPolicyText)
        }
        withStyle(style = SpanStyle(color = textColor)) {
            append(text2)
        }
        withStyle(style = SpanStyle(color = PrimaryColor)) {
            pushStringAnnotation(tag = termsAndConditionsText, annotation = termsAndConditionsText)
            append(termsAndConditionsText)
        }
    }
    ClickableText(text = annotatedString, onClick = { offset ->
        annotatedString.getStringAnnotations(offset, offset)
            .firstOrNull()?.also { span ->
                Log.d("ClickableTextComponent", "{${span.item}")

                if (span.item == termsAndConditionsText || span.item == privacyPolicyText) {
                    onTextSelected(span.item)
                }
            }
    })
}

@Composable
fun ClickableLoginTextComponent(
    tryingToLogin: Boolean = true,
    onTextSelected: (String) -> Unit,
    textColor: Color
) {
    val text1 = if (tryingToLogin) "Already have an account? " else "Don't have an account yet? "
    val loginText = if (tryingToLogin) "Login" else "Register"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = textColor)) {
            append(text1)
        }
        withStyle(style = SpanStyle(color = PrimaryColor)) {
            pushStringAnnotation(tag = loginText, annotation = loginText)
            append(loginText)
        }
    }

    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp),
        style = TextStyle(
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center
        ),
        text = annotatedString, onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset)
                .firstOrNull()?.also { span ->
                    Log.d("ClickableTextComponent", "{${span.item}")

                    if (span.item == loginText) {
                        onTextSelected(span.item)
                    }
                }
        })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextFieldComponent(labelValue: String, iconImage: ImageVector) {
    // Using remember and mutableStateOf to create a state for textValue
    val textValue = remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        label = { Text(text = labelValue) },  // Using a composable for label
        value = textValue.value,  // Accessing the value from the MutableState
        onValueChange = { textValue.value = it },  // Updating the value in the MutableState
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            cursorColor = PrimaryColor,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        singleLine = true,
        maxLines = 1,
        leadingIcon = {
            Icon(
                imageVector = iconImage, // Replace with desired icon
                contentDescription = "Email Icon"
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordFieldComponent(labelValue: String, iconImage: ImageVector) {
    val localFocusManager = LocalFocusManager.current

    // Using remember and mutableStateOf to create a state for textValue
    val textValue = remember { mutableStateOf("") }

    val passwordVisible = remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        label = { Text(text = labelValue) },  // Using a composable for label
        value = textValue.value,  // Accessing the value from the MutableState
        onValueChange = { textValue.value = it },  // Updating the value in the MutableState
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            cursorColor = PrimaryColor,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        keyboardActions = KeyboardActions {
            localFocusManager.clearFocus()
        },
        maxLines = 1,
        leadingIcon = {
            Icon(
                imageVector = iconImage, // Replace with desired icon
                contentDescription = "Email Icon"
            )
        },
        trailingIcon = {
            val visibilityIcon = if (passwordVisible.value) {
                Icons.Filled.Visibility
            } else {
                Icons.Filled.VisibilityOff
            }
            val description = if (passwordVisible.value) {
                "Hide Password"
            } else {
                "Show Password"
            }
            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                Icon(imageVector = visibilityIcon, contentDescription = description)
            }
        },
        visualTransformation = if (passwordVisible.value) VisualTransformation.None
        else PasswordVisualTransformation()

    )
}

@Composable
fun CheckBoxComponent(
    value: String,
    onCheckedChange: (Boolean) -> Unit,
    onTextSelected: (String) -> Unit,
    textColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val checkState = remember {
            mutableStateOf(false)
        }
        Checkbox(
            checked = checkState.value,
            onCheckedChange = {
                checkState.value = it
                onCheckedChange(it)
            }
        )
        ClickableTextComponent(
            value = value,
            onTextSelected, textColor = textColor
        )
    }
}

@Composable
fun ButtonComponent(value: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(48.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(SecondaryColor, PrimaryColor)),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RegisterButtonComponent(value: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(48.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(SecondaryColor, PrimaryColor)),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DividerTextComponent(color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = color,
            thickness = 1.dp
        )

        Text(
            modifier = Modifier.padding(8.dp),
            text = "or",
            fontSize = 18.sp,
            color = color
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = color,
            thickness = 1.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextFieldComponent(
    labelValue: String,
    iconImage: ImageVector,
    isPasswordField: Boolean = false,
    onValueChange: (String) -> Unit,
    textValue: String,
    textColor: Color,
    labelColor: Color,
    iconTint: Color
) {
    val localFocusManager = LocalFocusManager.current
    val passwordVisible = remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        label = { Text(text = labelValue) },
        value = textValue,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            cursorColor = PrimaryColor,
        ),
        keyboardOptions = if (isPasswordField) KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ) else KeyboardOptions.Default,
        keyboardActions = if (isPasswordField) KeyboardActions {
            localFocusManager.clearFocus()
        } else KeyboardActions.Default,
        singleLine = true,
        maxLines = 1,
        leadingIcon = {
            Icon(
                imageVector = iconImage,
                contentDescription = null
            )
        },
        trailingIcon = if (isPasswordField) {
            {
                val visibilityIcon = if (passwordVisible.value) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }
                val description = if (passwordVisible.value) {
                    "Hide Password"
                } else {
                    "Show Password"
                }
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            }
        } else null,
        visualTransformation = if (isPasswordField && !passwordVisible.value) PasswordVisualTransformation()
        else VisualTransformation.None
    )
}

@Composable
fun TitleCard(title: String, backgroundColor: Color, textColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp), // Rounded corners at the bottom
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Align content
        ) {
            // Add the logo image, similar to HomeTitleCard
            Image(
                painter = painterResource(id = R.mipmap.pa_logo_foreground),
                contentDescription = null, // Provide description if necessary
                modifier = Modifier
                    .size(60.dp) // Adjust size as needed
                    .clip(CircleShape) // Circular shape for the logo
            )

            // Spacer to create empty space between the logo and the title
            Spacer(modifier = Modifier.weight(.5f))

            // Center the text in the remaining space
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                modifier = Modifier.padding(start = 16.dp) // Padding to separate from the logo
            )

            // Spacer to balance the layout (since the text is centered)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@Composable
fun HomeTitleCard(title: String, backgroundColor: Color, onLogoutClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp), // Rounded corners at the bottom
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // To place items at opposite ends
        ) {
            Image(
                painter = painterResource(id = R.mipmap.pa_logo_foreground),
                contentDescription = null, // You can provide a description here if needed
                modifier = Modifier
                    .size(60.dp) // Adjust size as needed
                    .clip(CircleShape) // Makes the image circular
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(GrayColor)
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = 0.dp,
                        vertical = 0.dp
                    ) // Adjust padding here
                )
            }
        }
    }
}

@Composable
fun FileTitleCard(
    title: String,
    backgroundColor: Color,
    textColor: Color,
    onMediaImported: () -> Unit,
    selectMediaLauncher: ActivityResultLauncher<Array<String>> // Pass the launcher
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp), // Rounded corners at the bottom
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Align content
        ) {
            // Add the logo image, similar to HomeTitleCard
            Image(
                painter = painterResource(id = R.mipmap.pa_logo_foreground),
                contentDescription = null, // Provide description if necessary
                modifier = Modifier
                    .size(60.dp) // Adjust size as needed
                    .clip(CircleShape) // Circular shape for the logo
            )

            // Spacer to create empty space between the logo and the title
            Spacer(modifier = Modifier.weight(.75f))

            // Center the text in the remaining space
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                modifier = Modifier.padding(start = 16.dp) // Padding to separate from the logo
            )

            // Spacer to balance the layout (since the text is centered)
            Spacer(modifier = Modifier.weight(1f))

            // Add an IconButton to the right with a default icon
            IconButton(
                onClick = {
                    // Launch the media picker for images and videos
                    selectMediaLauncher.launch(arrayOf("video/*"))
                    onMediaImported()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Upload, // Example icon
                    contentDescription = "Select Media",
                    tint = textColor,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}


// Handle the result in the activity
fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
    onMediaImported: (Uri?) -> Unit
) {
    if (requestCode == REQUEST_CODE_PICK_MEDIA && resultCode == Activity.RESULT_OK) {
        val uri: Uri? = data?.data
        onMediaImported(uri) // Return the selected media URI
    }
}

const val REQUEST_CODE_PICK_MEDIA = 100