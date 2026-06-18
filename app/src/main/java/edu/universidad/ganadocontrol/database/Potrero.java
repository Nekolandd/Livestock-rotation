package edu.universidad.ganadocontrol.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "potreros")
public class Potrero implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nombre;
    public double medidas;
    public String fechaCreacion;
    public String fotoPath;
    public String videoPath;

    public Potrero() {}

    public Potrero(String nombre, double medidas, String fechaCreacion, String fotoPath, String videoPath) {
        this.nombre = nombre;
        this.medidas = medidas;
        this.fechaCreacion = fechaCreacion;
        this.fotoPath = fotoPath;
        this.videoPath = videoPath;
    }
}
