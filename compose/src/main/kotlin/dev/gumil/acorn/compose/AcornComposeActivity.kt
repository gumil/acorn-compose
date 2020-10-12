package dev.gumil.acorn.compose

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.nhaarman.acorn.android.AcornActivityDelegate
import com.nhaarman.acorn.android.navigation.NavigatorProvider
import com.nhaarman.acorn.android.presentation.ActivityController
import com.nhaarman.acorn.android.presentation.ActivityControllerFactory
import com.nhaarman.acorn.android.presentation.ComposingViewControllerFactory
import com.nhaarman.acorn.android.presentation.NoopActivityControllerFactory
import com.nhaarman.acorn.android.presentation.NoopViewControllerFactory
import com.nhaarman.acorn.android.presentation.SceneViewControllerFactory
import com.nhaarman.acorn.android.presentation.ViewController
import com.nhaarman.acorn.android.presentation.ViewControllerFactory
import com.nhaarman.acorn.android.transition.ComposingSceneTransitionFactory
import com.nhaarman.acorn.android.transition.DefaultSceneTransitionFactory
import com.nhaarman.acorn.android.transition.NoopSceneTransitionFactory
import com.nhaarman.acorn.android.transition.SceneTransition
import com.nhaarman.acorn.android.transition.SceneTransitionFactory
import com.nhaarman.acorn.navigation.Navigator
import com.nhaarman.acorn.presentation.Scene

/**
 * A base [AppCompatActivity] implementation to simplify Acorn usage.
 *
 * If you can't or don't want to inherit from this class, you can use the
 * [AcornActivityDelegate] class and route the necessary Activity function
 * calls to it.
 */
abstract class AcornComposeActivity : AppCompatActivity() {

    /**
     * Returns the [NavigatorProvider] to use in this Activity.
     *
     * [NavigatorProvider] instances should be shared across instances, so
     * make sure you cache this instance outside of this Activity.
     */
    protected abstract fun provideNavigatorProvider(): NavigatorProvider

    /**
     * Returns the [ViewControllerFactory] that can provide
     * [ViewController] instances for this Activity.
     *
     * The instance returned here will be combined with a
     * [SceneViewControllerFactory] to be able to use [Scene] instances as
     * ViewController factories.
     *
     * Returns [NoopViewControllerFactory] by default.
     */
    protected open fun provideViewControllerFactory(): ComposeControllerFactory {
        return NoopComposeControllerFactory
    }

    /**
     * Returns the [ActivityControllerFactory] that can provide
     * [ActivityController] instances when using external Activities.
     */
    protected open fun provideActivityControllerFactory(): ActivityControllerFactory {
        return NoopActivityControllerFactory
    }

    /**
     * Returns the root [ViewGroup] that is used to inflate Scene views in.
     *
     * This method will be called once by Acorn, so it is safe to create new instances here.
     *
     * Override this method if you want to provide your own ViewGroup implementation.
     * If the returned ViewGroup has no parent, it will be passed to a call to [setContentView].
     *
     * This method returns `null` by default, which will result in an empty [FrameLayout] being
     * used as the content view.
     *
     * @return a ViewGroup to be used as the root view, or `null` to fall back to default behavior.
     * @see rootView
     */
    protected open fun provideRootView(): ViewGroup? {
        return null
    }

    private val navigatorProvider: NavigatorProvider by lazy {
        provideNavigatorProvider()
    }

    private val viewControllerFactory: ComposeControllerFactory by lazy {
        provideViewControllerFactory()
    }

    private val activityControllerFactory: ActivityControllerFactory by lazy {
        provideActivityControllerFactory()
    }

    /**
     * Returns the navigator used in this instance.
     * Must only be called _after_ [onCreate] has been called.
     */
    protected fun navigator(): Navigator {
        return acornDelegate.navigator()
    }

    private val rootView by lazy {
        val rootView = provideRootView() ?: FrameLayout(this)
        if (rootView.parent == null) {
            setContentView(rootView)
        }
        rootView
    }

    private val acornDelegate: AcornComposeActivityDelegate by lazy {
        AcornComposeActivityDelegate.from(
            activity = this,
            root = rootView,
            navigatorProvider = navigatorProvider,
            viewControllerFactory = viewControllerFactory,
            activityControllerFactory = activityControllerFactory,
        )
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acornDelegate.onCreate(savedInstanceState)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        acornDelegate.onStart()
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        acornDelegate.onActivityResult(requestCode, resultCode, data)
    }

    @CallSuper
    override fun onBackPressed() {
        if (!acornDelegate.onBackPressed()) {
            super.onBackPressed()
        }
    }

    @CallSuper
    override fun onStop() {
        acornDelegate.onStop()
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        acornDelegate.onDestroy()
        super.onDestroy()
    }

    /**
     * [AppCompatActivity.onSaveInstanceState] saves the view hierarchy state,
     * which is something we do manually. Therefore we do not call the super
     * implementation.
     */
    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        acornDelegate.onSaveInstanceState(outState)
    }
}
