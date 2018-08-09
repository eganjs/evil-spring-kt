package io.eganjs.evil.spring.config

import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class ServerPortWatcher : ApplicationListener<ServletWebServerInitializedEvent> {

    private val portSubject: BehaviorSubject<Int> = BehaviorSubject.create<Int>()
    val port: Single<Int> = portSubject
            .filter { it != -1 }
            .firstOrError()

    override fun onApplicationEvent(event: ServletWebServerInitializedEvent) {
        event.webServer?.port?.let {
            assignedPort -> portSubject.onNext(assignedPort)
        }
    }
}
