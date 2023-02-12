package wee.digital.sample.ui.base

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.view.*
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import wee.digital.sample.app
import wee.digital.sample.util.ViewClickListener
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

interface BaseView {

    val fragmentActivity: FragmentActivity?

    val baseActivity: BaseActivity<*>? get() = fragmentActivity as? BaseActivity<*>

    val lifecycleOwner: LifecycleOwner

    val viewLifecycle: Lifecycle get() = lifecycleOwner.lifecycle

    val lifecycleScope: LifecycleCoroutineScope get() = lifecycleOwner.lifecycleScope

    fun activityNavController(): NavController?

    fun onViewCreated()

    fun addObserve(observer: LifecycleObserver): LifecycleObserver {
        lifecycleOwner.lifecycle.addObserver(observer)
        return observer
    }

    fun whenResumed(block: suspend CoroutineScope.() -> Unit): Job {
        return lifecycleScope.launchWhenResumed(block)
    }


    /**
     * Activity navigate
     */
    fun <T : Activity> Activity.navigate(cls: KClass<T>) {
        this.startActivity(Intent(this, cls.java))
    }

    /**
     * Dialog utils
     */
    fun dismissAllDialogs() {
        lifecycleScope.launchWhenResumed {
            val activity = fragmentActivity ?: return@launchWhenResumed
            activity.supportFragmentManager.fragments.forEach { fragment ->
                if (fragment is DialogFragment) {
                    fragment.dismissAllowingStateLoss()
                }
            }
        }

    }

    fun dismissDialog(tag: String) {
        lifecycleScope.launchWhenResumed {
            val activity = fragmentActivity ?: return@launchWhenResumed
            val fragment = activity.supportFragmentManager.findFragmentByTag(tag) as? DialogFragment
            fragment?.dialog?.dismiss()
        }
    }

    fun dismissAllExcept(fragmentName: String) {
        lifecycleScope.launchWhenResumed {
            val activity = fragmentActivity ?: return@launchWhenResumed
            activity.supportFragmentManager.fragments.forEach { fragment ->
                if (fragment is DialogFragment && fragment::class.java.name != fragmentName) {
                    fragment.dismissAllowingStateLoss()
                }
            }
        }
    }

    fun showAlertDialog(block: (AlertDialog.Builder.() -> Unit)? = null) {

        lifecycleScope.launchWhenResumed {
            fragmentActivity?.also {
                val dialog = AlertDialog.Builder(it)
                block?.invoke(dialog)
                dialog.create().show()
            }
        }
    }

    fun show(tag: String?, dialog: () -> DialogFragment)


    /**
     * Fragment navigation uti
     */
    fun NavController?.navigate(@IdRes actionId: Int, block: (NavBuilder.() -> Unit)? = null) {
        this ?: return
        NavBuilder(this).also {
            it.setVerticalAnim()
            block?.invoke(it)
            it.navigate(actionId)
        }
    }

    fun navigate(@IdRes actionId: Int, block: (NavBuilder.() -> Unit)? = null) {
        activityNavController().navigate(actionId, block)
    }

    fun popBackStack(@IdRes fragmentId: Int, inclusive: Boolean = false) {
        activityNavController()?.popBackStack(fragmentId, inclusive)
    }

    fun <T> navResultLiveData(key: String? = null): MutableLiveData<T>? {
        return activityNavController()?.currentBackStackEntry?.savedStateHandle?.getLiveData(
            key
                ?: ""
        )
    }

    fun <T> setNavResult(key: String? = null, result: T) {
        activityNavController()
            ?.previousBackStackEntry
            ?.savedStateHandle?.set(key ?: "", result)
    }

    fun <T> setNavResult(result: T) {
        activityNavController()
            ?.previousBackStackEntry
            ?.savedStateHandle?.set("", result)
    }


    /**
     * LiveData utils
     */
    fun <T> LiveData<T>.observe(block: (T) -> Unit) {
        removeObservers(lifecycleOwner)
        observe(lifecycleOwner, Observer(block))
    }

    fun <T> LiveData<T>.removeObservers() {
        removeObservers(lifecycleOwner)
    }


    /**
     * Keyboard utils
     */
    fun requireWindow(block: (Window) -> Unit) {
        lifecycleScope.launchWhenResumed {
            fragmentActivity?.window?.also {
                val window: Window = when (this@BaseView) {
                    is DialogFragment -> dialog?.window
                    is Fragment -> activity?.window
                    is Activity -> window
                    else -> null
                } ?: return@launchWhenResumed
                block(window)
            }
        }
    }

    fun hideKeyboard() {
        requireWindow {
            val controller = WindowInsetsControllerCompat(it, it.decorView)
            controller.hide(WindowInsetsCompat.Type.ime())
        }
    }

    fun showKeyboard() {
        requireWindow {
            val controller = WindowInsetsControllerCompat(it, it.decorView)
            controller.show(WindowInsetsCompat.Type.ime())
        }
    }

    fun inputModeAdjustNothing() {
        requireWindow {
            val frags = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            it.setSoftInputMode(frags)
        }
    }

    fun inputModeAdjustPan() {
        requireWindow {
            val flags = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            it.setSoftInputMode(flags)
        }
    }

    fun inputModeAdjustResize() {
        requireWindow {
            @Suppress("DEPRECATION")
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    /**
     * [View] utils
     */
    fun <VB : ViewBinding> viewBinding(
        inflater: LayoutInflater,
        container: ViewGroup? = null,
        attachToParent: Boolean = false,
    ): VB {
        var genericVBClass = javaClass
        var cls: Class<VB>? = null
        while (cls !is Class<VB>) {
            val type = genericVBClass.genericSuperclass as? ParameterizedType
            cls = type?.actualTypeArguments?.get(0) as? Class<VB>
            if (cls == null) {
                genericVBClass = genericVBClass.superclass as Class<BaseView>
            }
        }
        val method = cls.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        @Suppress("UNCHECKED_CAST")
        return method.invoke(null, inflater, container, attachToParent) as VB
    }

    fun View.toSharedElement(): Pair<View, String> {
        return Pair(this, this.transitionName)
    }

    fun View.show() {
        if (visibility != View.VISIBLE) visibility = View.VISIBLE
    }

    fun View.isShow(show: Boolean?) {
        visibility = if (show == true) View.VISIBLE
        else View.INVISIBLE
    }

    fun View.hide() {
        if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
    }

    fun View.isHide(hide: Boolean?) {
        visibility = if (hide == true) View.INVISIBLE
        else View.VISIBLE
    }

    fun View.gone() {
        if (visibility != View.GONE) visibility = View.GONE
    }

    fun View.isGone(gone: Boolean?) {
        visibility = if (gone == true) View.GONE
        else View.VISIBLE
    }

    fun View?.post(delayed: Long, runnable: Runnable) {
        this?.postDelayed(runnable, delayed)
    }

    fun View?.addClickListener(delayedInterval: Long, listener: ((View) -> Unit)? = null) {
        this ?: return
        if (listener == null) {
            setOnClickListener(null)
            if (this is EditText) this.post {
                isFocusable = true
                isCursorVisible = true
                setActivated(true)
                setPressed(true)
                setTextIsSelectable(true)
            }
            return
        }
        isClickable = true
        if (this is EditText) {
            isFocusable = false
            isCursorVisible = false
        }
        setOnClickListener(object : ViewClickListener(delayedInterval, id) {
            override fun onClicks(v: View) {
                listener(v)
            }
        })

    }

    fun View?.addClickListener(listener: ((View) -> Unit)? = null) {
        this@addClickListener.addClickListener(0, listener)
    }

    fun TextView.setHyperText(@StringRes res: Int, vararg args: Any?) {
        setHyperText(string(res), * args)
    }

    fun TextView.setHyperText(s: String?, vararg args: Any?) {
        post {
            text = try {
                when {
                    s.isNullOrEmpty() -> null
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(
                        s.format(*args),
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    else -> {
                        @Suppress("DEPRECATION")
                        Html.fromHtml(s.format(*args))
                    }
                }
            } catch (e: Throwable) {
                s
            }
        }
    }

    fun CompoundButton.onCheckedChange(block: ((Boolean) -> Unit)?) {
        this.setOnCheckedChangeListener { _, isChecked -> block?.invoke(isChecked) }
    }

    fun addClickListener(vararg views: View?) {
        val listener = object : ViewClickListener() {
            override fun onClicks(v: View) {
                onViewClick(v)
            }
        }
        views.forEach { it?.setOnClickListener(listener) }
    }

    fun onViewClick(v: View?) = Unit


    fun show(vararg views: View) {
        for (v in views) v.show()
    }

    fun hide(vararg views: View) {
        for (v in views) v.hide()
    }

    fun gone(vararg views: View) {
        for (v in views) v.gone()
    }

    /**
     * Resources utils
     */
    fun color(@ColorRes res: Int): Int {
        return ContextCompat.getColor(app, res)
    }

    fun string(@StringRes res: Int, vararg args: Any?): String {
        return try {
            String.format(app.getString(res), *args)
        } catch (ignore: Exception) {
            ""
        }
    }

    fun drawable(@DrawableRes res: Int): Drawable? {
        return ContextCompat.getDrawable(app, res)
    }

    fun dimen(@DimenRes res: Int): Int {
        return app.resources.getDimension(res).toInt()
    }

    /**
     * [String] utils
     */
    fun String.br(): String = "$this<br>"

    fun String.color(hexString: String): String {
        return "<font color=$hexString>$this</font>"
    }

    fun String.color(@ColorInt color: Int): String {
        val hexString = "#${Integer.toHexString(color and 0x00ffffff)}"
        return this.color(hexString)
    }

    fun String.colorRes(@ColorRes res: Int): String {
        return this.color(ContextCompat.getColor(app, res))
    }

    fun String.bold(): String {
        return "<b>$this</b>"
    }


}