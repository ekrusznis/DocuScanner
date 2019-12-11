package ek.uw.docuscanner.ui.main.camera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Base64.NO_WRAP
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.camerakit.CameraKitView
import com.google.android.material.textfield.TextInputEditText
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import com.squareup.picasso.Picasso
import ek.uw.docuscanner.R
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CameraFragment : Fragment(){
    private lateinit var viewModel: CameraViewModel
    private lateinit var convertButton: Button
    private lateinit var camera: CameraKitView
    private lateinit var blackWhiteColorImage: ImageView
    private lateinit var image_view: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var pdfNameInput: TextInputEditText
    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.camera_fragment, container, false)
        camera = root.findViewById(R.id.camera)
        blackWhiteColorImage = root.findViewById(R.id.blackWhiteColorImage)
        toolbar = root.findViewById(R.id.toolbar)
        pdfNameInput = root.findViewById(R.id.pdfNameInput)
        convertButton = root.findViewById(R.id.convertButton)
        image_view = root.findViewById(R.id.image_view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (requireContext().checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
                //permission was not enabled
                val permission =
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            }
            else{
                openCamera()
            }
        }
        else{
            openCamera()
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

    }
    private fun openCamera() {
        toolbar.visibility = View.GONE
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    openCamera()
                }
                else{
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK){
            image_view.visibility = View.VISIBLE
            toolbar.visibility = View.VISIBLE
            image_view.setImageURI(image_uri)
            blackWhiteColorImage.setOnClickListener(View.OnClickListener {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, image_uri)
            toGrayScale(bitmap)
            })
            convertButton.setOnClickListener(View.OnClickListener {
                if (pdfNameInput.text.isNullOrEmpty()){
                    Toast.makeText(requireContext(), "Please fill in name section", Toast.LENGTH_SHORT).show()
                }else{
                    savePDF(image_uri.toString(), pdfNameInput.text.toString())
                }})
        }
    }

    fun savePDF(imageFileName: String, pdfName: String){
        val document = Document()
        val directoryPath =
            Environment.getExternalStorageDirectory().toString()
        PdfWriter.getInstance(
            document,
            FileOutputStream(directoryPath + "/" + pdfName)
        ) //  pdf's name.

        document.open()

        val image: Image = Image.getInstance(imageFileName) // Change image's name and extension.

        val scaler: Float = (document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / image.getWidth() * 100 // 0 means you have no indentation. If you have any, change it.

        image.scalePercent(scaler)
        image.setAlignment(Image.ALIGN_CENTER or Image.ALIGN_TOP)

        document.add(image)
        document.close()
    }
    fun toGrayScale(srcImage: Bitmap): Bitmap{
        val bmpGrayScale = Bitmap.createBitmap(
            srcImage.width,
            srcImage.height,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bmpGrayScale)
        val paint = Paint()

        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(srcImage, 0f, 0f, paint)

        return bmpGrayScale
    }
    fun setBitmapToImage(greyPic: Bitmap){
    }
    fun shareFile(file: File){
        val shareItent = Intent(Intent.ACTION_SEND)
        if (file.exists()){
            shareItent.type = "application/pdf"
            shareItent.extras.apply {
                Intent.EXTRA_STREAM
                Uri.fromFile(file)
            }
            shareItent.extras.apply {
                Intent.EXTRA_SUBJECT
                "Sharing File From DocuScanner"
            }
            shareItent.extras.apply {
                Intent.EXTRA_TEXT
                "Sharing File From DocuScanner"
            }
        }
        requireContext().startActivity(Intent.createChooser(shareItent, "Share your file"))
    }
}