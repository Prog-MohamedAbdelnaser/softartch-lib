package com.softartch_lib.component.extension

import android.view.View
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

inline fun LocalDate.toFormat( parseFormat:String,date: LocalDate,local:Locale= Locale.getDefault()):String{
    Locale.setDefault(local)
    val dateFormat = SimpleDateFormat(parseFormat,local)
    return dateFormat.format(date)
}
inline fun View.show(){
    this.visibility=View.VISIBLE
}

inline fun View.hide(){
    this.visibility=View.GONE
}
