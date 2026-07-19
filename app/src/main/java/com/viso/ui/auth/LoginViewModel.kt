package com.viso.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.viso.data.auth.AuthRepository
import com.viso.data.auth.AuthState
import com.viso.domain.usecase.SyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncUseCase: SyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    val authState = authRepository.authState

    fun onGoogleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            if (account == null) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val idToken = account.idToken
            if (idToken == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(LoginEvent.ShowError("Erro ao obter token de autenticação"))
                return@launch
            }

            val currentUser = authRepository.getCurrentUser()
            val isAnonymous = currentUser?.isAnonymous == true

            val result = if (isAnonymous) {
                authRepository.linkWithGoogle(idToken)
            } else {
                authRepository.signInWithGoogle(idToken)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)

            result.fold(
                onSuccess = {
                    syncUseCase.pullFromCloud()
                    _events.emit(LoginEvent.NavigateToHome)
                },
                onFailure = { error ->
                    val errorMessage = when {
                        error.message?.contains("12500") == true -> 
                            "Erro no Google Sign-In. Verifique se o SHA-1 está configurado no Firebase Console"
                        error.message?.contains("7") == true ->
                            "Erro de conexão. Verifique sua internet"
                        error.message?.contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") == true -> {
                            authRepository.signOut()
                            authRepository.signInWithGoogle(idToken)
                                .onSuccess {
                                    syncUseCase.pullFromCloud()
                                    _events.emit(LoginEvent.NavigateToHome)
                                }
                            return@launch
                        }
                        else -> error.message ?: "Erro ao fazer login com Google"
                    }
                    _events.emit(LoginEvent.ShowError(errorMessage))
                }
            )
        }
    }

    fun onGoogleSignInError(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = false)
            _events.emit(LoginEvent.ShowError(message))
        }
    }

    fun onLoginCancelled() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.signInAnonymously()

            _uiState.value = _uiState.value.copy(isLoading = false)

            result.fold(
                onSuccess = {
                    syncUseCase.pullFromCloud()
                    _events.emit(LoginEvent.NavigateToHome)
                },
                onFailure = { error ->
                    val errorMessage = when {
                        error.message?.contains("ADMIN_ONLY_OPERATION") == true ->
                            "Login anônimo não habilitado. Habilite em: Firebase Console > Authentication > Sign-in method > Anonymous"
                        error.message?.contains("NETWORK") == true ||
                        error.message?.contains("network") == true ->
                            "Erro de conexão. Verifique sua internet"
                        else -> "Erro ao fazer login: ${error.message}"
                    }
                    _events.emit(LoginEvent.ShowError(errorMessage))
                }
            )
        }
    }

    fun skipLogin() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToHome)
        }
    }
}
