package edu.universidad.ganadocontrol.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Potrero.class, HistoricoRotacion.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GanadoDao ganadoDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "ganado_control_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Permite consultas en el hilo principal para el entorno académico
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
