import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

val keywords:Map<String,Color> = mapOf("val" to Color.Blue);

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
        if (text==""){
            return@buildAnnotatedString;
        }
        val split = text.split(" ");
        split.forEachIndexed { index, s ->
            if(keywords.containsKey(s)){
                withStyle(style= SpanStyle(color = keywords[s]!!, fontSize = 12.sp)){
                    append(s);
                    append(" ");
                }
            }
            else{
                withStyle(style = SpanStyle(color = Color.Black, fontSize = 12.sp)){
                    append(s);
                    append(" ");
                }
            }

        }
    }
    return annotatedString;
}

@Composable
fun TextArea() {
    val text = rememberSaveable { mutableStateOf("") }
    BasicTextField(
        value = text.value,
        onValueChange = { text.value = it },
        textStyle = TextStyle(color = Color.Transparent, fontSize = 12.sp),
        modifier = Modifier
            .fillMaxWidth().height(500.dp)
            .padding(16.dp),
        visualTransformation = {TransformedText(getColoredText(text.value).subSequence(0,text.value.length),OffsetMapping.Identity) }
    )
}

@Composable
@Preview
fun App() {

    MaterialTheme {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextArea();
        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
