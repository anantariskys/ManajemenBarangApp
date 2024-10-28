package com.example.manajemenbarangapp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var barangAdapter: BarangAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAdd: Button
    private var barangList: MutableList<Barang> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitiy_main)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        buttonAdd = findViewById(R.id.buttonAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        barangAdapter = BarangAdapter(barangList,this)
        recyclerView.adapter = barangAdapter

        buttonAdd.setOnClickListener {
            val intent = Intent(this, CreateUpdateActivity::class.java)
            startActivity(intent)
        }

        loadBarangData()
    }

    override fun onResume() {
        super.onResume()

        loadBarangData()
    }


    private fun loadBarangData() {
        db.collection("barang").get()
            .addOnSuccessListener { documents ->
                barangList.clear()
                for (document in documents) {
                    val barang = document.toObject(Barang::class.java)
                    barang.id = document.id // Simpan ID dokumen
                    barangList.add(barang)
                }
                barangAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading barang: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onBarangClick(barang: Barang) {
        val intent = Intent(this, CreateUpdateActivity::class.java)
        intent.putExtra("barangId", barang.id)
        startActivity(intent)
    }
}
