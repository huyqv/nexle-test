package wee.digital.sample.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(),
    BaseView {

    protected val vb: VB by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        viewBinding(layoutInflater) as VB
    }


    /**
     * [AppCompatActivity] implements
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        hideKeyboard()
        setContentView(vb.root)
        onViewCreated()
    }


    /**
     * [BaseView] implements
     */
    override fun activityNavController(): NavController? {
        val s = "activityNavController is not implement in ${this::class.java.simpleName}"
        throw NullPointerException(s)
    }

    final override val fragmentActivity: FragmentActivity? get() = this

    final override val lifecycleOwner: LifecycleOwner get() = this

    private var dialogAnimationTag: String? = null

    final override fun show(tag: String?, dialog: () -> DialogFragment) {
        lifecycleScope.launchWhenResumed {
            try {
                if (dialogAnimationTag != null && dialogAnimationTag == tag) {
                    return@launchWhenResumed
                }
                val sfm = (baseActivity as? FragmentActivity)?.supportFragmentManager
                    ?: throw NullPointerException("supportFragmentManager not found")
                val existFragment: Fragment? = if (!tag.isNullOrEmpty()) {
                    sfm.findFragmentByTag(tag)
                } else {
                    null
                }
                if (existFragment != null) {
                    throw NullPointerException("${dialog::class.simpleName} dialog was shown, tag: ${tag}")
                }
                dialogAnimationTag = tag
                launch {
                    delay(300)
                    dialogAnimationTag = null
                }
                dialog().show(sfm, tag)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }


    /**
     * SupportFragmentManager
     */
    private fun <T : Fragment> FragmentManager.findFragment(cls: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return this.fragments.find { it::class.simpleName == cls.simpleName } as? T?
    }

    private fun <T : Fragment> FragmentManager.findFragment(fragment: T): T? {
        return findFragment(fragment::class)
    }

}


