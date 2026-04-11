package com.viso.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BillAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val billId = intent.getStringExtra("bill_id") ?: return
        val billName = intent.getStringExtra("bill_name") ?: return
        val dueDay = intent.getIntExtra("due_day", 0)
        val amountCents = intent.getLongExtra("amount_cents", 0L)

        val helper = NotificationHelper(context)
        helper.createChannel()
        helper.showBillNotification(billId, billName, dueDay, amountCents)
    }
}
