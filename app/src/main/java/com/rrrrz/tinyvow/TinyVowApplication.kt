package com.rrrrz.tinyvow

import android.app.Application
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.data.time.BusinessDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TinyVowApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        AppText.attach(this)
        applicationScope.launch {
            BusinessDay.loadStartHour(this@TinyVowApplication)
        }
    }
}
