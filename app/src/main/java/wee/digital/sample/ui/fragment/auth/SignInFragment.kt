package wee.digital.sample.ui.fragment.auth

import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel
import wee.digital.sample.R
import wee.digital.sample.databinding.SignInBinding
import wee.digital.sample.ui.base.BaseFragment
import wee.digital.sample.util.isEmail

class SignInFragment : BaseFragment<SignInBinding>() {

    private val authVM: AuthVM by viewModel()

    override fun onViewCreated() {
        addClickListener(vb.viewLogin, vb.textViewRegister)
        authVM.signInSuccessLiveData.observe {
            onSignInSuccess()
        }

        vb.inputViewEmail.text = "sample@email.com"
        vb.inputViewPassword.text = "123@Aa"
    }

    override fun onViewClick(v: View?) {
        when (v) {
            vb.viewLogin -> onSignIn()
            vb.textViewRegister -> mainNavigate(R.id.signUpFragment)
        }
    }

    /**
     *
     */
    private fun onSignIn() {
        val email = vb.inputViewEmail.text
        if (!email.isEmail) {
            vb.inputViewEmail.require()
        }
        val password = vb.inputViewPassword.text
        if (password.isNullOrEmpty()) {
            vb.inputViewPassword.require()
        }
        if (vb.inputViewEmail.hasError || vb.inputViewPassword.hasError) {
            return
        }
        authVM.signIn(vb.checkboxRemember.isChecked) {
            it.addProperty("email", email)
            it.addProperty("password", password)
        }
    }

    private fun onSignInSuccess() {
        mainNavigate(R.id.homeFragment) {
            clearBackStack()
        }
    }

}