package com.example.manajemenbarangapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class BarangAdapter(private val barangList: MutableList<Barang>, private val context: Context) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {


    inner class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewMerk: TextView = itemView.findViewById(R.id.textViewMerk)
        val textViewStok: TextView = itemView.findViewById(R.id.textViewStok)
        val buttonDownload: Button = itemView.findViewById(R.id.buttonDownload)
        val buttonUpdate: Button = itemView.findViewById(R.id.buttonUpdate)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        init {
            buttonDownload.setOnClickListener {
                val imageUrl = barangList[adapterPosition].imageUrl
                downloadImage(imageUrl, itemView.context)
            }

            buttonUpdate.setOnClickListener {
                // Navigate to CreateUpdateActivity with the selected item's data
                val intent = Intent(itemView.context, CreateUpdateActivity::class.java).apply {
                    putExtra("ITEM_ID", barangList[adapterPosition].id)
                    putExtra("ITEM_NAME", barangList[adapterPosition].nama)
                    putExtra("ITEM_PRICE", barangList[adapterPosition].harga)
                    putExtra("ITEM_MERK", barangList[adapterPosition].merk)
                    putExtra("ITEM_STOK", barangList[adapterPosition].stok)
                    putExtra("ITEM_IMAGE_URL", barangList[adapterPosition].imageUrl)
                }
                itemView.context.startActivity(intent)
            }

            buttonDelete.setOnClickListener {

                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes") { dialog, which -> deleteItem(adapterPosition) }
                    .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }

        private fun deleteItem(position: Int) {
            val item = barangList[position]
            val itemId = item.id
            val imageUrl = item.imageUrl

            val databaseReference = FirebaseFirestore.getInstance().collection("barang")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)


            storageReference.delete().addOnSuccessListener {
                databaseReference.document(itemId).delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        barangList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, barangList.size)
                        Toast.makeText(itemView.context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(itemView.context, "Failed to delete item from Firestore", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(itemView.context, "Failed to delete image", Toast.LENGTH_SHORT).show()
            }
        }



        private fun downloadImage(imageUrl: String, context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(input)


                    saveImageToGallery(bitmap, context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun saveImageToGallery(bitmap: Bitmap, context: Context) {
            val savedImageURL: String = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "Downloaded Image",
                "Image downloaded from the app"
            )

            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Image downloaded to gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = barangList[position]
        Glide.with(holder.imageView.context)
            .load(barang.imageUrl)
            .into(holder.imageView)

        holder.textViewName.text = "${barang.nama}"
        holder.textViewPrice.text = "${barang.harga}"
        holder.textViewMerk.text = "${barang.merk}"
        holder.textViewStok.text ="${barang.stok}"
    }

    override fun getItemCount(): Int = barangList.size
}
