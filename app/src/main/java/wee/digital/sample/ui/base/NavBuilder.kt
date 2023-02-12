package wee.digital.sample.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import wee.digital.sample.R


class NavBuilder(private val controller: NavController) {

    private val options = NavOptions.Builder()

    private var args: Bundle? = null

    private var extras: Navigator.Extras? = null

    fun clearBackStack() {
        options.setLaunchSingleTop(true)
        options.setPopUpTo(controller.graph.id, true)
    }

    fun setLaunchSingleTop() {
        options.setLaunchSingleTop(true)
    }

    fun setPopUpTo(@IdRes fragmentId: Int, inclusive: Boolean = false) {
        options.setLaunchSingleTop(true)
        options.setPopUpTo(fragmentId, inclusive)
    }

    fun setExtras(vararg sharedElements: Pair<View, String>) {
        extras = FragmentNavigatorExtras(*sharedElements)
    }

    fun sharedElements(vararg elements: View) {
        val list = mutableListOf<Pair<View, String>>()
        elements.forEach { list.add(Pair(it, it.transitionName)) }
        extras = FragmentNavigatorExtras(*list.toTypedArray())
        setNoneAnim()
    }

    fun setBundle(vararg pairs: Pair<String, Any?>) {
        args = bundleOf(*pairs)
    }


    fun setNoneAnim() {
        options.apply {
            setEnterAnim(0)
            setExitAnim(0)
            setPopEnterAnim(0)
            setPopExitAnim(0)
        }
    }


    fun setVerticalAnim() {
        options.apply {
            setEnterAnim(R.anim.vertical_enter)
            setExitAnim(R.anim.vertical_exit)
            setPopEnterAnim(R.anim.vertical_pop_enter)
            setPopExitAnim(R.anim.vertical_pop_exit)
        }
    }

    fun navigate(@IdRes actionId: Int) {
        controller.navigate(actionId, args, options.build(), extras)
    }

    fun remove(@IdRes vararg actionId: Int) {
        kotlin.runCatching {
            val queue: ArrayDeque<NavBackStackEntry> = controller.backQueue
            queue.toList().filter {
                actionId.contains(it.destination.id)
            }.iterator().forEach {
                queue.remove(it)
            }
        }
    }

}