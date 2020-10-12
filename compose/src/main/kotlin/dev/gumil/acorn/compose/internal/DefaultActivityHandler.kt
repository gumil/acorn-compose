/*
 *    Copyright 2018 Niek Haarman
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.gumil.acorn.compose.internal

import android.content.Intent
import android.util.Log
import com.nhaarman.acorn.android.presentation.ActivityController
import com.nhaarman.acorn.presentation.Container
import com.nhaarman.acorn.presentation.Scene
import com.nhaarman.acorn.presentation.SceneKey
import com.nhaarman.acorn.state.SavedState
import com.nhaarman.acorn.state.get
import com.nhaarman.acorn.state.savedState

internal class DefaultActivityHandler(
    private val callback: Callback,
    private val savedState: SavedState?
) : ActivityHandler {

    private var lastScene: Scene<*>? = null
        set(value) {
            field = value
            lastSceneKey = value?.key
        }

    private var lastSceneKey: SceneKey? = savedState.lastSceneKey
    private var lastActivityController: ActivityController? = null

    override fun withScene(scene: Scene<out Container>, activityController: ActivityController) {
        Log.v("ActivityHandler", "Scene changed: $scene.")

        if (lastSceneKey == scene.key) {
            Log.v(
                "ActivityHandler",
                "New external Scene has the same key as the previously dispatched Scene, not starting Activity."
            )

            lastScene = scene
            lastActivityController = activityController
            return
        }

        lastScene = scene
        lastActivityController = activityController

        val intent = activityController.createIntent()

        Log.v("ActivityHandler", "Starting Intent: $intent.")
        callback.startForResult(intent)
    }

    override fun withoutScene() {
        if (lastSceneKey != null) {
            Log.d("ActivityHandler", "Scene lost.")
            lastScene = null
            lastSceneKey = null
        }
    }

    override fun onActivityResult(resultCode: Int, data: Intent?) {
        Log.d("ActivityHandler", "Activity result: resultCode=$resultCode, data=$data")

        val scene = lastScene
        val activityController = lastActivityController
        if (scene == null || activityController == null) {
            Log.w(
                "ActivityHandler",
                "Activity result without active Scene, dropping result"
            )
            return
        }

        Log.v("ActivityHandler", "Attaching container to $scene.")
        scene.forceAttach(activityController)

        Log.v("ActivityHandler", "Notifying ActivityController of result.")
        activityController.onResult(resultCode, data)

        Log.v("ActivityHandler", "Detaching container from $scene.")
        scene.forceDetach(activityController)
    }

    override fun saveInstanceState(): SavedState {
        return savedState { it.lastSceneKey = lastSceneKey }
    }

    interface Callback {

        fun startForResult(intent: Intent)
    }

    companion object {

        private var SavedState?.lastSceneKey: SceneKey?
            get() = this?.get<String>("scene_key")?.let(::SceneKey)
            set(value) {
                this?.set("scene_key", value?.value)
            }
    }
}

@Suppress("UNCHECKED_CAST")
private fun Scene<*>.forceAttach(c: Container) {
    (this as Scene<Container>).attach(c)
}

@Suppress("UNCHECKED_CAST")
private fun Scene<*>.forceDetach(c: Container) {
    (this as Scene<Container>).detach(c)
}
