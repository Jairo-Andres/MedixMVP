package com.medix.mvp

import com.medix.mvp.core.time.DefaultTimeProvider
import com.medix.mvp.domain.usecase.ParseEntitiesUseCase
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun parseDateEntity() {
        val parser = ParseEntitiesUseCase(DefaultTimeProvider())
        val entities = parser.execute("cita para 16 de febrero")
        assertNotNull(entities.fecha)
    }
}
