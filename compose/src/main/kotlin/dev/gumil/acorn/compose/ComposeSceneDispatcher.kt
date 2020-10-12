package dev.gumil.acorn.compose

import android.content.Context
import android.content.Intent
import com.nhaarman.acorn.android.dispatching.AcornSceneDispatcher
import com.nhaarman.acorn.android.dispatching.SceneDispatcher
import com.nhaarman.acorn.android.presentation.ActivityControllerFactory
import dev.gumil.acorn.compose.uistate.UIHandler
import dev.gumil.acorn.compose.internal.ActivityHandler
import com.nhaarman.acorn.navigation.DisposableHandle
import com.nhaarman.acorn.navigation.Navigator
import com.nhaarman.acorn.navigation.TransitionData
import com.nhaarman.acorn.presentation.Container
import com.nhaarman.acorn.presentation.Scene
import com.nhaarman.acorn.state.SavedState
import com.nhaarman.acorn.state.get
import com.nhaarman.acorn.state.savedState

class ComposeSceneDispatcher internal constructor(
    private val context: Context,
    private val viewControllerFactory: ComposeControllerFactory,
    private val activityControllerFactory: ActivityControllerFactory,
    private val activityHandler: ActivityHandler,
    private val callback: AcornSceneDispatcher.Callback
): SceneDispatcher {

    override fun dispatchScenesFor(navigator: Navigator): DisposableHandle {
        return navigator.addNavigatorEventsListener(MyListener())
    }

    override fun onActivityResult(resultCode: Int, data: Intent?) {
        activityHandler.onActivityResult(resultCode, data)
    }

    override fun onUIVisible() {
        uiHandler.onUIVisible()
    }

    override fun onUINotVisible() {
        uiHandler.onUINotVisible()
    }

    override fun onBackPressed(): Boolean {
        return uiHandler.onBackPressed()
    }

    override fun saveInstanceState(): SavedState {
        return savedState {
            it.activityHandlerState = activityHandler.saveInstanceState()
        }
    }

    private inner class MyListener : Navigator.Events {

        override fun scene(scene: Scene<out Container>, data: TransitionData?) {

            if (viewControllerFactory.supports(scene)) {
                activityHandler.withoutScene()
                uiHandler.withScene(scene, viewControllerFactory, data)
                return
            }

            if (activityControllerFactory.supports(scene.key)) {
                uiHandler.withoutScene()
                activityHandler.withScene(scene, activityControllerFactory.activityControllerFor(scene, context))
                return
            }

            throw SceneDispatchFailureException(scene)
        }

        override fun finished() {
            callback.finished()
        }
    }

    private class SceneDispatchFailureException(
        private val scene: Scene<*>
    ) : IllegalStateException() {

        override val message: String by lazy {
            "Could not dispatch Scene with key [${scene.key}]\n" +
                "\n" +
                "\tNo Container could be created for $scene.\n" +
                "\tPossible causes include:\n" +
                "\n" +
                "\t\t- No ViewControllerFactory supports the Scene.\n" +
                "\t\t  Ensure either your Scene implements ViewProvidingScene (i.e. using ProvidesView),\n" +
                "\t\t  or register a ViewControllerFactory instance that can create a ViewController for the Scene.\n"
        }
    }

    companion object {
        private var SavedState?.activityHandlerState: SavedState?
            get() {
                return this?.get("activity_handler")
            }
            set(value) {
                this?.set("activity_handler", value)
            }
    }
}
