package edu.usfca.dataflow.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Device;

import edu.usfca.protobuf.Bid.BidLog;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;

/**
 * This file was copied from reference solution of a previous project, and slightly modified for the purpose of this
 * project.
 *
 * You can assume that all methods in this class are correct (once you fix a silly bug in some methods).
 *
 * It is up to you whether you re-use this code or you write your own (which may be more efficient).
 *
 * Also, if you are unsure about something or have questions, feel free to ask on Piazza (but also do check Project 2
 * instructions as you may find what you need faster that way).
 *
 * (It's strongly recommended that you use every single class/method provided here, because that will simplify a lot of
 * things in your code.)
 */

public class BidLogUtils {
  /**
   * (You can assume that this method is correct.)
   *
   * * A BidLog proto message is considered invalid if any of the following is true:
   *
   * (1) exchange is unknown (has enum value of 0)
   *
   * (2) bid_result is unknown (has enum value of 0)
   *
   * (3) bid_price <= 0 when bid_result is "BID" (see comments in bid.proto)
   *
   * (4) bid_price != 0 when bid_result is not "BID" (see comments in bid.proto)
   *
   * (5) received_at <= 0 (see comments in bid.proto)
   *
   * (6) processed_at <= received_at (see comments in bid.proto)
   *
   * (7) Not applicable.
   *
   * (8) bid_request.device.os is neither "ios" nor "android" (case-insensitive)
   *
   * (9) bid_request.device.ifa is an invalid uuid (use {@link java.util.UUID#fromString} to check)
   *
   * (10) bid_request.app.bundle is blank (use {@link org.apache.commons.lang3.StringUtils#isBlank})
   *
   * (11) bid_request.device.geo.country is blank (use {@link org.apache.commons.lang3.StringUtils#isBlank})
   *
   * (12) bid_request.device.geo.region is blank (use {@link org.apache.commons.lang3.StringUtils#isBlank})
   *
   * If none of the above is true, then return true (aka the given bidlog is valid).
   */
  public static boolean isValid(BidLog bidLog) {
    if (bidLog.getExchange() == Exchange.UNKNOWN_EXCHANGE) {
      return false;
    }
    if (getOsType(bidLog.getBidRequest().getDevice().getOs()) == OsType.UNKNOWN_OS_TYPE) {
      return false;
    }
    try {
      UUID.fromString(bidLog.getBidRequest().getDevice().getIfa());
    } catch (IllegalArgumentException e) {
      return false;
    }
    if (StringUtils.isBlank(bidLog.getBidRequest().getApp().getBundle())) {
      return false;
    }
    if (StringUtils.isBlank(bidLog.getBidRequest().getDevice().getGeo().getCountry())) {
      return false;
    }
    if (StringUtils.isBlank(bidLog.getBidRequest().getDevice().getGeo().getRegion())) {
      return false;
    }
    return true;
  }

  /**
   * Helper method for {@link #getDeviceProfile(BidLog)}.
   */
  private static OsType getOsType(String os) {
    if ("android".equalsIgnoreCase(os)) {
      return OsType.ANDROID;
    } else if ("ios".equalsIgnoreCase(os)) {
      return OsType.IOS;
    } else {
      return OsType.UNKNOWN_OS_TYPE;
    }
  }

  public static OsType getOs(String os) {
    if ("1".equals(os)) {
      return OsType.ANDROID;
    } else if ("2".equals(os)) {
      return OsType.IOS;
    } else {
      return OsType.UNKNOWN_OS_TYPE;
    }
  }

  /**
   * This returns a correct DeviceProfile proto, given BidLog proto.
   * @throws IllegalArgumentException if the input element is not valid (use {@link #isValid(BidLog)}.
   */
  public static DeviceProfile getDeviceProfile(BidLog bidLog) {
    if (!isValid(bidLog)) {
      throw new IllegalArgumentException("invalid bidlog");
    }

    BidRequest bidRequest = bidLog.getBidRequest();
    Device device = bidRequest.getDevice();

    final String bundle = bidRequest.getApp().getBundle();
    final Exchange exchange = bidLog.getExchange();
    final String country = device.getGeo().getCountry();
    final String region = device.getGeo().getRegion();

    return DeviceProfile.newBuilder()
        .setDeviceId(String.format("%d%s", getOsType(device.getOs()).getNumber(), device.getIfa().toLowerCase()))
        .setFirstAt(bidLog.getReceivedAt()) //
        .addGeo(String.format("%s%s", country, region))
        .addApp(AppActivity.newBuilder().setBundle(bundle).addExchange(exchange.getNumber()).addCount(1).build())
        .build();
  }
}
