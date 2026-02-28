package com.zedalpha.shadowgadgets.demo.topic

import android.os.Build
import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.zedalpha.shadowgadgets.compose.clippedShadow
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.ComposeViewBinding

internal val ComposeRootTopic =
    Topic(
        title = "Compose - Root",
        descriptionResId = R.string.description_compose_root,
        fragmentClass = ComposeRootFragment::class.java
    )

class ComposeRootFragment :
    TopicFragment<ComposeViewBinding>(ComposeViewBinding::inflate) {

    override fun loadUi(ui: ComposeViewBinding) {
        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { ComposeRootContent() }
        }
    }
}

@Composable
private fun ComposeRootContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =
            Arrangement.spacedBy(40.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var showDialog by remember { mutableStateOf(false) }
        var showAlertDialog by remember { mutableStateOf(false) }

        OutlinedButton({ showDialog = true }) {
            BoldText("Show Dialog", 16.sp)
        }
        OutlinedButton({ showAlertDialog = true }) {
            BoldText("Show AlertDialog", 16.sp)
        }

        ProvideTextStyle(LocalTextStyle.current.copy(color = Color.White)) {
            when {
                showDialog ->
                    DialogExample({ showDialog = false })
                showAlertDialog ->
                    AlertDialogExample({ showAlertDialog = false })
            }
        }
    }
}

@Composable
private fun DialogExample(
    dismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = modifier
                .rotate(-3F)
                .size(300.dp)
                .clickable { dismiss() }
                .background(
                    color = ClearBlue,
                    shape = MaterialTheme.shapes.medium
                )
                .rootClippedShadow()
                .padding(horizontal = 15.dp, vertical = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                BoldText("Dialog", 22.sp)
                BoldText("Click to dismiss", 22.sp)
            }
        }

        localDialogWindow()?.let { window ->
            SideEffect {
                window.decorView.elevation = 0F
                window.setDimAmount(0F)
                window.centerOver(view)
            }
        }
    }
}

@Composable
private fun AlertDialogExample(
    dismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    AlertDialog(
        onDismissRequest = dismiss,
        confirmButton = {},
        title = { BoldText("AlertDialog", 22.sp) },
        text = {
            BoldText("Click to dismiss", 22.sp)

            localDialogWindow()?.let { window ->
                SideEffect {
                    window.setDimAmount(0F)
                    window.centerOver(view)
                }
            }
        },
        modifier = modifier
            .rotate(3F)
            .size(300.dp)
            .clickable { dismiss() }
            .rootClippedShadow(),
        backgroundColor = ClearBlue,
        properties = DialogProperties(decorFitsSystemWindows = false)
    )
}

@Composable
private fun localDialogWindow(): Window? =
    (LocalView.current.parent as? DialogWindowProvider)?.window

private fun Modifier.rootClippedShadow(): Modifier =
    if (Build.VERSION.SDK_INT != 28) {
        // From AlertDialog, MaterialTheme.shapes.medium
        val shape = RoundedCornerShape(4.dp)
        this.clippedShadow(
            elevation = 10.dp,
            shape = shape,
            ambientColor = Color.Blue,
            spotColor = Color.Blue
        )
    } else {
        this.border(5.dp, Color.Magenta)
    }

@Composable
private fun BoldText(text: String, fontSize: TextUnit) =
    Text(text = text, fontSize = fontSize, fontWeight = FontWeight.Bold)