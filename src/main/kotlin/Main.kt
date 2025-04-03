import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

const val TAB_SPACE_AMOUNT = "    "; // 1 tab is 4 spaces
val keywords: Map<String, Color> =
    mapOf("val" to Color.Blue, "var" to Color.Blue, "if" to Color.Blue, "for" to Color.Blue, "fun" to Color.Blue);

fun getColoredText(input: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("([^\\s;]+)|([\\s;]+)") // Captures words and all whitespaces including ;
        val matches = regex.findAll(input);

        for (match in matches) {
            val part = match.value
            val color = when {
                part in keywords.keys -> {
                    keywords[part]
                }

                part == ";" -> Color.Gray // Special color for ;
                else -> Color.Black
            }
            withStyle(style = SpanStyle(color = color!!)) {
                append(part) // Keep whitespace intact
            }
        }
    }
}

@Composable
fun HighlitableTextArea() {
    val text = rememberSaveable { mutableStateOf(TextFieldValue("")) }
    BasicTextField(
        value = text.value,
        onValueChange = {
            val modifiedText = it.text.replace("\t",TAB_SPACE_AMOUNT);
            var newCursor = it.selection.start;
            if(modifiedText.length >it.text.length) {
                newCursor +=3;
            }
            text.value = TextFieldValue(text=modifiedText, selection = TextRange(newCursor))
        },
        textStyle = TextStyle(color = Color.Transparent, fontSize = 12.sp),
        modifier = Modifier
            .fillMaxWidth().height(500.dp)
            .padding(16.dp),
        visualTransformation = {
            TransformedText(
                getColoredText(text.value.text).subSequence(0, text.value.text.length),
                OffsetMapping.Identity
            )
        }
    )
}


@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            Row {

            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        HighlitableTextArea();
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().background(Color.Black).fillMaxHeight()) {
                    Text(
                        "this is a veeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeery long text",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
