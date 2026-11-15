package com.codebyramiz.ambient.core.data

import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ClockRepository {
    /** Emits the wall-clock time now and again at the start of every minute. */
    val minutes: Flow<LocalDateTime>
}

class SystemClockRepository(private val now: () -> LocalDateTime) : ClockRepository {
    @Inject
    constructor() : this({ LocalDateTime.now() })

    override val minutes: Flow<LocalDateTime> = flow {
        while (true) {
            val time = now()
            emit(time)
            val intoMinuteMillis = time.second * 1_000L + time.nano / 1_000_000L
            delay(MINUTE_MILLIS - intoMinuteMillis)
        }
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
