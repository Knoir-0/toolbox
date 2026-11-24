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
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.toolbox.R
import com.example.toolbox.ui.main.AppPermission
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createStandaloneToolIntent
import com.example.toolbox.ui.main.createToolShortcutBitmap
import com.example.toolbox.ui.main.resolveShortcutBackgroundColor
import com.example.toolbox.ui.main.resolveShortcutForegroundColor
import java.util.Collections

/**
 * Helper for scheduling exact/window alarms and posting system heads-up notifications
 * when a scheduled [TodoItem] reaches its scheduled date/time.
 */
object TodoNotificationHelper {

  const val CHANNEL_ID = "todo_schedule_channel"
  const val CHANNEL_NAME = "Scheduled To-Do Notes"
  const val TODO_NOTIFICATION_SHORTCUT_ID = "shortcut_notification_todo_list"
  const val ACTION_TODO_SCHEDULE_ALARM = "com.example.toolbox.ACTION_TODO_SCHEDULE_ALARM"
  const val EXTRA_TASK_ID = "extra_todo_task_id"
  const val EXTRA_TASK_TITLE = "extra_todo_task_title"
  const val EXTRA_TASK_CONTENT = "extra_todo_task_content"
  const val EXTRA_SCHEDULED_AT = "extra_todo_scheduled_at"

  private val scheduledAlarmTaskIds: MutableSet<String> =
    Collections.synchronizedSet(mutableSetOf())

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
    if (!NavigationPreferencesBridge.isPermissionGranted(AppPermission.POST_NOTIFICATIONS)) {
      return false
    }
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

    val launchIntent = createStandaloneToolIntent(context, Tool.TODO_LIST)
    val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      launchIntent,
      pendingIntentFlags,
    )

    val backgroundColor = resolveShortcutBackgroundColor(context = context)
    val foregroundColor = resolveShortcutForegroundColor(backgroundColor)
    val todoIconBitmap = try {
      createToolShortcutBitmap(
        imageVector = Tool.TODO_LIST.icon,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
      )
    } catch (_: Exception) {
      null
    }

    val bigTextStyle = NotificationCompat.BigTextStyle()
      .setBigContentTitle(title)
      .bigText(content)
    if (!summaryText.isNullOrBlank()) {
      bigTextStyle.setSummaryText(summaryText)
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_notification_todo_list)
      .setColor(backgroundColor.toArgb())
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(bigTextStyle)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_REMINDER)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)

    if (!summaryText.isNullOrBlank()) {
      builder.setSubText(summaryText)
    }

    if (todoIconBitmap != null) {
      builder.setLargeIcon(todoIconBitmap)
      try {
        val todoIconCompat = IconCompat.createWithAdaptiveBitmap(todoIconBitmap)
        val senderPerson = Person.Builder()
          .setName(title)
          .setKey("todo_task_$notificationId")
          .setIcon(todoIconCompat)
          .build()
        val shortcutInfo = ShortcutInfoCompat.Builder(context, TODO_NOTIFICATION_SHORTCUT_ID)
          .setShortLabel(Tool.TODO_LIST.title)
          .setLongLabel(Tool.TODO_LIST.title)
          .setIcon(todoIconCompat)
          .setIntent(launchIntent)
          .setPerson(senderPerson)
          .setLongLived(true)
          .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo)
        val messagingStyle = NotificationCompat.MessagingStyle(senderPerson)
          .setGroupConversation(false)
          .addMessage(content, System.currentTimeMillis(), senderPerson)
        builder
          .setStyle(messagingStyle)
          .setShortcutInfo(shortcutInfo)
      } catch (_: Exception) {
        // Fallback to BigTextStyle + LargeIcon if ShortcutManager is unavailable
      }
    }

    val notification = builder.build()

    try {
      NotificationManagerCompat.from(context).notify(notificationId, notification)
      NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        AppPermission.POST_NOTIFICATIONS,
        Tool.TODO_LIST,
      )
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
    scheduledAlarmTaskIds.add(task.id)
  }

  /**
   * Cancels any scheduled alarm for [taskId] and optionally dismisses any active status-bar notification.
   */
  fun cancelTaskAlarm(context: Context, taskId: String, dismissActiveNotification: Boolean = true) {
    scheduledAlarmTaskIds.remove(taskId)
    if (dismissActiveNotification) {
      try {
        NotificationManagerCompat.from(context).cancel(taskId.hashCode())
      } catch (_: Throwable) {}
    }
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
   * Synchronizes alarms for the current active task list, cancelling alarms for any tasks
   * that were deleted, completed, unscheduled, or already notified.
   */
  fun syncTaskAlarms(context: Context, tasks: List<TodoItem>) {
    ensureNotificationChannel(context)
    val activeTaskIds = tasks.map { it.id }.toSet()
    val removedTaskIds = synchronized(scheduledAlarmTaskIds) {
      scheduledAlarmTaskIds.toSet() - activeTaskIds
    }
    removedTaskIds.forEach { taskId ->
      cancelTaskAlarm(context, taskId, dismissActiveNotification = true)
    }
    tasks.forEach { task ->
      if (task.scheduledAtMillis != null && task.notifiedAtScheduleMillis != task.scheduledAtMillis) {
        scheduleTaskAlarm(context, task)
      } else {
        cancelTaskAlarm(context, task.id, dismissActiveNotification = false)
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
      NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        AppPermission.RECEIVE_BOOT_COMPLETED,
        Tool.TODO_LIST,
      )
      return
    }

    if (action == TodoNotificationHelper.ACTION_TODO_SCHEDULE_ALARM) {
      val taskId = intent.getStringExtra(TodoNotificationHelper.EXTRA_TASK_ID)
      val intentScheduledAt = intent.getLongExtra(TodoNotificationHelper.EXTRA_SCHEDULED_AT, -1L)
        .takeIf { it > 0L }
      val taskToNotify = TodoListEngine.consumeScheduledAlarm(
        repository = repo,
        taskId = taskId,
        scheduledAtMillis = intentScheduledAt,
      )
      if (taskToNotify != null) {
        TodoNotificationHelper.showTaskNotification(context, taskToNotify)
      }
    }
  }
}
