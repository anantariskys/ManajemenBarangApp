package com.example.manajemenbarangapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateUpdateActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var editTextNama: EditText
    private lateinit var editTextHarga: EditText
    private lateinit var editTextMerk: EditText
    private lateinit var editTextStok: EditText
    private lateinit var imageViewBarang: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSave: Button
    private var imageUri: Uri? = null
    private var isUpdate: Boolean = false
    private lateinit var barangId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_update)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        editTextNama = findViewById(R.id.editTextNama)
        editTextHarga = findViewById(R.id.editTextHarga)
        editTextMerk = findViewById(R.id.editTextMerk)
        editTextStok = findViewById(R.id.editTextStok)
        imageViewBarang = findViewById(R.id.imageViewBarang)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSave = findViewById(R.id.buttonSave)

        buttonSelectImage.setOnClickListener {
            selectImage()
        }


        if (intent.hasExtra("ITEM_ID")) {
            isUpdate = true
            barangId = intent.getStringExtra("ITEM_ID") ?: ""
            loadBarangData(barangId)
            buttonSave.text = "Update Barang"
        }

        buttonSave.setOnClickListener {
            if (isUpdate) {
                updateBarang(barangId)
            } else {
                saveBarang()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageViewBarang.setImageURI(imageUri)
        }
    }

    private fun saveBarang() {
        val nama = editTextNama.text.toString()
        val harga = editTextHarga.text.toString().toDoubleOrNull()
        val merk = editTextMerk.text.toString()
        val stok = editTextStok.text.toString().toIntOrNull()


        if (harga == null || stok == null) {
            Toast.makeText(this, "Harga dan stok harus berupa angka valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {

                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val barang = Barang(nama = nama, harga = harga, merk = merk, stok = stok, imageUrl = downloadUri.toString())

                        db.collection("barang").add(barang)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Barang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error adding barang: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Silakan pilih gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBarang(barangId: String) {
        Log.d("UpdateBarang", "Update barang dengan ID: $barangId")
        val nama = editTextNama.text.toString()
        val harga = editTextHarga.text.toString().toDoubleOrNull()
        val merk = editTextMerk.text.toString()
        val stok = editTextStok.text.toString().toIntOrNull()

        if (harga == null || stok == null) {
            Toast.makeText(this, "Harga dan stok harus berupa angka valid", Toast.LENGTH_SHORT).show()
            return
        }


        db.collection("barang").document(barangId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val oldImageUrl = document.getString("imageUrl")

                    val barang = hashMapOf(
                        "nama" to nama,
                        "harga" to harga,
                        "merk" to merk,
                        "stok" to stok
                    )

                    if (imageUri != null) {
                        val newImageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")


                        newImageRef.putFile(imageUri!!)
                            .addOnSuccessListener {
                                newImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                    barang["imageUrl"] = downloadUri.toString()


                                    db.collection("barang").document(barangId).update(barang as Map<String, Any>)
                                        .addOnSuccessListener {

                                            if (oldImageUrl != null) {
                                                val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl)
                                                oldImageRef.delete()
                                                    .addOnSuccessListener {
                                                        Log.d("UpdateBarang", "Old image deleted successfully")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("UpdateBarang", "Failed to delete old image: ${e.message}")
                                                    }
                                            }
                                            Toast.makeText(this, "Barang berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Error updating barang: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {

                        db.collection("barang").document(barangId).update(barang as Map<String, Any>)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Barang berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error updating barang: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error retrieving barang: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadBarangData(barangId: String) {
        db.collection("barang").document(barangId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    editTextNama.setText(document.getString("nama"))
                    editTextHarga.setText(document.getDouble("harga")?.toString())
                    editTextMerk.setText(document.getString("merk"))
                    editTextStok.setText(document.getLong("stok")?.toString())
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null) {

                        Glide.with(this)
                            .load(imageUrl)
                            .into(imageViewBarang)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading barang data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
