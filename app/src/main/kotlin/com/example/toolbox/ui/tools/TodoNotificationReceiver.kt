package com.example.toolbox.ui.tools

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.toolbox.MainActivity
import com.example.toolbox.R

/**
 * Helper for scheduling exact/window alarms and posting system heads-up notifications
 * when a scheduled [TodoItem] reaches its scheduled date/time.
 */
object TodoNotificationHelper {

  const val CHANNEL_ID = "todo_schedule_channel"
  const val CHANNEL_NAME = "Scheduled To-Do Notes"
  const val ACTION_TODO_SCHEDULE_ALARM = "com.example.toolbox.ACTION_TODO_SCHEDULE_ALARM"
  const val EXTRA_TASK_ID = "extra_todo_task_id"
  const val EXTRA_TASK_TITLE = "extra_todo_task_title"
  const val EXTRA_TASK_CONTENT = "extra_todo_task_content"
  const val EXTRA_SCHEDULED_AT = "extra_todo_scheduled_at"

  fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        ?: return
      val existing = manager.getNotificationChannel(CHANNEL_ID)
      if (existing == null) {
        val channel = NotificationChannel(
          CHANNEL_ID,
          CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Notifications for scheduled To-Do tasks and notes"
          enableVibration(true)
        }
        manager.createNotificationChannel(channel)
      }
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  /**
   * Displays a high-priority heads-up notification with the note's title and contents.
   */
  fun showTaskNotification(
    context: Context,
    task: TodoItem,
  ) {
    val contentBody = task.buildNotificationContent()
    val scheduleSummary = task.scheduledAtMillis?.let {
      TodoListEngine.formatScheduleLabel(it, task.scheduledEndMillis)
    }
    showNotification(
      context = context,
      notificationId = task.id.hashCode(),
      title = task.title,
      content = contentBody,
      summaryText = scheduleSummary,
    )
  }

  fun showNotification(
    context: Context,
    notificationId: Int,
    title: String,
    content: String,
    summaryText: String? = null,
  ) {
    ensureNotificationChannel(context)
    if (!hasNotificationPermission(context)) return

    val launchIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      launchIntent,
      pendingIntentFlags,
    )

    val bigTextStyle = NotificationCompat.BigTextStyle()
      .setBigContentTitle(title)
      .bigText(content)
    if (!summaryText.isNullOrBlank()) {
      bigTextStyle.setSummaryText(summaryText)
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_tool_todo_list_foreground)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(bigTextStyle)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_REMINDER)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)
      .build()

    try {
      NotificationManagerCompat.from(context).notify(notificationId, notification)
    } catch (_: SecurityException) {
      // Permission might have been revoked
    }
  }

  /**
   * Schedules an alarm via [AlarmManager] for a single scheduled [TodoItem].
   */
  fun scheduleTaskAlarm(context: Context, task: TodoItem) {
    val scheduledAt = task.scheduledAtMillis ?: return
    if (task.notifiedAtScheduleMillis == scheduledAt) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, TodoAlarmReceiver::class.java).apply {
      action = ACTION_TODO_SCHEDULE_ALARM
      putExtra(EXTRA_TASK_ID, task.id)
      putExtra(EXTRA_TASK_TITLE, task.title)
      putExtra(EXTRA_TASK_CONTENT, task.buildNotificationContent())
      putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      task.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val triggerAt = maxOf(System.currentTimeMillis() + 500L, scheduledAt)
    try {
      val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
      } else {
        true
      }
      if (canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      }
    } catch (_: SecurityException) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
      }
    }
  }

  /**
   * Cancels any scheduled alarm for [taskId].
   */
  fun cancelTaskAlarm(context: Context, taskId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, TodoAlarmReceiver::class.java).apply {
      action = ACTION_TODO_SCHEDULE_ALARM
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      taskId.hashCode(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  /**
   * Synchronizes alarms for the current active task list.
   */
  fun syncTaskAlarms(context: Context, tasks: List<TodoItem>) {
    ensureNotificationChannel(context)
    tasks.forEach { task ->
      if (task.scheduledAtMillis != null && task.notifiedAtScheduleMillis != task.scheduledAtMillis) {
        scheduleTaskAlarm(context, task)
      } else {
        cancelTaskAlarm(context, task.id)
      }
    }
  }
}

/**
 * BroadcastReceiver that fires when a scheduled [TodoItem] alarm reaches its date/time,
 * or reschedules pending alarms on device boot.
 */
class TodoAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    val repo = SharedPreferencesTodoListRepository(context)

    if (action == Intent.ACTION_BOOT_COMPLETED) {
      val tasks = repo.loadTasks()
      TodoNotificationHelper.syncTaskAlarms(context, tasks)
      return
    }

    if (action == TodoNotificationHelper.ACTION_TODO_SCHEDULE_ALARM) {
      val taskId = intent.getStringExtra(TodoNotificationHelper.EXTRA_TASK_ID)
      val tasks = repo.loadTasks()
      val matchingTask = tasks.firstOrNull { it.id == taskId }

      if (matchingTask != null) {
        val scheduledAt = matchingTask.scheduledAtMillis
        if (scheduledAt != null && matchingTask.notifiedAtScheduleMillis != scheduledAt) {
          TodoNotificationHelper.showTaskNotification(context, matchingTask)
          val updated = TodoListEngine.markTaskNotified(tasks, matchingTask.id, scheduledAt)
          repo.saveTasks(updated)
        }
      } else if (taskId != null) {
        // Fallback if task was only in intent extras
        val fallbackTitle = intent.getStringExtra(TodoNotificationHelper.EXTRA_TASK_TITLE) ?: return
        val fallbackContent = intent.getStringExtra(TodoNotificationHelper.EXTRA_TASK_CONTENT) ?: fallbackTitle
        TodoNotificationHelper.showNotification(
          context = context,
          notificationId = taskId.hashCode(),
          title = fallbackTitle,
          content = fallbackContent,
        )
      }
    }
  }
}
