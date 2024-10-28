package com.example.manajemenbarangapp

data class Barang(
    var id: String = "",
    var nama: String = "",
    var harga: Double = 0.0,
    var merk: String = "",
    var stok: Int = 0,
    var imageUrl: String = ""
)
