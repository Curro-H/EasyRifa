package com.easyrifa.ui.screen.raffle

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyrifa.data.repository.RaffleRepository
import com.easyrifa.domain.usecase.share.CopyRaffleImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEditRaffleUiState(
    val name: String = "",
    val minNumber: String = "1",
    val maxNumber: String = "100",
    val imagePath: String? = null,
    val nameError: String? = null,
    val rangeError: String? = null,
    val isSaving: Boolean = false,
    val savedRaffleId: Long? = null
)

@HiltViewModel
class CreateEditRaffleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val raffleRepository: RaffleRepository,
    private val copyRaffleImageUseCase: CopyRaffleImageUseCase
) : ViewModel() {

    private val editRaffleId: Long? = savedStateHandle["raffleId"]

    private val _uiState = MutableStateFlow(CreateEditRaffleUiState())
    val uiState: StateFlow<CreateEditRaffleUiState> = _uiState.asStateFlow()

    init {
        editRaffleId?.let { id ->
            viewModelScope.launch {
                val raffle = raffleRepository.getRaffleById(id).first()
                raffle?.let {
                    _uiState.update { state ->
                        state.copy(
                            name = it.name,
                            minNumber = it.minNumber.toString(),
                            maxNumber = it.maxNumber.toString(),
                            imagePath = it.imagePath
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onMinChange(value: String) = _uiState.update { it.copy(minNumber = value, rangeError = null) }
    fun onMaxChange(value: String) = _uiState.update { it.copy(maxNumber = value, rangeError = null) }

    fun onImagePicked(uri: Uri) = viewModelScope.launch {
        // Remove old image file if replacing
        _uiState.value.imagePath?.let { copyRaffleImageUseCase.deleteImage(it) }

        copyRaffleImageUseCase.execute(uri).fold(
            onSuccess = { path -> _uiState.update { it.copy(imagePath = path) } },
            onFailure = { /* ignore, image is optional */ }
        )
    }

    fun onRemoveImage() {
        _uiState.value.imagePath?.let { copyRaffleImageUseCase.deleteImage(it) }
        _uiState.update { it.copy(imagePath = null) }
    }

    fun save() = viewModelScope.launch {
        val state = _uiState.value
        val name = state.name.trim()
        val min = state.minNumber.toIntOrNull()
        val max = state.maxNumber.toIntOrNull()

        var hasError = false

        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre no puede estar vacío") }
            hasError = true
        }
        if (min == null || max == null || min >= max) {
            _uiState.update { it.copy(rangeError = "El máximo debe ser mayor que el mínimo") }
            hasError = true
        } else if (max - min + 1 > 10_000) {
            _uiState.update { it.copy(rangeError = "El rango no puede superar 10.000 números") }
            hasError = true
        }

        if (hasError) return@launch

        _uiState.update { it.copy(isSaving = true) }

        if (editRaffleId == null) {
            val id = raffleRepository.createRaffle(name, min!!, max!!, state.imagePath)
            _uiState.update { it.copy(isSaving = false, savedRaffleId = id) }
        } else {
            val existing = raffleRepository.getRaffleById(editRaffleId).first()!!
            raffleRepository.updateRaffle(
                existing.copy(name = name, minNumber = min!!, maxNumber = max!!, imagePath = state.imagePath)
            )
            _uiState.update { it.copy(isSaving = false, savedRaffleId = editRaffleId) }
        }
    }
}
