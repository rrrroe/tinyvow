package com.rrrrz.tinyvow

import android.app.Application
import com.rrrrz.tinyvow.i18n.AppText

class TinyVowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppText.attach(this)
    }
}
