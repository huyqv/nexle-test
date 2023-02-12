package wee.digital.sample.ui.fragment.splash

import androidx.annotation.IdRes
import wee.digital.sample.R
import wee.digital.sample.currentUser
import wee.digital.sample.databinding.SplashBinding
import wee.digital.sample.ui.base.BaseFragment


class SplashFragment : BaseFragment<SplashBinding>() {

    override fun onViewCreated() {
        when (currentUser) {
            null -> navigateNext(R.id.signInFragment)
            else -> navigateNext(R.id.homeFragment)
        }
    }

    private fun navigateNext(@IdRes nextDestination: Int) {
        mainNavigate(nextDestination) {
            clearBackStack()
        }
    }

}