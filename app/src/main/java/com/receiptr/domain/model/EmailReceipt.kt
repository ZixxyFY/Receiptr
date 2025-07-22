package com.receiptr.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmailReceipt(
    val id: String,
    val emailId: String,
    val from: String,
    val subject: String,
    val body: String,
    val receivedDate: Long,
    val isProcessed: Boolean = false,
    val extractedData: ExtractedReceiptData? = null,
    val confidence: Float = 0.0f,
    val attachments: List<EmailAttachment> = emptyList()
) : Parcelable

@Parcelize
data class ExtractedReceiptData(
    val merchantName: String = "",
    val totalAmount: Double = 0.0,
    val currency: String = "USD",
    val transactionDate: Long = 0L,
    val category: String = "",
    val items: List<EmailReceiptItem> = emptyList(),
    val paymentMethod: String = "",
    val transactionId: String = ""
) : Parcelable

@Parcelize
data class EmailReceiptItem(
    val name: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
) : Parcelable

@Parcelize
data class EmailAttachment(
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val data: ByteArray? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmailAttachment

        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (size != other.size) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}

enum class EmailProvider(val domain: String, val displayName: String) {
    GMAIL("gmail.com", "Gmail"),
    OUTLOOK("outlook.com", "Outlook"),
    YAHOO("yahoo.com", "Yahoo Mail"),
    APPLE("icloud.com", "iCloud Mail"),
    OTHER("", "Other")
}

data class EmailAuthConfig(
    val provider: EmailProvider,
    val clientId: String,
    val clientSecret: String,
    val scopes: List<String>
)
