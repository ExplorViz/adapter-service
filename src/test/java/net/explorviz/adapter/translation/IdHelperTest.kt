package net.explorviz.adapter.translation

import com.google.common.io.BaseEncoding
import net.explorviz.adapter.service.converter.IdHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdHelperTest {

    @Test
    fun spanId() {
        val id = "7ef83c66eabd5fbb"
        val spanId = BaseEncoding.base16().lowerCase().decode(id)
        val convertedSpanId = IdHelper.convertSpanId(spanId)
        assertEquals("7ef83c66eabd5fbb", convertedSpanId)
    }

    @Test
    fun traceId() {
        val id = "50c246ad9c9883d1558df9f19b9ae7a6"
        val traceId = BaseEncoding.base16().lowerCase().decode(id)
        val convertedTraceId = IdHelper.convertTraceId(traceId)
        assertEquals("50c246ad9c9883d1558df9f19b9ae7a6", convertedTraceId)
    }
}
