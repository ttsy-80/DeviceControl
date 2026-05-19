package com.devicecontrol.engine.v2.inspection

import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.TaskExecution
import org.junit.Assert.assertEquals
import org.junit.Test

class V2ModeSettingsMapperTest {

    private val baseExecution = TaskExecution(
        taskId = 1L,
        gearRatioIndex = 0,
        speed = 300.0,
        speedStep = 10.0,
        continuousCycles = 1,
        jogInterval = 2,
        playbackSpeed = 300.0,
        operationMode = OperationMode.CONTINUOUS,
    )

    @Test
    fun degPerSecToSecPerRev_converts360OverSpeed() {
        assertEquals(360.0, V2ModeSettingsMapper.degPerSecToSecPerRev(1.0, 100.0), 0.001)
    }

    @Test
    fun degPerSecToSecPerRev_usesFallbackWhenTooSmall() {
        assertEquals(100.0, V2ModeSettingsMapper.degPerSecToSecPerRev(0.0, 100.0), 0.001)
    }

    @Test
    fun applyAutoToExecution_setsJogAndMappedFields() {
        val snapshot = V2ModeSettingsSnapshot(
            mapOf(
                "auto_continuous" to 1.0,
                "jog_hold" to 5.0,
                "auto_turns" to 3.0,
                "reverse_speed" to 2.0,
            ),
        )
        val result = V2ModeSettingsMapper.applyAutoToExecution(snapshot, baseExecution)
        assertEquals(OperationMode.JOG, result.operationMode)
        assertEquals(360.0, result.speed, 0.001)
        assertEquals(5, result.jogInterval)
        assertEquals(3, result.continuousCycles)
        assertEquals(180.0, result.playbackSpeed, 0.001)
    }

    @Test
    fun applyManualToExecution_mapsSpeedWithoutChangingMode() {
        val snapshot = V2ModeSettingsSnapshot(
            mapOf(
                "manual_continuous" to 2.0,
                "manual_speed_step" to 0.5,
            ),
        )
        val result = V2ModeSettingsMapper.applyManualToExecution(snapshot, baseExecution)
        assertEquals(OperationMode.CONTINUOUS, result.operationMode)
        assertEquals(180.0, result.speed, 0.001)
    }
}
