package ru.hollowhorizon.hc

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

data class Note(
    val id: Int,
    val content: String,
)


class NotesViewModel {
    private var nextId = 0
    val notes = mutableStateListOf<Note>()

    fun addNote(content: String) {
        notes.add(
            Note(
                id = nextId++,
                content = content
            )
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        notesWindow()
    }
}

@Composable
fun notesWindow() {
    val notes by remember { mutableStateOf(NotesViewModel()) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            NotesApp(notes)
        }
    }
}

@Composable
fun NotesApp(notesViewModel: NotesViewModel) {
    Column {
        var text by remember { mutableStateOf("Text") }
        TextField(text, { text = it })

        Button({
            text = "Ну привет :)"
        }) {
            Text("Hello!")
        }

        Box(Modifier.weight(1f)) {
            val state = rememberLazyListState()

            LazyColumn(state = state, modifier = Modifier.width(200.dp).fillMaxHeight()) {
                items(100) {
                    Text("Item $it")
                }
            }

            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}