package amirlabs.miniprojectailo

import amirlabs.miniprojectailo.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var openCamera:ActivityResultLauncher<Uri>
    private var image: File? = null
    private lateinit var uri: Uri
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var api: MainService
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        initData()
        initUI()
    }

    private fun initData(){
        api = NetworkConfig.apiService
        cameraExecutor = Executors.newSingleThreadExecutor()
        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val f = File(this@MainActivity.cacheDir, System.currentTimeMillis().toString())
                    withContext(Dispatchers.IO){
                        f.createNewFile()
                        val inputStream = this@MainActivity.contentResolver?.openInputStream(uri)
                        var fos: FileOutputStream? = null
                        try {
                            fos = FileOutputStream(f)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                        try {
                            fos?.write(inputStream?.readBytes())
                            fos?.flush()
                            fos?.close()
                            inputStream?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        image = f
                        withContext(Dispatchers.Main){
                            binding.ivPreview.visibility = View.VISIBLE
                            Glide.with(binding.root.context)
                                .load(image)
                                .into(binding.ivPreview)
                        }
                    }
                }
            }
        }
        openCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { res ->
            if (res) {
                binding.ivPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivPreview)
            }
        }
    }

    private fun initUI(){
        binding.btnGallery.setOnClickListener {
            if (checkPermissions()) {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                requestPermissions()
            }
        }
        binding.btnCamera.setOnClickListener {
            if (checkPermissions()) {
                dispatchTakePictureIntent()
            } else {
                requestPermissions()
            }
        }
        binding.btnSubmit.setOnClickListener {
            if(image != null){
                sendToAPI(image!!)
            }

        }
    }

    private fun sendToAPI(file: File) {
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addPart(filePart)
        api.submitImage(body.build())
            .subscribe({
                binding.tvResult.text = "Predicted class: "+it.data
            },{

            }).let(disposable::add)
    }

    private fun dispatchTakePictureIntent() {
        val file = File.createTempFile(
            "IMG_",
            ".jpg",
            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        image = file
        uri = FileProvider.getUriForFile(
            this,
            "amirlabs.miniprojectailo.provider",
            file
        )
        openCamera.launch(uri)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED&&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED&&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 1
        )
    }
}