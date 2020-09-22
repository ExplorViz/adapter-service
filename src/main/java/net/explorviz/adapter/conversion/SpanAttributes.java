package net.explorviz.adapter.conversion;

import net.explorviz.avro.SpanStructure;

public interface SpanAttributes {
  String getLandscapeToken();

  String getHostName();

  String getHostIPAddress();

  String getApplicationName();

  String getApplicationPID();

  String getApplicationLanguage();

  String getMethodFQN();

  void appendToStructure(SpanStructure.Builder builder);
}
