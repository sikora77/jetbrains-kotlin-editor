import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

val keywords: Map<String, Color> = mapOf("val" to Color.Blue);

fun getColoredText(input: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("([^\\s;]+)|([\\s;]+)") // Captures words and all whitespaces including ;
        val matches = regex.findAll(input)

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

fun BuildColoredWords(text: String): AnnotatedString {
    var startIndex = 0;
    val annotatedString = buildAnnotatedString {
        if (text == "") {
            return@buildAnnotatedString;
        }
        val split = text.split(" ");
        split.forEachIndexed { index, s ->
            if (keywords.containsKey(s)) {
                withStyle(style = SpanStyle(color = keywords[s]!!, fontSize = 12.sp)) {
                    append(s);
                    append(" ");
                }
            } else {
                withStyle(style = SpanStyle(color = Color.Black, fontSize = 12.sp)) {
                    append(s);
                    append(" ");
                }
            }

        }
    }
    return annotatedString;
}

@Composable
fun HighlitableTextArea() {
    val text = rememberSaveable { mutableStateOf("") }
    BasicTextField(
        value = text.value,
        onValueChange = { text.value = it },
        textStyle = TextStyle(color = Color.Transparent, fontSize = 12.sp),
        modifier = Modifier
            .fillMaxWidth().height(500.dp)
            .padding(16.dp),
        visualTransformation = {
            TransformedText(
                getColoredText(text.value).subSequence(0, text.value.length),
                OffsetMapping.Identity
            )
        }
    )
}


@Composable
@Preview
fun App() {

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    HighlitableTextArea();
                }
            }
            Column(modifier = Modifier.fillMaxWidth().background(Color.Black).fillMaxHeight(),) {
                Text("this is a veeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeery long text",color = Color.White,fontSize = 12.sp)

            }
        }



    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
