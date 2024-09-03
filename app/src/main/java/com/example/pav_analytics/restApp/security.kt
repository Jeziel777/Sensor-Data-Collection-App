package com.example.pav_analytics.restApp

import android.content.Context
import com.example.pav_analytics.R
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun getSecureOkHttpClient(context: Context): OkHttpClient {
    // Create a CertificateFactory for generating certificate instances
    val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X.509")

    // Open an input stream to your .pem file from the raw resources
    val inputStream: InputStream = context.resources.openRawResource(R.raw.cert)

    // Generate an X509Certificate from the input stream
    val ca: X509Certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate

    // Close the input stream as it's no longer needed
    inputStream.close()

    // Get the default KeyStore type and create a new KeyStore instance
    val keyStoreType: String = KeyStore.getDefaultType()
    val keyStore: KeyStore = KeyStore.getInstance(keyStoreType).apply {
        load(null, null) // Load the KeyStore with null parameters
    }

    // Set the certificate entry in the KeyStore with an alias "ca"
    keyStore.setCertificateEntry("ca", ca)

    // Initialize a TrustManagerFactory with the default algorithm
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())

    // Initialize the TrustManagerFactory with the KeyStore
    trustManagerFactory.init(keyStore)

    // Retrieve the TrustManagers from the factory
    val trustManagers = trustManagerFactory.trustManagers

    // Get the first TrustManager and cast it to X509TrustManager
    val trustManager = trustManagers[0] as X509TrustManager

    // Create an SSLContext with the TLS protocol
    val sslContext: SSLContext = SSLContext.getInstance("TLS")

    // Initialize the SSLContext with null key managers and the trust manager from the factory
    sslContext.init(null, arrayOf(trustManager), null)

    // Build and return an OkHttpClient instance configured with the SSLContext and trust manager
    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .build()
}

