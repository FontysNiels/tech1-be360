package com.firstapp.tech_1

import androidx.compose.ui.unit.dp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberImagePainter
import com.firstapp.tech_1.ui.theme.btnGray
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

//TODO:
// - FullScreen
// - Save Image

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)

    private lateinit var photoUri: Uri
    private lateinit var photoUriSelf: Uri
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)

    var lensSide = CameraSelector.LENS_FACING_BACK
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission Granted")
            shouldShowCamera.value = true
        } else {
            Log.i("kilo", "Permission Not Granted")
        }
    }
    var amountOfPics = 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            if (shouldShowCamera.value) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                CameraView(
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = ::handleImageCapture,
                    onSelfCaptured = ::handleImageCaptureSelfie,
                    onCancel = ::goBack,
                    side = lensSide,
                ) { Log.e("kilo", "View error:", it) }

            } else {

                //List for old images
                val fileList: ArrayList<File> = ArrayList()

                //Get old images
                fun imageReaderNew(root: File) {
                    val listAllFiles = root.listFiles()
                    if (listAllFiles != null && listAllFiles.isNotEmpty()) {
                        val sortedFiles = listAllFiles.sortedByDescending { it.lastModified() }
                        for (currentFile in sortedFiles) {
                            if (currentFile.name.endsWith(".jpg")) {
                                // File absolute path
                                Log.e("downloadFilePath", currentFile.absolutePath)
                                // File Name
                                Log.e("downloadFileName", currentFile.name)
                                fileList.add(currentFile.absoluteFile)
                            }
                        }
                        Log.w("fileList1", "" + fileList.size)
                    }
                }

                var gpath: String = outputDirectory.toString()
                var fullPath = File(gpath + File.separator)
                Log.w("fullpath", "" + fullPath)
                imageReaderNew(fullPath)

                Log.e("tester", outputDirectory.toString())

                fun DeleteImage(Img1: Int, Img2: Int) {
                    Log.e("delim", "$Img1 & $Img2")

                    val FirstImage = File(fileList[Img1].toString())
                    val SecondImage = File(fileList[Img2].toString())

                    if (FirstImage.exists() && SecondImage.exists()) {
                        val deleteFirstImage = FirstImage.delete()
                        val deleteSecondImage = SecondImage.delete()
                        if (deleteFirstImage && deleteSecondImage) {
                            shouldShowCamera.value = true
                            shouldShowCamera.value = false
                        } else {
                            // failed to delete file
                        }
                    } else {
                        // file does not exist
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    var count = 0
                    var image1 = 0
                    var image2 = 1

                    while (count < amountOfPics) {

                        if (fileList.size <= image2) {
                            break;
                        }
                        var num1 = image1
                        var num2 = image2
                        Box(
                            modifier = Modifier
//                                .border(width = 1.dp, Color.Red)
                                .fillMaxWidth()
                        ) {
                            Row(
                                Modifier
                                    .padding(0.dp)
                                    .fillMaxWidth()
                            ) {
                                Image(
                                    painter = rememberImagePainter(fileList[image1]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)

                                )
                                Image(
                                    painter = rememberImagePainter(fileList[image2]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(y = 10.dp)
                                    .background(
                                        color = btnGray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .clickable(onClick = { DeleteImage(num1, num2) }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "X",
                                    color = Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        count++
                        image1 += 2
                        image2 += 2
                    }



                    if (fileList.size > amountOfPics * 2) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { loadMore() },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Black,
                                    contentColor = White
                                )
                            ) {
                                Text(text = "Show More...")
                            }
                        }
                        Spacer(modifier = Modifier.height(80.dp))
                    } else {
                        Spacer(modifier = Modifier.height(50.dp))
                    }

                }

                CamButton()


                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            if (shouldShowPhoto.value) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(1.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(300.dp)
                            .width(700.dp)
                            .align(Alignment.Center)
                            .offset(x = 0.dp, y = 20.dp)
                    ) {
//                        Image(
//                            painter = rememberImagePainter(photoUri),
//                            contentDescription = null,
//                            modifier = Modifier.weight(1f)
//                        )
//                        Image(
//                            painter = rememberImagePainter(photoUriSelf),
//                            contentDescription = null,
//                            modifier = Modifier.weight(1f)
//                        )

                    }
                }

            }
        }
        requestCameraPermission()

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission Granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show Camera Permission dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    //Image 1
    private fun handleImageCapture(uri: Uri) {
        Log.i("kilo", "Image captured Back: $uri")

        photoUri = uri
        lensSide = CameraSelector.LENS_FACING_FRONT
        shouldShowCamera.value = false
        shouldShowCamera.value = true

    }

    //Image 2
    private fun handleImageCaptureSelfie(uri: Uri) {
        Log.i("kilo", "Image captured Selfie: $uri")
        photoUriSelf = uri
        shouldShowCamera.value = false
        shouldShowPhoto.value = true
        lensSide = CameraSelector.LENS_FACING_BACK
    }

    private fun loadMore() {
        amountOfPics += 3
        shouldShowCamera.value = true
        shouldShowCamera.value = false
    }

    //Cancel Button
    private fun goBack() {
        shouldShowCamera.value = false
    }

    //Onclick of CamButton
    private fun changeData() {
        shouldShowCamera.value = true
        shouldShowPhoto.value = false
        amountOfPics = 4
    }

    //Dunno, Something with directory....
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    @Composable
    fun CamButton() {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
//                .border(width = 1.dp, Color.Green)
        ) {
            Button(
                onClick = { changeData() },
                colors = ButtonDefaults
                    .buttonColors(backgroundColor = Black, contentColor = White)
            ) {
                Text(text = "Take A 360\u00B0 Picture")
            }
        }
    }


}


