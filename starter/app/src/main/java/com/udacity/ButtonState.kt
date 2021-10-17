package com.udacity


sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()

    // state "machine"
    fun next() = when (this) {
        Completed -> Clicked
        Clicked -> Loading
        Loading -> Completed
    }
}