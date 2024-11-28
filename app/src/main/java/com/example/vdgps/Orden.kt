package com.example.vdgps

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Orden(
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var inicio: Timestamp? = null, // Cambiado a Timestamp?
    var final: Timestamp? = null,  // Cambiado a Timestamp?
    var estado: String = "",
    var usuarioId: String = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        titulo = parcel.readString() ?: "",
        descripcion = parcel.readString() ?: "",
        inicio = parcel.readParcelable(Timestamp::class.java.classLoader), // Leer Timestamp
        final = parcel.readParcelable(Timestamp::class.java.classLoader), // Leer Timestamp
        estado = parcel.readString() ?: "",
        usuarioId = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(titulo)
        parcel.writeString(descripcion)
        parcel.writeParcelable(inicio, flags) // Escribir Timestamp
        parcel.writeParcelable(final, flags) // Escribir Timestamp
        parcel.writeString(estado)
        parcel.writeString(usuarioId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Orden> {
        override fun createFromParcel(parcel: Parcel): Orden = Orden(parcel)
        override fun newArray(size: Int): Array<Orden?> = arrayOfNulls(size)
    }
}