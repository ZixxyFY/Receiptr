package com.receiptr.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class NotificationData(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionType: NotificationAction? = null
) : Parcelable

enum class NotificationType(val channelId: String, val channelName: String, val importance: Int) {
    SUCCESSFUL_SCAN("successful_scan", "Receipt Scanned", 3),
    EMAIL_RECEIPT("email_receipt", "Email Receipts", 2),
    PDF_EXPORT("pdf_export", "PDF Export", 2),
    WEEKLY_SUMMARY("weekly_summary", "Weekly Reports", 2),
    REMINDER_SCAN("reminder_scan", "Scan Reminders", 1),
    BUDGET_ALERT("budget_alert", "Budget Alerts", 4),
    LARGE_PURCHASE("large_purchase", "Large Purchases", 3)
}

enum class NotificationAction(val action: String) {
    OPEN_RECEIPT("open_receipt"),
    CATEGORIZE_RECEIPT("categorize_receipt"),
    SHARE_PDF("share_pdf"),
    VIEW_ANALYTICS("view_analytics"),
    SCAN_RECEIPT("scan_receipt"),
    VIEW_BUDGET("view_budget"),
    VIEW_RECEIPT_DETAIL("view_receipt_detail")
}

data class NotificationTemplate(
    val type: NotificationType,
    val titleTemplate: String,
    val bodyTemplate: String,
    val action: NotificationAction? = null
) {
    companion object {
        val templates = mapOf(
            NotificationType.SUCCESSFUL_SCAN to NotificationTemplate(
                type = NotificationType.SUCCESSFUL_SCAN,
                titleTemplate = "Receipt Saved! ✅",
                bodyTemplate = "Your receipt from {merchant} for {amount} has been successfully processed.",
                action = NotificationAction.OPEN_RECEIPT
            ),
            NotificationType.EMAIL_RECEIPT to NotificationTemplate(
                type = NotificationType.EMAIL_RECEIPT,
                titleTemplate = "New Auto-Receipt Added 📧",
                bodyTemplate = "We've imported your receipt from {merchant}. Tap to categorize it.",
                action = NotificationAction.CATEGORIZE_RECEIPT
            ),
            NotificationType.PDF_EXPORT to NotificationTemplate(
                type = NotificationType.PDF_EXPORT,
                titleTemplate = "Your Export is Ready 📄",
                bodyTemplate = "Your PDF receipt has been generated. Tap to share or save it.",
                action = NotificationAction.SHARE_PDF
            ),
            NotificationType.WEEKLY_SUMMARY to NotificationTemplate(
                type = NotificationType.WEEKLY_SUMMARY,
                titleTemplate = "Your Weekly Report is Ready 📊",
                bodyTemplate = "You spent {total_amount} this week. Tap to see your full breakdown.",
                action = NotificationAction.VIEW_ANALYTICS
            ),
            NotificationType.REMINDER_SCAN to NotificationTemplate(
                type = NotificationType.REMINDER_SCAN,
                titleTemplate = "Don't Forget Your Receipts! 🧾",
                bodyTemplate = "Stay on top of your spending. Take a moment to scan any new receipts.",
                action = NotificationAction.SCAN_RECEIPT
            ),
            NotificationType.BUDGET_ALERT to NotificationTemplate(
                type = NotificationType.BUDGET_ALERT,
                titleTemplate = "Budget Alert: {category} ⚠️",
                bodyTemplate = "You're close to your limit! You've spent {amount_spent} of your {total_budget}.",
                action = NotificationAction.VIEW_BUDGET
            ),
            NotificationType.LARGE_PURCHASE to NotificationTemplate(
                type = NotificationType.LARGE_PURCHASE,
                titleTemplate = "Large Expense Added 💰",
                bodyTemplate = "A new purchase of {amount} at {merchant} was just recorded.",
                action = NotificationAction.VIEW_RECEIPT_DETAIL
            )
        )
    }
}
