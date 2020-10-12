package dev.gumil.acorn.compose

import com.nhaarman.acorn.presentation.Scene

interface ComposeControllerFactory {

    fun supports(scene: Scene<*>): Boolean

    fun viewControllerFor(scene: Scene<*>): ComposeContainer
}

object NoopComposeControllerFactory : ComposeControllerFactory {

    override fun supports(scene: Scene<*>): Boolean {
        return false
    }

    override fun viewControllerFor(scene: Scene<*>): ComposeContainer {
        error("NoopComposeControllerFactory can not create ViewControllers.")
    }
}
