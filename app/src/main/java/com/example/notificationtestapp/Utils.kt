package com.example.notificationtestapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.BatteryManager
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap

fun drawableToBitmap(
    context: Context,
    @DrawableRes drawableRes: Int
): Bitmap {
    return AppCompatResources
        .getDrawable(context, drawableRes)!!
        .toBitmap()
}

fun Intent.batteryPercent(): Int? {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) level * 100 / scale else null
}