package com.example.speechtotextapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.speechtotextapp.ui.theme.SpeechToTextAppTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var speechResultLauncher: ActivityResultLauncher<Intent>
    // Define textState here to make it a member variable of the class
    private val textState = mutableStateOf("")
    private val selectedLanguage = mutableStateOf("Canadian French")  // Default selection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the speechResultLauncher before setContent
        speechResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val spokenText: String =
                        result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            .orEmpty().firstOrNull().orEmpty()
                    // Now you can access textState because it's been defined at the class level
                    textState.value += "$spokenText\n"
                }
            }

        setContent {
            SpeechToTextAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the member variable textState to SpeechToTextUI
                    SpeechToTextUI(
                        onSpeak = { startSpeechToText(selectedLanguage.value) },
                        textState = textState,
                        selectedLanguage = selectedLanguage
                    )
                }
            }
        }
    }
    private fun startSpeechToText(language: String) {
        val locale = when (language) {
            "Canadian French" -> Locale.CANADA_FRENCH
            "Canadian English" -> Locale.CANADA
            else -> Locale.getDefault()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,locale.language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now / Parler maintenant")
        }
        try {
            speechResultLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle exception, e.g., show a toast message
        }
    }


}


@Composable
fun SpeechToTextUI(
    onSpeak: () -> Unit,
    textState:MutableState<String>,
    selectedLanguage: MutableState<String>
) {
    val languages = listOf("Canadian French", "Canadian English")
    val expanded = remember { mutableStateOf(false) }
    val heightConfiguration: Int = LocalConfiguration.current.screenHeightDp
    val widthConfiguration: Int = LocalConfiguration.current.screenWidthDp
    Column(modifier = Modifier.padding(16.dp)) {

        val undoStack = remember { mutableListOf<String>() }
        val redoStack = remember { mutableListOf<String>() }
        OutlinedTextField(
            value = textState.value,
            onValueChange = {
                                undoStack.add(textState.value) // Save current state to undo stack
                                redoStack.clear() // Clear redo stack
                                textState.value = it
                            },
            label = { Text(text="Transcribed Text",fontSize=20.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 10.dp) // Adjust vertical padding to make the text field taller
                .height(heightConfiguration.dp-150.dp), // Set a fixed height for the text field,
            textStyle = TextStyle(fontSize = 16.sp)
        )



        Row(modifier = Modifier.padding(1.dp))
        {
            MyButtonComposable(name = "Undo",textState = textState, undoStack = undoStack, redoStack = redoStack, onSpeak = {})
            MyButtonComposable(name = "Redo",textState = textState, undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            MyButtonComposable(name = "backspace",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            MyButtonComposable(name = "microphone", textState = textState,undoStack = undoStack, redoStack = redoStack, onSpeak = { onSpeak() })
            // Repeat for other buttons
        }

        // Numeric keypad layout
        /**Column(modifier = Modifier
            .padding(5.dp)
        )
        {
            Row (modifier = Modifier
                .padding(5.dp)
                )
            {
            MyButtonComposable(name = "0",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            MyButtonComposable(name = "1",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            MyButtonComposable(name = "2",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            MyButtonComposable(name = "3",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            MyButtonComposable(name = "4",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
            }
            Row(modifier = Modifier
                .padding(5.dp)
                )
            {
                MyButtonComposable(name = "5",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
                MyButtonComposable(name = "6",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
                MyButtonComposable(name = "7",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
                MyButtonComposable(name = "8",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
                MyButtonComposable(name = "9",textState = textState,undoStack = undoStack, redoStack = redoStack,onSpeak = {})
                // Repeat for other buttons
            }
      } */
        Row(modifier = Modifier.padding(8.dp).fillMaxSize(1f))
        {
                Text(
                    modifier = Modifier.padding(bottom=8.dp,top= 8.dp).height((heightConfiguration.dp/15)),
                    text = "Select Language: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            Box (modifier = Modifier.padding(start= 8.dp, top=5.dp)){
                Text(selectedLanguage.value, modifier = Modifier
                    .clickable { expanded.value = true }
                    .background(Color(0, 148, 171))
                    .padding(8.dp),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                    color= Color(0,0,0)
                )
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },

                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color= Color(0,0,0),
                                    text=language
                                ) },
                            onClick = {
                                selectedLanguage.value = language
                                expanded.value = false
                            },
                            modifier = Modifier
                                .background(Color(0, 148, 171))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyButtonComposable(name: String, textState: MutableState<String>, undoStack: MutableList<String> = mutableListOf(), redoStack: MutableList<String> = mutableListOf(),onSpeak: () -> Unit){
    val heightConfiguration: Int = LocalConfiguration.current.screenHeightDp
    val widthConfiguration: Int = LocalConfiguration.current.screenWidthDp
    Button(
        onClick = {
            when (name) {
                "microphone" -> onSpeak()
                "backspace" -> {
                    if (textState.value.isNotEmpty()) {
                        redoStack.add(textState.value) // Add current state to redo stack before backspacing
                        handleTextChange(textState.value.dropLast(1), textState, undoStack, redoStack, isBackspace = true)
                    }
                }
                "Undo" -> {
                    if (undoStack.isNotEmpty()) {
                        redoStack.add(textState.value)
                        textState.value = undoStack.removeAt(undoStack.size - 1)
                    Log.d("test : UNDO works",textState.value)
                    }
                    Log.d("test : UNDO is EMPTY",textState.value)
                }
                "Redo" -> {
                    if (redoStack.isNotEmpty()) {
                        undoStack.add(textState.value)
                        textState.value = redoStack.removeAt(redoStack.size - 1)
                        Log.d("test : REDO works",textState.value)
                    }
                    Log.d("test : REDO is EMPTY",textState.value)
                }
                else -> {
                    handleTextChange(textState.value + name, textState, undoStack, redoStack)
                }
            }
        },

        colors =
            if(name=="microphone")
            {
                ButtonDefaults.buttonColors(containerColor = Color.Red )
            }
            else
            {
                ButtonDefaults.buttonColors()
            },

        modifier = Modifier
                        .width(widthConfiguration.dp/4.6f)
                        .height(heightConfiguration.dp/18),
        )

    {
        if(name=="backspace"){
        Icon(painter = painterResource(id = R.drawable.baseline_backspace_24), contentDescription = "backspace" ,
            modifier = Modifier.size(24.dp))
            return@Button
        }
        if(name=="microphone") {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.baseline_mic_24),
                contentDescription = "Speak",
                tint = Color.White,
                modifier = Modifier.size(44.dp).width(widthConfiguration.dp/4)
                    .height(heightConfiguration.dp/18),
            )
            return@Button
        }
        if(name=="Undo" || name=="Redo") {
            Text(text = name, fontSize = 10.sp)
        }
    }
    Spacer(modifier = Modifier.padding(1.dp))
}
fun handleTextChange(newText: String, textState: MutableState<String>, undoStack: MutableList<String>, redoStack: MutableList<String>, isBackspace: Boolean = false) {
    if (newText != textState.value) {
        undoStack.add(textState.value)
        if (!isBackspace) {
            redoStack.clear()
        }
        textState.value = newText
    }
}





