package net.explorviz.adapter.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringJoiner;
import net.explorviz.avro.EVSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(HashHelper.class);

  private static final String DIGEST_ALGORITHM = "SHA3-256";

  public static String spanToHexHashString(final EVSpan span) {

    final StringJoiner joiner = new StringJoiner(";");

    /*
     * By definition getFullyQualifiedOperationName().split("."): Last entry is method name, next to
     * last is class name, remaining elements form the package name
     */
    final String fullyQualifiedOperationName =
        span.getFullyQualifiedOperationName().replace(".", ";");

    joiner.add(span.getLandscapeToken());
    joiner.add(span.getHostIpAddress());
    joiner.add(span.getAppPid());
    joiner.add(fullyQualifiedOperationName);

    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
    } catch (final NoSuchAlgorithmException e) {
      LOGGER.error("Set digest algorithm is not available. Did you use 'SHA3-256'?", e);
      throw new RuntimeException(e);
    }

    final byte[] hashbytes = digest.digest(joiner.toString().getBytes(StandardCharsets.UTF_8));

    return bytesToHex(hashbytes);

  }

  private static String bytesToHex(final byte[] hash) {
    final StringBuffer hexString = new StringBuffer();
    for (final byte element : hash) {
      final String hex = Integer.toHexString(0xff & element);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

}
