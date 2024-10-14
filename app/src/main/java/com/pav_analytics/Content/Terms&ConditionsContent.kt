package com.pav_analytics.Content

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color

@Composable
fun TermsTextContent(color: Color) {
    val annotatedText = buildAnnotatedString {
        append("\n")
        append("By accepting our Privacy Policy and Terms of Use, you agree to provide data, including videos, ")
        append("images, and sensor readings such as accelerometer, gyro, and magnetometer, along with your exact GPS location. ")
        append("This data will be used to gather information about the condition of cyclist pavements ")
        append("and other related active routes.\n\n")

        append("In compliance with GDPR, you acknowledge the following:\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("1. Recording Private Information: ")
        }
        append("We implement privacy-by-design principles. Your camera will only capture " +
                "the road and not private individuals. Audio recording will be " +
                "disabled as it is not required for this project.\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("2. Legal Basis for Data Processing: ")
        }
        append("Data collection is based on public interest grounds, ensuring that it is necessary " +
                "and proportionate for improving road infrastructure (Article 6).\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("3. Transparency: ")
        }
        append("You acknowledge that you are informed about data collection through visible notices and electronic privacy statements. " +
                "These explain the purpose of data collection, the methods used for processing, and your rights as a user " +
                "(Article 13(1), Article 29).\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("4. Data Minimization: ")
        }
        append("Only the data necessary for the project is captured. Your camera will be turned off " +
                "when not required. Data Protection Impact Assessments are conducted to identify and mitigate risks " +
                "(Article 35(1)).\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("5. Secure Data Storage: ")
        }
        append("All data you provide is securely stored, with access limited to authorized personnel. " +
                "Encryption and other security measures ensure that your data is protected from unauthorized access, " +
                "and it will only be retained for the minimum period necessary (Article 5(1)(c) and (e)).\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("6. Respecting Individual Rights: ")
        }
        append("You have the right to access, correct, or erase your personal data. Any requests regarding your data will be handled " +
                "in compliance with GDPR and EDPB requirements (Article 5(d), Article 17). If any third-party individuals are captured " +
                "in your recordings, they will be pixelated to protect their privacy.\n\n")

        append("Please ensure that you read the instructions in the app for guidance on setting your camera angle, and learn about any " +
                "restrictions that may apply when uploading videos to the platform.\n\n\n")
    }

    // Display the formatted text
    Text(text = annotatedText, color = color)
}