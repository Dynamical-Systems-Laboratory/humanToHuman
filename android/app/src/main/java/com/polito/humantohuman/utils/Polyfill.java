package com.polito.humantohuman.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import com.polito.humantohuman.Activities.ScanActivity;
import com.polito.humantohuman.R;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class Polyfill {

  private Polyfill() {}

  public interface Supplier<T> { T get(); }

  public interface Consumer<T> { void accept(T t); }

  public static class CountdownExecutor {

    public final AtomicInteger countdown;
    public final Runnable executable;

    public CountdownExecutor(int countdown, Runnable executable) {
      this.countdown = new AtomicInteger(countdown);
      this.executable = executable;
    }

    public void decrement() {
      if (countdown.decrementAndGet() == 0) {
        executable.run();
      }
    }
  }

  public static class RunOnceExecutor<T> {

    public final AtomicBoolean hasRun;
    public final Consumer<T> executable;

    public RunOnceExecutor(Consumer<T> executable) {
      this.hasRun = new AtomicBoolean(false);
      this.executable = executable;
    }

    public void run(T t) {
      if (hasRun.compareAndSet(false, true)) {
        executable.accept(t);
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static void startForeground(Service service, int id, String channelId,
                                     String channelName) {
    /**
     * Creating Notification Channel
     */
    NotificationChannel chan = new NotificationChannel(
        channelId, channelName, NotificationManager.IMPORTANCE_UNSPECIFIED);
    chan.setLightColor(Color.BLUE);
    chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    NotificationManager manager = (NotificationManager)service.getSystemService(
        Context.NOTIFICATION_SERVICE);

    /**
     * Since it crash sometimes with UNSPECIFIED, we should try with None.
     */
    try {
      manager.createNotificationChannel(chan);
      Log.d("Status", "Starting the service with UNSPECIFIED importance");
    } catch (IllegalArgumentException e) {
      chan.setImportance(NotificationManager.IMPORTANCE_NONE);
      manager.createNotificationChannel(chan);
      Log.d("Status", "Starting the service with NONE importance");
    }

    // Setting onclick on the notification
    Intent intent = new Intent(service, ScanActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(service, 0, intent, 0);

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(service, channelId);
    Notification notification =
        notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setPriority(NotificationManager.IMPORTANCE_UNSPECIFIED)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(
                "HTH is scanning"))
            .build();

    service.startForeground(id, notification);
  }
}
