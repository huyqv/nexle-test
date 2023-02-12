package wee.digital.sample.ui.fragment.home

import org.koin.androidx.viewmodel.ext.android.viewModel
import wee.digital.sample.R
import wee.digital.sample.currentUser
import wee.digital.sample.databinding.HomeBinding
import wee.digital.sample.ui.base.BaseFragment
import wee.digital.sample.ui.fragment.auth.AuthVM

class HomeFragment : BaseFragment<HomeBinding>() {

    private val authVM: AuthVM by viewModel()

    override fun onViewCreated() {
        vb.textViewSignOut.addClickListener {
            authVM.signOut()
        }
        authVM.signOutSuccessLiveData.observe {
            mainNavigate(R.id.signInFragment) {
                clearBackStack()
            }
        }
        bindUser()

    }

    /**
     *
     */
    private fun bindUser() {
        vb.textViewName.text = currentUser?.displayName
    }
}