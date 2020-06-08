package edu.usfca.dataflow.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;

import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;

/**
 * This file was copied from reference solution of a previous project, and slightly modified for the purpose of this
 * project.
 *
 * You can assume that all methods in this class are correct (once you fix a silly bug in some methods).
 *
 * It is up to you whether you re-use this code or you write your own.
 *
 * Also, if you are unsure about something or have questions, feel free to ask on Piazza (but also do check Project 2
 * instructions as you may find what you need faster that way).
 *
 * It's recommended that you use the provided code to start with, until you pass all unit tests, and then begin
 * optimizing your code. That way, you will have a correct implementation to refer to, as things may go south while
 * optimizing things.
 */
public class DeviceProfileUtils {

  /**
   * This method takes DeviceId proto, and lowers its uuid field (to normalize).
   *
   * That way, you can easily compare two DeviceIds.
   *
   * (This method has no intended bugs, and you can consider it "correct".)
   */
  public static DeviceId getCanonicalId(DeviceId id) {
    String uuid = id.getUuid();
    for (int i = 0; i < uuid.length(); i++) {
      if (!Character.isLowerCase(uuid.charAt(i))) {
        return id.toBuilder().setUuid(id.getUuid().toLowerCase()).build();
      }
    }
    return id;
  }

  /**
   * This returns true if the given DeviceId is valid.
   *
   * In this project: DeviceId is considered valid if its Os is known (either IOS and Android) and its uuid is NOT
   * blank.
   *
   * (This is the same method from Project 2.)
   */
  public static boolean isDeviceIdValid(DeviceId did) {
    if (did.getOs() != OsType.ANDROID && did.getOs() != OsType.IOS) {
      return false;
    }

    if (StringUtils.isBlank(did.getUuid())) {
      return false;
    }
    return true;
  }

  public static AppActivity mergeAppList(List<AppActivity> appActivityList) {
    Map<Integer, Integer> countPerExchange = new HashMap<>();

    for (AppActivity appActivity : appActivityList) {
      for (int i = 0; i < appActivity.getCountCount(); i++) {
        countPerExchange.merge(appActivity.getExchange(i), appActivity.getCount(i), Integer::sum);
      }
    }

    List<Integer> exchanges = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    for (Entry<Integer, Integer> kv : countPerExchange.entrySet()) {
      exchanges.add(kv.getKey());
      counts.add(kv.getValue());
    }

    return AppActivity.newBuilder() //
        .setBundle(appActivityList.get(0).getBundle()) //
        .addAllExchange(exchanges) //
        .addAllCount(counts) //
        .build();
  }

  public static DeviceProfile mergeDps(Iterable<DeviceProfile> dps) {
    if (Iterables.size(dps) == 0) {
      return DeviceProfile.getDefaultInstance();
    }
    if (Iterables.size(dps) == 1) {
      return dps.iterator().next();
    }

    Set<String> geos = new HashSet<>();
    List<AppActivity> appActivityList = new ArrayList<>();
    Map<String, List<AppActivity>> apps = new HashMap<>();
    long firstAt = Long.MAX_VALUE;

    for (DeviceProfile dp : dps) {
      geos.addAll(dp.getGeoList());
      for (AppActivity appActivity : dp.getAppList()) {
        apps.putIfAbsent(appActivity.getBundle(), new ArrayList<>());
        apps.get(appActivity.getBundle()).add(appActivity);
      }
      firstAt = Math.min(firstAt, dp.getFirstAt());
    }

    for (List<AppActivity> appActivities : apps.values()) {
      appActivityList.add(mergeAppList(appActivities));
    }

    return DeviceProfile.newBuilder() //
        .setDeviceId(dps.iterator().next().getDeviceId()) //
        .addAllApp(appActivityList) //
        .addAllGeo(geos) //
        .setFirstAt(firstAt) //
        .build();
  }
}
