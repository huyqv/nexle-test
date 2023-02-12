package wee.digital.sample.ui.main

import androidx.navigation.NavController
import androidx.navigation.findNavController
import wee.digital.sample.R
import wee.digital.sample.databinding.MainBinding
import wee.digital.sample.ui.base.BaseActivity


class MainActivity : BaseActivity<MainBinding>() {

    override fun activityNavController(): NavController? {
        return findNavController(R.id.fragmentContainerView)
    }

    override fun onViewCreated() {
        inputModeAdjustResize()
    }

}






