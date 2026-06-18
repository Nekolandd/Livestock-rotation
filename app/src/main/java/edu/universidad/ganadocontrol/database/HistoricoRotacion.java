package edu.universidad.ganadocontrol.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(
    tableName = "historicos_rotacion",
    foreignKeys = @ForeignKey(
        entity = Potrero.class,
        parentColumns = "id",
        childColumns = "potreroId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("potreroId")}
)
public class HistoricoRotacion implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int potreroId;
    public long fechaInicio;
    public long fechaFinRojo;
    public long fechaFinNaranja;

    public HistoricoRotacion() {}

    public HistoricoRotacion(int potreroId, long fechaInicio, long fechaFinRojo, long fechaFinNaranja) {
        this.potreroId = potreroId;
        this.fechaInicio = fechaInicio;
        this.fechaFinRojo = fechaFinRojo;
        this.fechaFinNaranja = fechaFinNaranja;
    }
}
