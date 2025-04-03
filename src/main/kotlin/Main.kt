import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
fun HighlitableTextArea(text: String, changeText: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = {it->
            changeText(it)
//            val modifiedText = it.text.replace("\t", TAB_SPACE_AMOUNT);
//
//            if (modifiedText.length > it.text.length) {
//                var newCursor = it.selection.start;
//                newCursor += 3;
//                changeText(TextFieldValue(text = modifiedText, selection = TextRange(newCursor)))
//            }
//            else{
//                changeText(TextFieldValue(text = modifiedText,selection=TextRange(modifiedText.length)))
//            }
        },
        textStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
        modifier = Modifier
            .fillMaxWidth().height(500.dp)
            .padding(16.dp),
        visualTransformation = {
            TransformedText(
                getColoredText(text).subSequence(0, text.length),
                OffsetMapping.Identity
            )
        },interactionSource = remember { MutableInteractionSource() }, // Ensures interactivity and text selection
    )
}

fun runScript(script: String, modifyOutputText: (String) -> Unit, modifyScriptRunning: (Boolean, Int?) -> Unit) {
    Thread {
        val filename = "/home/sikora/IdeaProjects/KotlinEditor/script.kts"
        File(filename).writeText(script);
        val process = ProcessBuilder(listOf("kotlinc", "-script", filename)).redirectErrorStream(true).start();
        modifyScriptRunning(true, null);
        val inputStream = process.inputStream;
        val reader = BufferedReader(InputStreamReader(inputStream));
        reader.forEachLine {
            modifyOutputText(it);
        }
        val exitCode = process.waitFor()
        modifyScriptRunning(false, exitCode);
    }.start()


}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scriptText = rememberSaveable { mutableStateOf("") }
        val outputText = rememberSaveable { mutableStateOf("") }
        val exitCode: MutableState<Int?> = rememberSaveable { mutableStateOf(null) }
        val isScriptRunning = rememberSaveable { mutableStateOf(false) }

        Column {
            Row {
                Button(onClick = {
                    runScript(
                        scriptText.value,
                        { output -> outputText.value += output;outputText.value += "\n" },
                        { running, code -> isScriptRunning.value = running;exitCode.value = code })
                }) {
                    Text("Run Script")
                }
                Text("Is script running: ${isScriptRunning.value}")
                Text("Return Code: ${exitCode.value}")
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                            HighlitableTextArea(scriptText.value, changeText = { it -> scriptText.value = it });
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().background(Color.Black).fillMaxHeight()) {
                    Text(
                        outputText.value,
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
