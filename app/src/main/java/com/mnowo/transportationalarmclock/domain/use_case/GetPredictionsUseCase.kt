package com.mnowo.transportationalarmclock.domain.use_case

import com.mnowo.transportationalarmclock.domain.repository.AlarmClockRepository

class GetPredictionsUseCase(
    private val repository: AlarmClockRepository
) {

    operator fun invoke() {

    }
}