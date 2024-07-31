package com.example.younme.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.younme.R
import com.example.younme.adapter.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class InsertionStatusActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mDbRefStatus: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImageView: CircleImageView
    private lateinit var txtName: TextView
    private lateinit var etPostContent: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var ivSelectedImage: ImageView
    private lateinit var btnPost: Button

    private lateinit var progressDialog: ProgressDialog
    private var selectedImageUri: Uri? = null

    private val captureImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri?.let {
                ivSelectedImage.setImageURI(it)
                ivSelectedImage.visibility = ImageView.VISIBLE
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivSelectedImage.setImageURI(it)
            ivSelectedImage.visibility = ImageView.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertion_status)

        profileImageView = findViewById(R.id.profile_image)
        txtName = findViewById(R.id.txtName)
        etPostContent = findViewById(R.id.etPostContent)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
        btnPost = findViewById(R.id.btnPost)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference("user")
        mDbRefStatus = FirebaseDatabase.getInstance().getReference("status")
        storageReference = FirebaseStorage.getInstance().reference

        progressDialog = ProgressDialog(this).apply {
            setMessage("Uploading...")
            setCancelable(false)
        }

        loadUserProfile()

        btnUploadImage.setOnClickListener {
            showImagePickerDialog()
        }

        btnPost.setOnClickListener {
            postStatus()
        }
    }

    private fun loadUserProfile() {
        val currentUser = mAuth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            mDbRef.child(userId).get().addOnSuccessListener { snapshot ->
                val userName = snapshot.child("name").getValue(String::class.java)
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                txtName.text = userName ?: "No name"
                if (profileImageUrl != null) {
                    Glide.with(this).load(profileImageUrl).into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.profile) // Default avatar image
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val photoUri = createImageUri()
                    selectedImageUri = photoUri
                    captureImage.launch(photoUri)
                }
                options[item] == "Choose from Gallery" -> {
                    pickImage.launch("image/*")
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun createImageUri(): Uri? {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "new_image.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun postStatus() {
        progressDialog.show()

        val currentUser = mAuth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val postContent = etPostContent.text.toString()

            mDbRef.child(userId).get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                if (selectedImageUri != null) {
                    val imageRef = storageReference.child("status/$userId/${UUID.randomUUID()}.jpg")
                    imageRef.putFile(selectedImageUri!!)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                val status = Status(
                                    postContent,
                                    imageUrl.toString(),
                                    System.currentTimeMillis(),
                                    userId,
                                    profileImageUrl
                                )
                                mDbRefStatus.child(userId).push().setValue(status)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Status posted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        progressDialog.dismiss()
                                        finish() // Close activity after posting
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Failed to post status",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        progressDialog.dismiss()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT)
                                .show()
                            progressDialog.dismiss()
                        }
                } else {
                    val status = Status(
                        postContent,
                        null,
                        System.currentTimeMillis(),
                        userId,
                        profileImageUrl
                    )
                    mDbRefStatus.child(userId).push().setValue(status)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Status posted successfully", Toast.LENGTH_SHORT)
                                .show()
                            progressDialog.dismiss()
                            finish() // Close activity after posting
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to post status", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to retrieve user profile image URL",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismiss()
            }
        }
    }
}
