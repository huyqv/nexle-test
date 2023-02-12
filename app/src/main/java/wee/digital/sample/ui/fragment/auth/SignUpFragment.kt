package wee.digital.sample.ui.fragment.auth

import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel
import wee.digital.sample.databinding.SignUpBinding
import wee.digital.sample.ui.base.BaseFragment
import wee.digital.sample.util.isEmail
import wee.digital.sample.util.isPassword
import wee.digital.sample.util.toast

class SignUpFragment : BaseFragment<SignUpBinding>() {

    private val authVM: AuthVM by viewModel()

    override fun onViewCreated() {
        addClickListener(vb.viewSignUp)
        authVM.signUpSuccessLiveData.observe {
            mainPopBackStack()
        }
        vb.inputViewFirstName.text = "Foo"
        vb.inputViewLastName.text = "Bar"
        vb.inputViewEmail.text = "sample@email.com"
        vb.inputViewPassword.text = "123@Aa"
    }

    override fun onViewClick(v: View?) {
        when (v) {
            vb.viewSignUp -> onSignUp()
        }
    }

    /**
     *
     */
    private fun onSignUp() {
        val firstName = vb.inputViewFirstName.trimText
        if (firstName.length < 2) {
            vb.inputViewFirstName.error = "The first name must be at least 2 characters"
        }

        val lastName = vb.inputViewLastName.trimText
        if (lastName.length < 2) {
            vb.inputViewLastName.error = "The last name must be at least 2 characters"
        }

        val email = vb.inputViewEmail.trimText
        if (!email.isEmail) {
            vb.inputViewEmail.error = "Email is invalid"
        }

        val password = vb.inputViewPassword.trimText
        if (!password.isPassword) {
            vb.inputViewPassword.error =
                "The password must be between 6-18 characters and contain at least one digit, one special character, and one letter."
        }

        if (vb.inputViewFirstName.hasError || vb.inputViewLastName.hasError || vb.inputViewEmail.hasError || vb.inputViewPassword.hasError) {
            return
        }

        if (!vb.checkboxTerm.isChecked) {
            toast("Check to check box i agree...")
            return
        }

        authVM.signUp {
            it.addProperty("firstName", firstName)
            it.addProperty("lastName", lastName)
            it.addProperty("email", email)
            it.addProperty("password", password)
        }
    }


}