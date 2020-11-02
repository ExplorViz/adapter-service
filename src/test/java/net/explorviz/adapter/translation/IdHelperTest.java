package net.explorviz.adapter.translation;

import com.google.common.io.BaseEncoding;
import net.explorviz.adapter.conversion.converter.IdHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IdHelperTest {

  @Test
  void spanId() {
    String id = "7ef83c66eabd5fbb";
    byte[] spanId = BaseEncoding.base16().lowerCase().decode(id);
    String got = IdHelper.converterSpanId(spanId);
    Assertions.assertEquals("7ef83c66eabd5fbb", got);
  }

  @Test
  void traceId() {
    String id = "50c246ad9c9883d1558df9f19b9ae7a6";
    byte[] traceId = BaseEncoding.base16().lowerCase().decode(id);
    String got = IdHelper.converterTraceId(traceId);
    Assertions.assertEquals("50c246ad9c9883d1558df9f19b9ae7a6", got);
  }

}
