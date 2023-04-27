package au.com.shiftyjelly.pocketcasts.wear.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import au.com.shiftyjelly.pocketcasts.compose.images.GravatarProfileImage
import au.com.shiftyjelly.pocketcasts.ui.theme.Theme
import au.com.shiftyjelly.pocketcasts.wear.theme.WearAppTheme
import au.com.shiftyjelly.pocketcasts.images.R as IR
import au.com.shiftyjelly.pocketcasts.localization.R as LR

object LoggingInScreen {
    const val route = "loggingInScreen"
}

@Composable
fun LoggingInScreen(
    onClose: () -> Unit,
) {

    val viewModel = hiltViewModel<LoggingInScreenViewModel>()
    val email = remember { viewModel.getEmail() }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClose() }
            .padding(16.dp)
            .fillMaxSize()
    ) {
        val placeholder = @Composable {
            SpinningIcon(Modifier.size(48.dp))
        }
        if (email == null) {
            placeholder()
        } else {
            GravatarProfileImage(
                email = email,
                contentDescription = null,
                placeholder = placeholder,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(LR.string.profile_logging_in),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.title3
        )
        if (email != null) {
            Text(
                text = email,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun SpinningIcon(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Icon(
        painter = painterResource(IR.drawable.ic_retry),
        contentDescription = null,
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
            }
    )
}

@Preview
@Composable
private fun LoggingInScreenPreview() {
    WearAppTheme(Theme.ThemeType.DARK) {
        LoggingInScreen(
            onClose = {}
        )
    }
}