package com.kenowa.kingsgame

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.*

fun showMessage(context: Context, mensaje: String) {
    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getAge(fecha: String): String? {
    val year = fecha.substring(0, 4).toInt()
    val month = fecha.substring(5, 7).toInt()
    val day = fecha.substring(8, 10).toInt()
    val dob = Calendar.getInstance()
    val today = Calendar.getInstance()
    dob[year, month] = day
    var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
    if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
        age--
    }
    val ageInt = age
    return ageInt.toString()
}

fun hideProgressBar(progressBar: ProgressBar) {
    progressBar.visibility = View.GONE
}

fun showProgressBar(progressBar: ProgressBar) {
    progressBar.visibility = View.VISIBLE
}

fun referenceDatabase(nombre: String): DatabaseReference {
    val database = FirebaseDatabase.getInstance()
    return database.getReference(nombre)
}

fun isUser(user: String?, id: String?): Boolean {
    if (user == id) {
        return true
    }
    return false
}

fun visualizeSpinner(spinner: Spinner, lista: Int, context: Context): ArrayAdapter<CharSequence> {
    return ArrayAdapter.createFromResource(
        context,
        lista,
        R.layout.spinner_item
    ).also { adapter ->
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }
}

@SuppressLint("SimpleDateFormat")
@RequiresApi(Build.VERSION_CODES.N)
fun saveDate(tv_fecha: TextView, tv_day: TextView?, context: Context) {
    val cal = Calendar.getInstance()
    val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = "yyyy-MM-dd"
            val simpleDateFormat = SimpleDateFormat(format, Locale.US)
            val fechaRegistro = simpleDateFormat.format(cal.time).toString()
            tv_fecha.text = fechaRegistro

            val day = android.text.format.DateFormat.format("EEEE", cal)
            tv_day?.text = day
        }

    DatePickerDialog(
        context,
        dateSetListener,
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun savePhotoInStorage(imageBitmap: Bitmap, id: String, myRef: DatabaseReference) {
    val mStorage = FirebaseStorage.getInstance()
    val photoRef = mStorage.reference.child(id)
    val baos = ByteArrayOutputStream()
    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

    val data = baos.toByteArray()
    val uploadTask = photoRef.putBytes(data)
    uploadPhoto(uploadTask, photoRef, id, myRef)
}

private fun uploadPhoto(
    uploadTask: UploadTask,
    photoRef: StorageReference,
    id: String,
    myRef: DatabaseReference
) {
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        photoRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val childUpdate = HashMap<String, Any>()
            val urlPhoto = task.result.toString()
            childUpdate["foto"] = urlPhoto
            myRef.child(id).updateChildren(childUpdate)
        }
    }
}