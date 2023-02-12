package wee.digital.sample.util

val String?.isPassword: Boolean
    get() {
        val regex =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~\$^+=<>]).{6,18}\$".toRegex()
        return this?.matches(regex) ?: false
    }

val String?.isEmail: Boolean
    get() {
        this ?: return false
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this.trim()).matches()
    }