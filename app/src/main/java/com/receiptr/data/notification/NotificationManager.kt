package com.receiptr.data.notification

import com.receiptr.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val notificationService: NotificationService
) {

    fun sendReceiptScannedNotification(receipt: Receipt) {
        val template = NotificationTemplate.templates[NotificationType.SUCCESSFUL_SCAN] ?: return
        val replacements = mapOf(
            "merchant" to receipt.merchantName.ifEmpty { "Unknown Store" },
            "amount" to "${receipt.currency} ${String.format("%.2f", receipt.totalAmount)}"
        )
        
        val notificationData = createNotificationFromTemplate(
            template = template,
            replacements = replacements,
            data = mapOf("receipt_id" to receipt.id)
        )
        
        notificationService.sendNotification(notificationData)
    }

    fun sendEmailReceiptNotification(receipt: Receipt) {
        val template = NotificationTemplate.templates[NotificationType.EMAIL_RECEIPT] ?: return
        val replacements = mapOf(
            "merchant" to receipt.merchantName.ifEmpty { "Unknown Store" }
        )
        
        val notificationData = createNotificationFromTemplate(
            template = template,
            replacements = replacements,
            data = mapOf("receipt_id" to receipt.id)
        )
        
        notificationService.sendNotification(notificationData)
    }

    fun sendPdfExportNotification(receiptId: String, pdfPath: String) {
        val template = NotificationTemplate.templates[NotificationType.PDF_EXPORT] ?: return
        
        val notificationData = createNotificationFromTemplate(
            template = template,
            replacements = emptyMap(),
            data = mapOf(
                "receipt_id" to receiptId,
                "pdf_path" to pdfPath
            )
        )
        
        notificationService.sendNotification(notificationData)
    }

    fun sendWeeklySummaryNotification(totalAmount: Double, currency: String = "USD") {
        val template = NotificationTemplate.templates[NotificationType.WEEKLY_SUMMARY] ?: return
        val replacements = mapOf(
            "total_amount" to "$currency ${String.format("%.2f", totalAmount)}"
        )
        
        val notificationData = createNotificationFromTemplate(
            template = template,
            replacements = replacements,
            data = mapOf("total_amount" to totalAmount.toString())
        )
        
        notificationService.sendNotification(notificationData)
    }

    fun sendReminderScanNotification() {
        val template = NotificationTemplate.templates[NotificationType.REMINDER_SCAN] ?: return
        
        val notificationData = createNotificationFromTemplate(
            template = template,
            replacements = emptyMap(),
            data = emptyMap()
        )
        
        notificationService.sendNotification(notificationData)
    }

    fun sendBudgetAlertNotification(
        category: String,
        amountSpent: Double,
        totalBudget: Double,
        currency: String = "USD"
    ) {
        val template = NotificationTemplate.templates[NotificationType.BUDGET_ALERT] ?: return
        val replacements = mapOf(
            "category" to category,
            "amount_spent" to "$currency ${String.format("%.2f", amountSpent)}",
            "total_budget" to "$currency ${String.format("%.2f", totalBudget)}"
        )
        
        val notificationData = createNotificationFromTemplate(
            template = template,
            replacements = replacements,
            data = mapOf(
                "category" to category,
                "amount_spent" to amountSpent.toString(),
                "total_budget" to totalBudget.toString()
            )
        )
        
        notificationService.sendNotification(notificationData)
    }

    fun sendLargePurchaseNotification(receipt: Receipt, threshold: Double = 100.0) {
        if (receipt.totalAmount >= threshold) {
            val template = NotificationTemplate.templates[NotificationType.LARGE_PURCHASE] ?: return
            val replacements = mapOf(
                "amount" to "${receipt.currency} ${String.format("%.2f", receipt.totalAmount)}",
                "merchant" to receipt.merchantName.ifEmpty { "Unknown Store" }
            )
            
            val notificationData = createNotificationFromTemplate(
                template = template,
                replacements = replacements,
                data = mapOf(
                    "receipt_id" to receipt.id,
                    "amount" to receipt.totalAmount.toString()
                )
            )
            
            notificationService.sendNotification(notificationData)
        }
    }

    private fun createNotificationFromTemplate(
        template: NotificationTemplate,
        replacements: Map<String, String>,
        data: Map<String, String>
    ): NotificationData {
        val title = replacements.entries.fold(template.titleTemplate) { acc, (key, value) ->
            acc.replace("{$key}", value)
        }
        
        val body = replacements.entries.fold(template.bodyTemplate) { acc, (key, value) ->
            acc.replace("{$key}", value)
        }
        
        return NotificationData(
            type = template.type,
            title = title,
            body = body,
            data = data,
            actionType = template.action
        )
    }
}
