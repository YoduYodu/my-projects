package edu.usfca.dataflow;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultiset;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;

public class __TestHelper {

  public static void printB64All(Iterable<? extends Message> pc) {
    for (Message msg : pc) {
      System.out.println(ProtoUtils.encodeMessageBase64(msg));
    }
  }

  public static void printJSONAll(Iterable<? extends Message> pc) throws InvalidProtocolBufferException {
    for (Message msg : pc) {
      System.out.println(ProtoUtils.getJsonFromMessage(msg, true));
    }
  }

  public static DeviceId getCanonicalId(DeviceId id) {
    return id.toBuilder().setUuid(id.getUuid().toLowerCase()).build();
  }

  public static DeviceProfile getDpWithCanonicalId(DeviceProfile _dp) {
    return _dp.toBuilder().setDeviceId(getCanonicalId(_dp.getDeviceId())).build();
  }

  static Function<DeviceProfile, DeviceId> idFromDpFunc = x -> getCanonicalId(x.getDeviceId());

  public static boolean areDeviceProfileSetsEqual(Set<DeviceProfile> _expected, Set<DeviceProfile> _actual)
      throws InvalidProtocolBufferException {
    // DeviceId's UUID is case-insensitive. Handle that here.
    Map<DeviceId, DeviceProfile> expected = _expected.stream().collect(Collectors.toMap(idFromDpFunc, dp -> dp));
    Map<DeviceId, DeviceProfile> actual = _actual.stream().collect(Collectors.toMap(idFromDpFunc, dp -> dp));

    // Check if the key sets agree.
    if (!expected.keySet().equals(actual.keySet())) {
      return false;
    }
    for (DeviceId id : expected.keySet()) {
      DeviceProfile expDp = expected.get(id), actDp = actual.get(id);
      boolean result = true;

      result &= (new ImmutableMultiset.Builder<GeoActivity>().addAll(expDp.getGeoList()).build())
          .equals((new ImmutableMultiset.Builder<GeoActivity>().addAll(actDp.getGeoList()).build()));

      result &= (new ImmutableMultiset.Builder<AppActivity>().addAll(expDp.getAppList()).build())
          .equals((new ImmutableMultiset.Builder<AppActivity>().addAll(actDp.getAppList()).build()));

      result &= expDp.toBuilder().clearGeo().clearApp().build().equals(actDp.toBuilder().clearGeo().clearApp().build());

      if (!result) {
        System.out.println("Different Device Profiles found: ");
        System.out.println(ProtoUtils.getJsonFromMessage(expDp, true));
        System.out.println(ProtoUtils.getJsonFromMessage(actDp, true));
        System.out.println(ProtoUtils.encodeMessageBase64(expDp));
        System.out.println(ProtoUtils.encodeMessageBase64(actDp));
        return false;
      }
    }

    return true;
  }
}
