package com.kenowa.kingsgame

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
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

fun getAge(year: Int, month: Int, day: Int): String? {
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