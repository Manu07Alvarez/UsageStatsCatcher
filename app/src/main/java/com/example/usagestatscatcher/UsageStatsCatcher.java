package com.example.usagestatscatcher;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

public class UsageStatsCatcher extends AppCompatActivity {

  private static final String TAG = "UsageStatsLog";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Verificar si el permiso ya está concedido
    if (!hasUsageStatsPermission()) {
      // Si no está concedido, lanzar la intent para que el usuario habilite el permiso
      Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
      startActivity(intent);
    } else {
      // Si el permiso ya está concedido, puedes continuar con tu lógica
      performTask();
    }

    // Finalizar la actividad para cerrar la app
    finish();
  }

  private boolean hasUsageStatsPermission() {
    UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
    long currentTime = System.currentTimeMillis();
    List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * 60, currentTime);
    return stats != null && !stats.isEmpty();
  }
  private void performTask() {
    UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

    // Configurar el tiempo al inicio del sistema
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    long startTime = calendar.getTimeInMillis();

    // Configurar el tiempo al horario actual
    calendar.setTimeInMillis(System.currentTimeMillis());
    long endTime = calendar.getTimeInMillis();

    Log.d(TAG, "Inicio: " + startTime);
    Log.d(TAG, "Fin: " + endTime);
    // Obtener estadísticas de uso
    List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
    if (usageStatsList.isEmpty()) {
      Log.d(TAG, "No hay estadísticas de uso disponibles para el periodo solicitado");
    } else {
      createFile(usageStatsList);
    }
  }

  private void createFile(List<UsageStats> usageStatsList) {

    ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "usage_stats.txt");
    values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
    values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/UsageStats");
    
    // Crear un archivo de texto en el almacenamiento externo
    Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);



    try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
      for (UsageStats usageStats : usageStatsList) {
        String data = "Paquete: " + usageStats.getPackageName() + "\n" +
            "Tiempo total en pantalla: " + usageStats.getTotalTimeInForeground() + " ms\n\n";
        outputStream.write(data.getBytes());
      }
      Log.d(TAG, "Operacion Completada Exitosamente");
    } catch (IOException e) {
      Log.e(TAG, "Error writing to file", e);
    }
  }
}