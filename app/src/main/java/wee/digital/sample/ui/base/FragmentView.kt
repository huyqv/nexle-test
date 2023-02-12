package wee.digital.sample.ui.base

import android.app.Activity
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kotlin.reflect.KClass

interface FragmentView : BaseView {

    val fragment: Fragment get() = this as Fragment

    override fun activityNavController(): NavController? {
        return baseActivity?.activityNavController()
    }

    fun onCreateView() = Unit

    /**
     * Activity util
     */
    fun <T : Activity> navigate(cls: KClass<T>) {
        fragment.requireActivity().apply {
            startActivity(Intent(this, cls.java))
        }
    }

    /**
     * [BaseView] implements
     */
    override val fragmentActivity: FragmentActivity? get() = fragment.activity

    override val lifecycleOwner: LifecycleOwner get() = fragment

    override fun show(tag: String?, dialog: () -> DialogFragment) {
        lifecycleScope.launchWhenResumed {
            baseActivity?.show(tag, dialog)
        }
    }

    fun dismissAllExceptSelf() {
        dismissAllExcept(fragment::class.java.simpleName)
    }

    /**
     * Fragment navigation uti
     */
    fun childNavigate(@IdRes actionId: Int, block: (NavBuilder.() -> Unit)? = null) {
        fragment.findNavController().navigate(actionId, block)
    }

    fun childPopBackStack(@IdRes fragmentId: Int = 0, inclusive: Boolean = false) {
        if (fragmentId != 0) {
            fragment.findNavController().popBackStack(fragmentId, inclusive)
        } else {
            fragment.findNavController().popBackStack()
        }
    }

    fun mainNavigate(@IdRes actionId: Int, block: (NavBuilder.() -> Unit)? = null) {
        baseActivity?.navigate(actionId, block)
    }

    fun mainPopBackStack(@IdRes fragmentId: Int = 0, inclusive: Boolean = false) {
        baseActivity?.popBackStack(fragmentId, inclusive)
    }


    /**
     * Back press handle
     */
    val backPressedCallback: OnBackPressedCallback?

    fun getBackPressCallBack(): OnBackPressedCallback {
        return object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackInvoked()
            }
        }
    }

    fun onBackInvoked() {
        backPressedCallback?.remove()
        when (val f = fragment) {
            is DialogFragment -> {
                f.dismissAllowingStateLoss()
            }
            else -> {
                f.requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }


}