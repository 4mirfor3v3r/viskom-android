package amirlabs.plate_detection

import amirlabs.plate_detection.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.signature.ObjectKey
import id.zelory.compressor.Compressor
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

    private var originalWidth: Int? = null
    private var originalHeight: Int?= null

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
//                        image = f
//                        withContext(Dispatchers.Main){
//                            binding.ivPreview.visibility = View.VISIBLE
//                            Glide.with(binding.root.context)
//                                .load(image)
//                                .into(binding.ivPreview)
//                        }



                        withContext(Dispatchers.IO){
                            val compressedImage = Compressor.compress(this@MainActivity, f)
                            image = compressedImage
                            withContext(Dispatchers.Main){
                                binding.textureImage.invalidate()
                                binding.ivPreview.visibility = View.VISIBLE
                                Glide.with(binding.root.context)
                                    .load(image)
                                    .into(binding.ivPreview)
                            }
                        }
                    }
                }
            }
        }
        openCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { res ->
            if (res) {
                binding.ivPreview.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO){
                        val compressedImage = Compressor.compress(this@MainActivity, image!!)
                        image = compressedImage
                        withContext(Dispatchers.Main){
                            binding.textureImage.invalidate()
                            Glide.with(binding.root.context)
                                .load(image)
                                .into(binding.ivPreview)
                        }
                    }
                }
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
        binding.tvResult.text = "Loading..."
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addPart(filePart)
        api.submitImage(body.build())
            .subscribe({
                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        if(it.data?.isNotEmpty() ==true) {
                            var txt = ""
                            for (item in it.data) {
                                if(item.confidenceScore > 0.5) {
//                                    txt += "BBOX : ${item.box}\n"

//                                    val matrix: Matrix = binding.ivPreview.imageMatrix
//                                    val values = FloatArray(9)
//                                    matrix.getValues(values)

                                    // Offset gambar dalam ImageView
//                                    val transX = values[Matrix.MTRANS_X]
//                                    val transY = values[Matrix.MTRANS_Y]


                                    // Dimensi tampilan gambar
//                                    val displayedWidth: Int = binding.ivPreview.measuredWidth
//                                    val displayedHeight: Int = binding.ivPreview.measuredHeight


                                    // Hitung rasio skala
//                                    val scaleX: Float = displayedWidth.toFloat() / originalWidth!!
//                                    val scaleY: Float = displayedHeight.toFloat() / originalHeight!!


                                    // Rescale koordinat
//                                    var newX1 = (item.box[0] * scaleX)
//                                    var newY1 = (item.box[1] * scaleY)
//                                    var newX2 = (item.box[2] * scaleX)
//                                    var newY2 = (item.box[3] * scaleY)


                                    // Offset jika scaleType bukan fitXY
//                                    val matrix: Matrix = binding.ivPreview.getImageMatrix()
//                                    val values = FloatArray(9)
//                                    matrix.getValues(values)

//                                    val offsetX = values[Matrix.MTRANS_X]
//                                    val offsetY = values[Matrix.MTRANS_Y]

//                                    newX1 += offsetX;
//                                    newY1 += offsetY;
//                                    newX2 += offsetX;
//                                    newY2 += offsetY;

//                                    Log.e("TAG", "sendToAPI: ($offsetX, $offsetY, $displayedWidth, $displayedHeight, $scaleX, $scaleY) (${item.box[0]}, ${item.box[1]}, ${item.box[2]}, ${item.box[3]}" )

//                                    binding.textureImage.setBoundingBox(
//                                        (transX + item.box[0] + l).toInt()-20,
//                                        (transY + item.box[1] + l).toInt()+12,
//                                        (transX + item.box[2] + r).toInt()-12,
//                                        (transY + item.box[3] + (item.box[1])).toInt()+20
//                                    )

//                                        binding.textureImage.setBoundingBox(
//                                            newX1.toInt(),
//                                            newY1.toInt(),
//                                            newX2.toInt(),
//                                            newY2.toInt()
//                                        )

                                        Glide.with(binding.root.context)
                                            .load(it.imageLink)
                                            .signature(ObjectKey(System.currentTimeMillis().toString()))
                                            .into(binding.ivPreview)
//                                        txt += "Confidence Score: ${item.confidenceScore}"
                                }
                            }
                            binding.tvResult.text = txt
                        }else{
                            binding.tvResult.text = "No Result"
                        }
                    }
                }

            },{
                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        binding.tvResult.text = it.message
                    }
                }

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
            "amirlabs.plate_detection.provider",
            file
        )
        openCamera.launch(uri)
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
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