package wee.digital.sample

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import wee.digital.sample.data.model.User
import wee.digital.sample.di.allModules
import wee.digital.sample.util.SharedPref
import wee.digital.sample.util.toJsonObject

lateinit var app: App private set

val pref by lazy {
    SharedPref(BuildConfig.APPLICATION_ID)
}

var currentUser: User? = null
    set(value) {
        pref.put("user", value.toJsonObject().toString())
    }
    get() {
        if (field == null) {
            field = pref.obj("user")
        }
        return field
    }

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(allModules)
        }
    }

}





