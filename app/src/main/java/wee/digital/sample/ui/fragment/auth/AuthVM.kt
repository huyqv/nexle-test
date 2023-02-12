package wee.digital.sample.ui.fragment.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wee.digital.sample.currentUser
import wee.digital.sample.data.ApiService
import wee.digital.sample.ui.base.SingleLiveData
import wee.digital.sample.util.toast

class AuthVM(private val apiService: ApiService) : ViewModel() {

    val signInSuccessLiveData = SingleLiveData<Boolean>()
    val signUpSuccessLiveData = SingleLiveData<Boolean>()
    val signOutSuccessLiveData = SingleLiveData<Boolean>()

    fun signIn(isRemember: Boolean, block: (JsonObject) -> Unit) {
        val body = JsonObject()
        block(body)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = apiService.signIn(body)
                if (isRemember) {
                    currentUser = user
                }
                signInSuccessLiveData.postValue(true)
            } catch (e: Exception) {
                toast(e.message)
            }
        }
    }

    fun signUp(block: (JsonObject) -> Unit) {
        val body = JsonObject()
        block(body)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = apiService.signUp(body)
                if (user.id.isNullOrEmpty()) {
                    throw NullPointerException()
                } else {
                    signUpSuccessLiveData.postValue(true)
                }

            } catch (e: Exception) {
                toast(e.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = currentUser?.token ?: throw NullPointerException()
                val response = apiService.signOut(token)
                if (response.code() == 200) {
                    currentUser = null
                    signOutSuccessLiveData.postValue(true)
                } else {
                    toast(response.message())
                }

            } catch (e: Exception) {
                toast(e.message)
            }
        }
    }

}