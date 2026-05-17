package com.shaalevikas.app.ui.admin

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.shaalevikas.app.data.model.Need
import com.shaalevikas.app.data.repository.AuthRepository
import com.shaalevikas.app.data.repository.NeedsRepository
import com.shaalevikas.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateNeedState {
    object Idle : CreateNeedState()
    object Loading : CreateNeedState()
    object Success : CreateNeedState()
    data class Error(val message: String) : CreateNeedState()
    data class AiTitleSuggestion(
        val title: String
    ) : CreateNeedState()
    data class AiDescriptionSuggestion(
        val description: String,
        val costEstimate: String
    ) : CreateNeedState()
}

@HiltViewModel
class CreateNeedViewModel @Inject constructor(
    private val needsRepository: NeedsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = SingleLiveEvent<CreateNeedState>()
    val state: LiveData<CreateNeedState> = _state

    private var isPublishing = false

    private val generativeModel by lazy {
        try {
            GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey    = "AIzaSyCkw94i0xkOM0eesr_cs-b0eS4_j08ueMw"
            )
        } catch (e: Exception) {
            null
        }
    }

    fun generateAiTitle(partialTitle: String) {
        if (partialTitle.isBlank()) return

        if (generativeModel == null) {
            _state.value = CreateNeedState.AiTitleSuggestion(
                generateFallbackTitle(partialTitle)
            )
            return
        }

        _state.value = CreateNeedState.Loading

        viewModelScope.launch {
            try {
                val prompt = """
                    Rural school need title for India alumni app.
                    Input: "$partialTitle"
                    Give ONE clear title max 8 words.
                    Reply with ONLY the title.
                """.trimIndent()

                val response = generativeModel!!.generateContent(prompt)
                val title = response.text?.trim()
                    ?.removeSurrounding("\"")
                    ?: partialTitle

                _state.postValue(
                    CreateNeedState.AiTitleSuggestion(title)
                )
            } catch (e: Exception) {
                _state.postValue(
                    CreateNeedState.AiTitleSuggestion(
                        generateFallbackTitle(partialTitle)
                    )
                )
            }
        }
    }

    fun generateAiDescription(
        title: String,
        categories: List<String>,
        urgency: String
    ) {
        val categoryText = categories.joinToString(", ")

        if (generativeModel == null) {
            _state.value = CreateNeedState.AiDescriptionSuggestion(
                description  = generateFallbackDescription(
                    title, categoryText, urgency
                ),
                costEstimate = "15000"
            )
            return
        }

        _state.value = CreateNeedState.Loading

        viewModelScope.launch {
            try {
                val prompt = """
                    Rural school need for India alumni donation app.
                    Title: $title
                    Category: $categoryText
                    Urgency: $urgency
                    Write 2 sentences about the problem and impact.
                    Format:
                    DESCRIPTION: [text]
                    COST: [number only]
                """.trimIndent()

                val response = generativeModel!!.generateContent(prompt)
                val text = response.text ?: ""

                val desc = if (text.contains("DESCRIPTION:"))
                    text.substringAfter("DESCRIPTION:")
                        .substringBefore("COST:").trim()
                else text.lines().firstOrNull()?.trim()
                    ?: generateFallbackDescription(
                        title, categoryText, urgency
                    )

                val cost = if (text.contains("COST:"))
                    text.substringAfter("COST:")
                        .trim()
                        .filter { it.isDigit() }
                        .take(8)
                else "15000"

                _state.postValue(
                    CreateNeedState.AiDescriptionSuggestion(
                        description  = desc.ifEmpty {
                            generateFallbackDescription(
                                title, categoryText, urgency
                            )
                        },
                        costEstimate = cost.ifEmpty { "15000" }
                    )
                )
            } catch (e: Exception) {
                _state.postValue(
                    CreateNeedState.AiDescriptionSuggestion(
                        description  = generateFallbackDescription(
                            title, categoryText, urgency
                        ),
                        costEstimate = "15000"
                    )
                )
            }
        }
    }

    fun publishNeed(
        title: String,
        categories: List<String>,
        description: String,
        costEstimate: Double,
        targetAmount: Double,
        urgency: String,
        photoUri: Uri?,
        contentResolver: ContentResolver
    ) {
        if (isPublishing) return
        isPublishing = true
        _state.value = CreateNeedState.Loading

        viewModelScope.launch {
            try {
                var photoUrl = ""
                if (photoUri != null) {
                    val stream = contentResolver
                        .openInputStream(photoUri)!!
                    val result = needsRepository.uploadPhoto(
                        stream,
                        "needs/${System.currentTimeMillis()}.jpg"
                    )
                    if (result.isSuccess) {
                        photoUrl = result.getOrDefault("")
                    }
                }

                val need = Need(
                    schoolId       = "school_001",
                    title          = title,
                    category       = categories.joinToString(", "),
                    description    = description,
                    costEstimate   = costEstimate,
                    targetAmount   = targetAmount,
                    urgency        = urgency,
                    beforePhotoUrl = photoUrl
                )

                val result = needsRepository.createNeed(need)

                if (result.isSuccess) {
                    _state.postValue(CreateNeedState.Success)
                } else {
                    _state.postValue(
                        CreateNeedState.Error(
                            result.exceptionOrNull()?.message
                                ?: "Failed to publish"
                        )
                    )
                }
            } catch (e: Exception) {
                _state.postValue(
                    CreateNeedState.Error(
                        e.message ?: "Failed"
                    )
                )
            } finally {
                isPublishing = false
            }
        }
    }

    fun markFulfilled(
        needId: String,
        afterPhotoUri: Uri?,
        contentResolver: ContentResolver
    ) {
        _state.value = CreateNeedState.Loading

        viewModelScope.launch {
            try {
                var afterUrl = ""
                if (afterPhotoUri != null) {
                    val stream = contentResolver
                        .openInputStream(afterPhotoUri)!!
                    val result = needsRepository.uploadPhoto(
                        stream,
                        "fulfilled/${System.currentTimeMillis()}.jpg"
                    )
                    if (result.isSuccess) {
                        afterUrl = result.getOrDefault("")
                    }
                }

                val result = needsRepository
                    .markFulfilled(needId, afterUrl)

                if (result.isSuccess) {
                    _state.postValue(CreateNeedState.Success)
                } else {
                    _state.postValue(
                        CreateNeedState.Error(
                            result.exceptionOrNull()?.message
                                ?: "Failed"
                        )
                    )
                }
            } catch (e: Exception) {
                _state.postValue(
                    CreateNeedState.Error(
                        e.message ?: "Failed"
                    )
                )
            }
        }
    }

    fun resetState() {
        isPublishing = false
    }

    private fun generateFallbackTitle(input: String): String {
        return when {
            input.lowercase().contains("roof") ->
                "Leaking Roof Repair Needed"
            input.lowercase().contains("toilet") ||
                    input.lowercase().contains("sanit") ->
                "Sanitation Facility Repair Required"
            input.lowercase().contains("desk") ||
                    input.lowercase().contains("furni") ->
                "Classroom Furniture Replacement Needed"
            input.lowercase().contains("paint") ||
                    input.lowercase().contains("wall") ->
                "Classroom Wall Painting Required"
            else -> "$input Repair Needed"
        }
    }

    private fun generateFallbackDescription(
        title: String,
        category: String,
        urgency: String
    ): String {
        return "The school requires $urgency attention for " +
                "$title related to $category. This issue is " +
                "affecting the students learning environment and " +
                "needs immediate support from alumni to resolve."
    }
}
