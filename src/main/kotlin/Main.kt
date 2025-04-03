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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

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
fun HighlitableTextArea(text:TextFieldValue,changeText:(TextFieldValue)->Unit) {
    BasicTextField(
        value = text,
        onValueChange = {
            val modifiedText = it.text.replace("\t",TAB_SPACE_AMOUNT);
            var newCursor = it.selection.start;
            if(modifiedText.length >it.text.length) {
                newCursor +=3;
            }
            changeText(TextFieldValue(text=modifiedText, selection = TextRange(newCursor)))
        },
        textStyle = TextStyle(color = Color.Transparent, fontSize = 12.sp),
        modifier = Modifier
            .fillMaxWidth().height(500.dp)
            .padding(16.dp),
        visualTransformation = {
            TransformedText(
                getColoredText(text.text).subSequence(0, text.text.length),
                OffsetMapping.Identity
            )
        }
    )
}

fun runScript(script:String){
    val filename="/home/sikora/IdeaProjects/KotlinEditor/script.kts"
    File(filename).writeText(script);
    val process = ProcessBuilder(listOf("kotlinc" ,"-script",filename)  ).redirectErrorStream(true).start();
    val inputStream = process.inputStream;
    val reader = BufferedReader(InputStreamReader(inputStream));
    Thread{
        reader.forEachLine {
            println(it)
        }
    }.start()
    process.waitFor()
}
@Composable
@Preview
fun App() {
    MaterialTheme {
        val scriptText = rememberSaveable { mutableStateOf(TextFieldValue("")) }

        Column {
            Row {
                Button(onClick = { runScript(scriptText.value.text) }) {
                    Text("Run Script")
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        HighlitableTextArea(scriptText.value,changeText = {it->scriptText.value =it });
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
