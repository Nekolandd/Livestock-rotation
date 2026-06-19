package edu.universidad.ganadocontrol.fragments

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import edu.universidad.ganadocontrol.MainActivity
import edu.universidad.ganadocontrol.R
import edu.universidad.ganadocontrol.database.AppDatabase
import edu.universidad.ganadocontrol.database.Potrero
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PotreroFormFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etArea: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var ivPhoto: ImageView
    private lateinit var btnCapturePhoto: Button
    private lateinit var btnCaptureVideo: Button
    private lateinit var btnPlayVideo: Button
    private lateinit var tvVideoStatus: TextView
    private lateinit var btnSave: Button
    private lateinit var tvFormTitle: TextView

    private lateinit var db: AppDatabase
    private var editingPotreroId: Int = 0
    private var currentPhotoPath: String? = null
    private var currentVideoPath: String? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_VIDEO_CAPTURE = 102
        private const val REQUEST_CAMERA_PERMISSION = 103
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_potrero_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        // Vincular componentes
        tvFormTitle = view.findViewById(R.id.tvFormTitle)
        etName = view.findViewById(R.id.etName)
        etArea = view.findViewById(R.id.etArea)
        etDate = view.findViewById(R.id.etDate)
        ivPhoto = view.findViewById(R.id.ivPotreroPhoto)
        btnCapturePhoto = view.findViewById(R.id.btnCapturePhoto)
        btnCaptureVideo = view.findViewById(R.id.btnCaptureVideo)
        btnPlayVideo = view.findViewById(R.id.btnPlayVideo)
        tvVideoStatus = view.findViewById(R.id.tvVideoStatus)
        btnSave = view.findViewById(R.id.btnSave)

        // Configurar selector de fecha
        etDate.setOnClickListener {
            showDatePicker()
        }

        // Obtener ID si es modo edición
        arguments?.let {
            editingPotreroId = it.getInt("potrero_id", 0)
        }

        if (editingPotreroId > 0) {
            tvFormTitle.text = "Actualizar Potrero"
            btnSave.text = "Actualizar Potrero"
            loadPotreroData(editingPotreroId)
        } else {
            tvFormTitle.text = "Registro de Potrero"
            btnSave.text = "Guardar Potrero"
            // Por defecto, fecha actual
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDate.setText(sdf.format(Date()))
        }

        // Eventos de captura
        btnCapturePhoto.setOnClickListener {
            pendingCameraAction = { launchCameraForPhoto() }
            launchCameraForPhoto()
        }

        btnCaptureVideo.setOnClickListener {
            pendingCameraAction = { launchCameraForVideo() }
            launchCameraForVideo()
        }

        btnPlayVideo.setOnClickListener {
            playVideo()
        }

        btnSave.setOnClickListener {
            saveOrUpdatePotrero()
        }
    }

    private fun showDatePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDate.setText(sdf.format(calendar.time))
        }

        DatePickerDialog(
            requireContext(),
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadPotreroData(id: Int) {
        val potrero = db.ganadoDao().getPotreroById(id)
        potrero?.let {
            etName.setText(it.nombre)
            etArea.setText(it.medidas.toString())
            etDate.setText(it.fechaCreacion)
            currentPhotoPath = it.fotoPath
            currentVideoPath = it.videoPath

            // Cargar foto si existe
            if (!it.fotoPath.isNullOrEmpty()) {
                val file = File(it.fotoPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(it.fotoPath)
                    ivPhoto.setImageBitmap(bitmap)
                }
            }

            // Mostrar estado del video
            if (!it.videoPath.isNullOrEmpty()) {
                val file = File(it.videoPath)
                if (file.exists()) {
                    btnPlayVideo.visibility = View.VISIBLE
                    tvVideoStatus.text = "Video registrado"
                }
            }
        }
    }

    private fun launchCameraForPhoto() {
        if (!hasCameraPermission()) return
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCameraForVideo() {
        if (!hasCameraPermission()) return
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        try {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la cámara para video: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private var pendingCameraAction: (() -> Unit)? = null

    private fun hasCameraPermission(): Boolean {
        val ctx = requireContext()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pendingCameraAction?.invoke()
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            Toast.makeText(context, "Se requiere permiso de cámara para capturar foto/video", Toast.LENGTH_SHORT).show()
        }
        pendingCameraAction = null
    }

    private fun playVideo() {
        if (!currentVideoPath.isNullOrEmpty()) {
            try {
                val file = File(currentVideoPath!!)
                if (file.exists()) {
                    val context = requireContext()
                    val videoUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(videoUri, "video/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "El archivo de video no existe", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al reproducir video: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        try {
                            val file = File(requireContext().externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
                            FileOutputStream(file).use { out ->
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            currentPhotoPath = file.absolutePath
                            ivPhoto.setImageBitmap(imageBitmap)
                            Toast.makeText(context, "Foto capturada y guardada", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                REQUEST_VIDEO_CAPTURE -> {
                    val videoUri = data?.data
                    if (videoUri != null) {
                        try {
                            val file = File(requireContext().externalCacheDir, "video_${System.currentTimeMillis()}.mp4")
                            requireContext().contentResolver.openInputStream(videoUri)?.use { input ->
                                FileOutputStream(file).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            currentVideoPath = file.absolutePath
                            btnPlayVideo.visibility = View.VISIBLE
                            tvVideoStatus.text = "Video grabado con éxito"
                            Toast.makeText(context, "Video guardado", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al guardar video: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun saveOrUpdatePotrero() {
        val nombre = etName.text.toString().trim()
        val medidasStr = etArea.text.toString().trim()
        val fecha = etDate.text.toString().trim()

        if (nombre.isEmpty()) {
            etName.error = "Ingresa el nombre del lote"
            return
        }

        if (medidasStr.isEmpty()) {
            etArea.error = "Ingresa las medidas en mts²"
            return
        }

        val medidas = medidasStr.toDoubleOrNull()
        if (medidas == null || medidas <= 0) {
            etArea.error = "Ingresa una medida válida y mayor a cero"
            return
        }

        val potrero = Potrero().apply {
            id = editingPotreroId
            this.nombre = nombre
            this.medidas = medidas
            this.fechaCreacion = fecha
            this.fotoPath = currentPhotoPath
            this.videoPath = currentVideoPath
        }

        val dao = db.ganadoDao()
        if (editingPotreroId > 0) {
            dao.updatePotrero(potrero)
            Toast.makeText(context, "Potrero actualizado con éxito", Toast.LENGTH_SHORT).show()
        } else {
            dao.insertPotrero(potrero)
            Toast.makeText(context, "Potrero guardado con éxito", Toast.LENGTH_SHORT).show()
        }

        clearForm()
        (activity as? MainActivity)?.navigateToPotrerosList()
    }

    private fun clearForm() {
        editingPotreroId = 0
        etName.text?.clear()
        etArea.text?.clear()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.setText(sdf.format(Date()))
        ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
        btnPlayVideo.visibility = View.GONE
        tvVideoStatus.text = "Sin video registrado"
        currentPhotoPath = null
        currentVideoPath = null
    }
}
