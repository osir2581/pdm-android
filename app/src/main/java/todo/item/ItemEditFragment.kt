package ro.ubbcluj.cs.ilazar.myapp2.todo.item

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_item_edit.*
import ro.ubbcluj.cs.ilazar.myapp2.MainActivity
import ro.ubbcluj.cs.ilazar.myapp2.R
import ro.ubbcluj.cs.ilazar.myapp2.core.NetworkLiveData
import ro.ubbcluj.cs.ilazar.myapp2.core.TAG
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ItemEditFragment : Fragment() {
    companion object {
        const val ITEM_ID = "ITEM_ID"
    }

    private lateinit var connectivityManager: ConnectivityManager
    lateinit var networkLiveData: NetworkLiveData
    private lateinit var viewModel: ItemEditViewModel
    private var itemId: String? = null
    private var item: Item? = null
    private val REQUEST_PERMISSION = 10
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2

    lateinit var currentPhotoPath: String

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                itemId = it.getString(ITEM_ID).toString()
            }
        }
        connectivityManager =
            activity?.getSystemService(android.net.ConnectivityManager::class.java) as ConnectivityManager
        networkLiveData = NetworkLiveData(connectivityManager)
        networkLiveData.observe(this, Observer {
            Log.v(TAG, "recieved notification from live data : $it")
            NetworkLiveData.networkInfo = it
            viewModel.connected = it
            //onlineSwitch.isChecked = it;
            //Log.i("MainActivity","Online switch is checked: "+onlineSwitch.isChecked.toString())
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fab.setOnClickListener {
            Log.v(TAG, "save item")
            val i = item
            if (i != null) {
                i.description = item_text.text.toString()
                i.title = item_title.text.toString()
                viewModel.saveOrUpdateItem(i)
            }
        }
        butonCamera.setOnClickListener { openCamera() }
        butonGalerie.setOnClickListener { openGallery() }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ItemEditViewModel::class.java)
        viewModel.fetching.observe(viewLifecycleOwner, { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        })
        viewModel.fetchingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.completed.observe(viewLifecycleOwner, { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                //findNavController().popBackStack()
                findNavController().navigate(R.id.action_ItemEditFragment_to_ItemListFragment)
            }
        })
        val id = itemId
        if (id == null) {
            item = Item("", "", "")
        } else {
            viewModel.getItemById(id).observe(viewLifecycleOwner, {
                Log.v(TAG, "update items")
                if (it != null) {
                    item = it
                    item_text.setText(it.description)
                    item_title.setText(it.title)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
            != PackageManager.PERMISSION_GRANTED) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_PERMISSION
                )
            }
        }
    }

    private fun openCamera() {
        Log.i("asd","AICI")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.let {
                intent.resolveActivity(it.packageManager)?.also {
                    val photoFile: File? = try {
                        createCapturedPhoto()
                    } catch (ex: IOException) {
                        null
                    }
                    Log.d("MainActivity", "photofile $photoFile");
                    photoFile?.also {
                        val photoURI = FileProvider.getUriForFile(
                            requireActivity(),
                            "ro.ubbcluj.cs.ilazar.myapp2.fileprovider",
                            it
                        )
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                val uri = Uri.parse(currentPhotoPath)
                imageView.setImageURI(uri)
            } else if (requestCode == REQUEST_PICK_IMAGE) {
                val uri = data?.getData()
                imageView.setImageURI(uri)
            }
        }
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

}