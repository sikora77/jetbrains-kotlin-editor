import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

const val TAB_SPACE_AMOUNT = "    "; // 1 tab is 4 spaces
val keywords: Map<String, Color> =
    mapOf(
        "val" to Color.Blue,
        "var" to Color.Blue,
        "if" to Color.Blue,
        "for" to Color.Blue,
        "fun" to Color.Blue,
        "println" to Color.Blue
    );

fun getColoredText(input: String): AnnotatedString {
    return buildAnnotatedString {
        val regex =
            Regex("(//.*|/\\*[\\s\\S]*?\\*/)|([^()\\s;]+)|([()\\s;]+)") // Captures words and all whitespaces including ;
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
    RoundedCard(modifier = Modifier, bgColor = Color.White) {
        BasicTextField(
            value = text,
            onValueChange = { it ->
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
                .fillMaxWidth().height(500.dp).padding(8.dp),
            visualTransformation = {
                TransformedText(
                    getColoredText(text).subSequence(0, text.length),
                    OffsetMapping.Identity
                )
            },
            interactionSource = remember { MutableInteractionSource() }, // Ensures interactivity and text selection
        )
    }
}

fun runScript(
    script: String,
    modifyOutputText: (String) -> Unit,
    modifyOutputErrors: (String) -> Unit,
    modifyScriptRunning: (Boolean, Int?) -> Unit,
) {
    Thread {
        val filename = "/home/sikora/IdeaProjects/KotlinEditor/script.kts"
        File(filename).writeText(script);
        val process = ProcessBuilder(listOf("kotlinc", "-script", filename)).redirectErrorStream(false).start();
        modifyScriptRunning(true, null);
        val inputStream = process.inputStream;
        val errorStream = process.errorStream;
        val errorReader = BufferedReader(InputStreamReader(errorStream));
        val reader = BufferedReader(InputStreamReader(inputStream));
        Thread {
            reader.forEachLine {
                modifyOutputText(it);
            }
        }.start();
        Thread {
            errorReader.forEachLine {
                modifyOutputErrors(it);
            }
        }.start()
        val exitCode = process.waitFor()
        modifyScriptRunning(false, exitCode);
    }.start()


}

@Composable
fun RoundedCard(modifier: Modifier, bgColor: Color, inner: @Composable() () -> Unit) {
    Card(
        backgroundColor = bgColor,
        shape = RoundedCornerShape(15.dp), modifier = modifier.padding(4.dp).fillMaxWidth(), border = BorderStroke(
            1.dp,
            Color.Black
        )
    ) { inner() }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scriptText = rememberSaveable {
            mutableStateOf(
                "// This is a simple Kotlin script to test the process execution\n" +
                        "println(\"Hello from the Kotlin script!\")\n" +
                        "\n" +
                        "// Simple computation\n" +
                        "val result = 42 * 2\n" +
                        "println(\"The result of 42 * 2 is: \$result\")\n" +
                        "\n" +
                        "// Simulate a delay (for testing live output)\n" +
                        "Thread.sleep(1000)\n" +
                        "println(\"This is printed after a 1-second delay.\")\n"
            )
        }
        val outputText = rememberSaveable { mutableStateOf("") }
        val annotatedTextBuilder = remember { AnnotatedString.Builder() }
        var textToShow by remember { mutableStateOf(annotatedTextBuilder.toAnnotatedString()) }
        val exitCode: MutableState<Int?> = rememberSaveable { mutableStateOf(null) }
        val isScriptRunning = rememberSaveable { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(textToShow) {
            coroutineScope.launch { scrollState.animateScrollTo(scrollState.maxValue) }

        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(modifier = Modifier.height(40.dp).width(130.dp), onClick = {
                    runScript(
                        scriptText.value,
                        { output ->
                            annotatedTextBuilder.withStyle(SpanStyle(color = Color.Gray)) { append(output + "\n") }
                            textToShow = annotatedTextBuilder.toAnnotatedString()
                        },
                        { output ->
                            annotatedTextBuilder.withStyle(SpanStyle(color = Color.Red)) { append(output + "\n") }
                            textToShow = annotatedTextBuilder.toAnnotatedString()
                        },
                        { running, code -> isScriptRunning.value = running;exitCode.value = code },
                    )
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isScriptRunning.value) {
                            CircularProgressIndicator(Modifier.width(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.PlayArrow, "Play arrow")
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Run Script", fontSize = 12.sp)
                    }
                }
//                Text("Is script running: ${isScriptRunning.value}")
                Text("Return Code: ${exitCode.value}")
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        HighlitableTextArea(scriptText.value, changeText = { it -> scriptText.value = it });
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    Row(modifier = Modifier.fillMaxWidth()) {

                        RoundedCard(modifier = Modifier.fillMaxWidth(0.98f).fillMaxHeight(), bgColor = Color.Black) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                                    Text(
                                        textToShow,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }

                            }
                        }
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(scrollState),
                            modifier = Modifier.fillMaxHeight(), style = ScrollbarStyle(
                                minimalHeight = 16.dp,
                                thickness = 6.dp,
                                shape = RoundedCornerShape(3.dp),
                                hoverDurationMillis = 300,
                                unhoverColor = Color(0x80007AFF), // Semi-transparent Apple blue
                                hoverColor = Color(0xFF007AFF)  // Solid Apple blue
                            )
                        )
                    }
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
