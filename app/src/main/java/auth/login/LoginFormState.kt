package ro.ubbcluj.cs.ilazar.myapp2.auth.login

data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)
