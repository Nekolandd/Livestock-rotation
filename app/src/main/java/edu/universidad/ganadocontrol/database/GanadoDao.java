package edu.universidad.ganadocontrol.database;

import androidx.room.*;
import java.util.List;

@Dao
public interface GanadoDao {

    // --- Operaciones de Potreros ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPotrero(Potrero potrero);

    @Update
    void updatePotrero(Potrero potrero);

    @Delete
    void deletePotrero(Potrero potrero);

    @Transaction
    default void deletePotreroWithHistorico(Potrero potrero) {
        deleteRotacionesForPotrero(potrero.id);
        deletePotrero(potrero);
    }

    @Query("SELECT * FROM potreros ORDER BY id ASC")
    List<Potrero> getAllPotreros();

    @Query("SELECT * FROM potreros WHERE id = :id LIMIT 1")
    Potrero getPotreroById(int id);

    // --- Operaciones de Históricos de Rotación ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRotacion(HistoricoRotacion rotacion);

    @Query("SELECT * FROM historicos_rotacion WHERE potreroId = :potreroId ORDER BY fechaInicio DESC")
    List<HistoricoRotacion> getRotacionesForPotrero(int potreroId);

    @Query("SELECT * FROM historicos_rotacion ORDER BY fechaInicio DESC")
    List<HistoricoRotacion> getAllRotaciones();

    @Query("DELETE FROM historicos_rotacion WHERE potreroId = :potreroId")
    void deleteRotacionesForPotrero(int potreroId);
}
