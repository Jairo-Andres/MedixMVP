package com.medix.mvppro.data.repository

import com.medix.mvppro.data.datasource.FakeAppointmentDataSource
import com.medix.mvppro.data.mapper.AppointmentMapper
import com.medix.mvppro.domain.model.AgendaSnapshot
import com.medix.mvppro.domain.repository.AppointmentRepository

class AppointmentRepositoryImpl(
    private val dataSource: FakeAppointmentDataSource
) : AppointmentRepository {
    override fun getSnapshot(): AgendaSnapshot = AgendaSnapshot(
        available = dataSource.getAvailable().map(AppointmentMapper::toDomain),
        booked = dataSource.getBooked().map(AppointmentMapper::toDomain)
    )

    override fun proposeNext(userRequest: String): String = dataSource.proposeNext(userRequest)

    override fun confirmPending(): String = dataSource.confirmPending()

    override fun cancelLast(): String = dataSource.cancelLast()

    override fun reschedule(userRequest: String): String = dataSource.reschedule(userRequest)
}
