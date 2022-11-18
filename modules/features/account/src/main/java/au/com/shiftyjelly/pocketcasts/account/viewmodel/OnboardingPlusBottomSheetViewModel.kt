package au.com.shiftyjelly.pocketcasts.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.shiftyjelly.pocketcasts.account.viewmodel.OnboardingPlusBottomSheetState.Loaded
import au.com.shiftyjelly.pocketcasts.account.viewmodel.OnboardingPlusBottomSheetState.Loading
import au.com.shiftyjelly.pocketcasts.account.viewmodel.OnboardingPlusBottomSheetState.NoSubscriptions
import au.com.shiftyjelly.pocketcasts.models.type.Subscription
import au.com.shiftyjelly.pocketcasts.repositories.subscription.ProductDetailsState
import au.com.shiftyjelly.pocketcasts.repositories.subscription.SubscriptionManager
import au.com.shiftyjelly.pocketcasts.utils.log.LogBuffer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingPlusBottomSheetViewModel @Inject constructor(
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingPlusBottomSheetState>(Loading)
    val state: StateFlow<OnboardingPlusBottomSheetState> = _state

    init {
        viewModelScope.launch {
            subscriptionManager
                .observeProductDetails()
                .asFlow()
                .stateIn(viewModelScope)
                .collect { productDetails ->
                    val subscriptions = when (productDetails) {
                        is ProductDetailsState.Error -> null
                        is ProductDetailsState.Loaded -> productDetails.productDetails.mapNotNull { productDetailsState ->
                            Subscription.fromProductDetails(
                                productDetails = productDetailsState,
                                isFreeTrialEligible = subscriptionManager.isFreeTrialEligible()
                            )
                        }
                    } ?: emptyList()
                    _state.update { stateFromList(subscriptions) }
                }
        }
    }

    fun updateSelectedSubscription(subscription: Subscription) {
        val current = state.value
        when (current) {
            is Loaded -> {
                _state.update { current.copy(selectedSubscription = subscription) }
            }
            else -> {
                LogBuffer.e(
                    LogBuffer.TAG_INVALID_STATE,
                    "Updating selected subscription without any available subscriptions. This should never happen."
                )
            }
        }
    }

    fun stateFromList(subscriptions: List<Subscription>): OnboardingPlusBottomSheetState {
        val defaultSelected = subscriptionManager.getDefaultSubscription(subscriptions)
        return if (defaultSelected == null) {
            NoSubscriptions
        } else {
            Loaded(subscriptions, defaultSelected)
        }
    }
}

sealed class OnboardingPlusBottomSheetState {
    object Loading : OnboardingPlusBottomSheetState()
    object NoSubscriptions : OnboardingPlusBottomSheetState()
    data class Loaded constructor(
        val subscriptions: List<Subscription>, // This list should never be empty
        val selectedSubscription: Subscription
    ) : OnboardingPlusBottomSheetState() {
        init {
            if (subscriptions.isEmpty()) {
                LogBuffer.e(
                    LogBuffer.TAG_INVALID_STATE,
                    "Loaded subscription selection bottom sheet during onboarding with no subscriptions. This should never happen."
                )
            }
        }
    }
}
