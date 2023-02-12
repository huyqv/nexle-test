package wee.digital.sample.util

import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wee.digital.sample.app

var toast: Toast? = null

fun toast(message: String?) {
    message ?: return
    CoroutineScope(Dispatchers.Main).launch {
        kotlin.runCatching {
            toast?.cancel()
            toast = Toast.makeText(app.applicationContext, message, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }
}