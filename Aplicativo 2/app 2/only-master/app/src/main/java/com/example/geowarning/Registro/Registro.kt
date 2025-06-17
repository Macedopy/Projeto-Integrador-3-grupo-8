package com.example.geowarning.Registro

import android.os.Parcel
import android.os.Parcelable

data class Registro(
    val latitude: Double,
    val longitude: Double,
    val title: String?,
    val description: String?,
    val riskLevel: String?,
    val imageBase64: String?, // <-- Mantenha esta linha
    val userId: String?,
    val timestamp: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(), // <-- Mantenha esta linha para Parcelable
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(riskLevel)
        parcel.writeString(imageBase64) // <-- Mantenha esta linha para Parcelable
        parcel.writeString(userId)
        parcel.writeString(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Registro> {
        override fun createFromParcel(parcel: Parcel): Registro {
            return Registro(parcel)
        }

        override fun newArray(size: Int): Array<Registro?> {
            return arrayOfNulls(size)
        }
    }
}