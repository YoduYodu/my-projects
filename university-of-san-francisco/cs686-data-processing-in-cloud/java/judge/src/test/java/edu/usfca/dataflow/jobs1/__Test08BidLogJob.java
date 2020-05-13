package edu.usfca.dataflow.jobs1;

import static edu.usfca.dataflow.__TestHelper.areDeviceProfileSetsEqual;
import static edu.usfca.dataflow.__TestHelper.getCanonicalId;
import static edu.usfca.dataflow.__TestHelper.getDpWithCanonicalId;
import static edu.usfca.dataflow.__TestHelper.printJSONAll;
import static edu.usfca.dataflow.transforms.SuspiciousIDs.getSuspiciousIDs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.beam.sdk.io.TFRecordIO;
import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.usfca.dataflow.jobs1.BidLogJob.BidLog2DeviceProfile;
import edu.usfca.dataflow.transforms.AppProfiles.ComputeAppProfiles;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;

/**
 * This class tests your pipeline's correctness by using 65 BidLogs (which are all valid).
 */
public class __Test08BidLogJob {
  @Rule
  public Timeout timeout = Timeout.millis(5000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  // NOTE: It may be a good idea to decode these Base64-encoded proto messages (BidLogs),
  // and print them to the console so you can see the contents.
  static String[] bidLogsB64 = new String[] {
      "CoABCg5pZDA5MDgwLTU4ODI0MSIKQghhcHAuMTIzNCo3IgkaA3VzYSICQ0FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY2IIYmExNjY2MzSCAQhiYTI2MjM1OYoBCGJhNjUzOTc5igEIYmE1MzgwODUQFRig8wIgiPYHKAEw8So=",
      "CmoKDmlkMDI3OTItNTgzMzE2IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJjYXIDaW9zogEkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjYghiYTQ0MTU2MooBCGJhODA2MzM1EAIY65oFIKuSDygBMIZM",
      "CnUKDmlkMDI1ODItOTk0MDk5IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJDQXIDSU9TogEkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjYghiYTIyMDI5MoIBCGJhMzE5NTIwigEIYmExODAyMDUQFRij0QQggKsNKAEw+IEC",
      "CoABCg5pZDA2MDg3LTEwNjU1MyIKQghhcHAuMTIzNCo3IgkaA3VzYSICY2FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY2oIYmE2NDQyOTOCAQhiYTY5NDIwOIoBCGJhNzAzNTE1igEIYmE0MDIzNDEQAhikZyDK0wYoATDOgAE=",
      "CmsKDmlkMDE4MzItODAxMzY0IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJueXIDaW9zogEkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5aghiYTM1NDI4NIoBCWJhMTE5NTY0MBAVGI+cBSDkpQooATD4jQE=",
      "CmAKDmlkMDAzODQtMDMyMTA1IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJOWXIDaU9zogEkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5igEIYmEyNTM4MDQQAhiD3AMglt0LKAEw5r4B",
      "CmsKDmlkMDc4NjMtODk5MTA0IgpCCGFwcC4xMjM0KjciCRoDVVNBIgJueXIDaW9zogEkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5agliYTExNTMwODeCAQhiYTkwMzUyNBACGOT9BCCdlQwoATCFZg==",
      "CngKDmlkMDY0OTktMDU2MzM5IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJDQXIDaW9zogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3ggEJYmExMjA1NDI1ggEIYmE5MzMyMjeKAQliYTEyMzgwNTUQFhjVxAMglasIKAEw44UB",
      "CnUKDmlkMDg0NTQtOTczMTE0IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJjYXIDaW9zogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3agliYTEyMTA0MzCCAQhiYTE5NTg4NIoBB2JhOTI1MzQQAhim0gMgtKEPKAEwqqEB",
      "Cl8KDmlkMDk4MTQtNTk0NzY1IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJDQXIDSU9TogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3YghiYTM1NDU3MBAWGKS7BCCBmw0oATC6Dw==",
      "Cn8KDmlkMDE2NjktNzc3NzMxIgpCCEFwcC4xMjM0KjciCRoDdXNhIgJjYXIDaW9zogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3aghiYTE3Mzc3NmoHYmE5NDk4NIIBCGJhNTE5NjQ3ggEJYmExMjQ2NDc1EAIYyJcEIILdCCgBMKLWAQ==",
      "CnQKDmlkMDE3MTAtMjY2MTQxIgpCCEFwcC4xMjM0KjciCRoDdXNhIgJueXIDaW9zogEkMzEwNDIxOWEtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhYghiYTI4NTU0MWIIYmE1Njc2NjGCAQhiYTM3Nzc5MhAVGOyrASCuiAooATCpkwE=",
      "CmsKDmlkMDgxNzMtNzcwNzI1IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJOWXIDaU9zogEkMzEwNDIxOWEtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhagliYTEwNTEyMTCCAQhiYTk0NjA1OBAJGJqwAyCr5wwoATDOQA==",
      "CpQBCg5pZDA4MzIxLTU5MzE3NSIKQghBcHAuMTIzNCo3IgkaA1VTQSICbnlyA2lPU6IBJDMxMDQyMTlhLTExYTYtNGQ0ZS05NGRhLWU4ZTM3YjI3ZDAzYWIIYmE2MjI3ODRiCWJhMTEyMDgzMGoIYmE1ODYyNDNqCGJhMzQ5MTA4ggEIYmEzMzg3NzWKAQhiYTMxMTQxNhACGJrlBCDfog8oATC1nAE=",
      "CmQKDmlkMDY0MjgtNTIxMzI5Ig5CDEFwcC5mc29jaWV0eSo3IgkaA3VzYSICQ0FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY4IBCGJhNjM1MDc3EBUYgaYBIIqiCSgBMJ7jAQ==",
      "CmQKDmlkMDMxNjYtNjc5NTU2Ig5CDEFwcC5mc29jaWV0eSo3IgkaA3VzYSICY2FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY4oBCGJhNTA1MjgzEAIYg9IEILn/BigBMNIc",
      "CpgBCg5pZDA5MTA4LTA1ODgwNSIOQgxBcHAuZnNvY2lldHkqNyIJGgN1c2EiAm55cgNpb3OiASQxODM1ZWQ1My0zMDdmLTRhNDUtOGQ4My1mYjQ0OGE4NWI5MjliCWJhMTEyODI3NGIIYmExMDYzNDdqCGJhNzQxNDkwaghiYTg0NDY2NIIBCGJhODQzODgyigEIYmE1NTg5OTMQFRj83AIgrvsMKAEw0HI=",
      "Cm8KDmlkMDM1NzEtNjIyOTgyIg5CDEFwcC5mc29jaWV0eSo3IgkaA3VzYSICTllyA2lPc6IBJDE4MzVlZDUzLTMwN2YtNGE0NS04ZDgzLWZiNDQ4YTg1YjkyOWIIYmExMzE1NTCKAQliYTExMDAwNjYQAhjbsgIgmvIQKAEwsCk=",
      "CoQBCg5pZDAzMjA3LTk2NTE3OCIOQgxBcHAuZnNvY2lldHkqNyIJGgN1c2EiAkNBcgNpb3OiASQyZDQwNWU5YS01NmNjLTRkNWEtOGRjYy04NTc3OGQ4YmY3ZDdiCWJhMTA0MDE2NWIJYmExMTYxMjE2aghiYTI4MjY0OYoBCGJhNDA4Mjg1EBYY1YoFILGdDigBMIKSAg==",
      "ClkKDmlkMDY5MTYtODU4NjExIg5CDEFwcC5mc29jaWV0eSo3IgkaA3VzYSICY2FyA2lvc6IBJDJkNDA1ZTlhLTU2Y2MtNGQ1YS04ZGNjLTg1Nzc4ZDhiZjdkNxACGNXyBCCN1Q4oATD6cQ==",
      "CmMKDmlkMDQ3MDMtODUwNTc3Ig5CDEFwcC5mc29jaWV0eSo3IgkaA3VzYSICbnlyA2lvc6IBJDMxMDQyMTlhLTExYTYtNGQ0ZS05NGRhLWU4ZTM3YjI3ZDAzYWoIYmE0Mzc2ODUQFRiYcSCXmQ4oATDQlAE=",
      "Co4BCg5pZDAyNTQ2LTcwMTQyMyIOQgxBcHAuZnNvY2lldHkqNyIJGgN1c2EiAk5ZcgNpT3OiASQzMTA0MjE5YS0xMWE2LTRkNGUtOTRkYS1lOGUzN2IyN2QwM2FiCGJhOTc2NjExaghiYTI5ODY4MmoJYmExMjc2ODM3ggEIYmExNDAzODOKAQhiYTc2MzM1NhAJGNi5AyCktwkoATDAqQE=",
      "CnIKDmlkMDI3ODQtNTY3NzczIg5CDEFwcC5mc29jaWV0eSo7IgkaA1VTQSICbnlyB2FuZHJvaWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmViCGJhNDYyOTcyagliYTEyMDk5MjEQAhjpqQMghswPKAEwwnY=",
      "CmkKDmlkMDk4NDAtNjk3MDc0Ig5CDEFwcC5mc29jaWV0eSo7IgkaA1VzYSICbnlyB2FuZHJvaWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmWKAQliYTEwNzM4NDkQCRjAqAMgia8MKAEwp+sB",
      "CnIKDmlkMDk5NjgtODY3MDQ4Ig5CDEFwcC5mc29jaWV0eSo7IgkaA3VzYSICbnlyB0FuZHJvaWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmViCGJhMTI0NzI3ggEIYmExODMxMTMQARil6gQguawNKAEw9ckB",
      "CnMKDmlkMDczOTYtMzkxNTg4Ig5CDEFwcC5mc29jaWV0eSo7IgkaA3VzQSICbnlyB2FOZHJvaWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmWCAQhiYTE0OTM3OIoBCGJhMjc4MTM5EAQYx+gCIOLiDygBMPSIAQ==",
      "CocBCg5pZDA2NTY0LTQ3NjgzNiIOQgxBcHAuZnNvY2lldHkqPCIKGgNCUkEiA3NhbnIHYW5Ecm9pZKIBJDQzNTEwYjVlLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZWIHYmE3OTk3M2IIYmE1MTIwOTeCAQhiYTQ4NzU2OIoBCGJhNjkzOTI3EAIYgaoDINzBDCgBMJpC",
      "CogBCg5pZDA0MDMwLTk5MjA4MyIOQgxBcHAuZnNvY2lldHkqPCIKGgNNRVgiA2NhbnIHYW5kUm9pZKIBJDQzNTEwYjVlLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZWIIYmE0ODg4NTZqCGJhNTUyMTk0ggEIYmE1OTYyMzaKAQhiYTM5OTIzORAVGPaZBCDnjA0oATClHg==",
      "CmoKDmlkMDA5OTAtNTg4MzkyIg5CDEFwcC5mc29jaWV0eSo9IgsaA0tPUiIEamVqdXIHYW5kck9pZKIBJDQzNTEwYjVlLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZYoBCGJhNjg4MjA1EAIY/PoDIJecDygBMPGMAg==",
      "CnIKDmlkMDk0ODctMjMwMDkwIg5CDEFwcC5mc29jaWV0eSo7IgkaA0NBTiICT05yB2FuZHJvSWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmViCGJhNzg4MDAzggEIYmE2NTUxNTEQAhjjhgEg8tEOKAEw/ks=",
      "ClwKDmlkMDc0OTgtNDU4Mzc3Ig5CDEFwcC5mc29jaWV0eSo6IggaA0ZSQSIBcHIHYW5kcm9pRKIBJDQzNTEwYjVlLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZRAWGPrvBCDX5g4oATCOwAE=",
      "CnQKDmlkMDM0MDUtNzMzNTQxIg5CDEFwcC5mc29jaWV0eSo7IgkaA0RFTiICZnJyB0FORHJvaWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmWCAQliYTExMTk3ODaKAQhiYTE5MjY4NhACGLn5AiClqA0oATCHhwI=",
      "CmsKDmlkMDQzNjItMjYyNzQ0Ig5CDEFwcC5mc29jaWV0eSo/Ig0aA0dCUiIGbG9uZG9ucgdhbmRST0lkogEkNDM1MTBiNWUtOGQ3Ny00NWQ2LWJhNWMtNmJmY2ZhZmMyOWJlaghiYTM0NzE3ORACGMiYBCCzkQsoATC2GA==",
      "CmwKDmlkMDEzMDItNDA2MDQ0Ig5CDEFwcC5mc29jaWV0eSo+IgwaA0lUQSIFbWlsYW5yB2FuZHJvaWSiASQ0MzUxMGI1ZS04ZDc3LTQ1ZDYtYmE1Yy02YmZjZmFmYzI5YmWCAQliYTEwNDc2MDYQAhjXmQQgyJUJKAEwoAo=",
      "CoABCg5pZDAxMTQwLTkxNjc0NyIOQgxBcHAuZnNvY2lldHkqPiIMGgNKUE4iBWt5b3RvcgdhbmRyb2lkogEkNDM1MTBiNWUtOGQ3Ny00NWQ2LWJhNWMtNmJmY2ZhZmMyOWJlaghiYTIwMjE0MGoIYmEzMzQzMzmKAQliYTEyMTYzMzcQAhj5nAUgx/8NKAEwi3E=",
      "CncKDmlkMDkyODctMjg0NjcyIg5CDEFwcC5mc29jaWV0eSo/Ig0aA0NITiIGcGVraW5ncgdhbmRyb2lkogEkNDM1MTBiNWUtOGQ3Ny00NWQ2LWJhNWMtNmJmY2ZhZmMyOWJlagliYTExNzYzMzKKAQhiYTQzMTE2MxACGM2MAyC+4QcoATDXMQ==",
      "CmgKDmlkMDE3NDQtNzQyMDEyIg9CDUFwcC5mc29jaWV0eTEqOyIJGgNVU0EiAm55cgdhbmRyb2lkogEkNTc4MTk2YTYtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4YghiYTc5Njg0ORACGNyvBSCQmgkoATC8hwE=",
      "Cn4KDmlkMDU2NDktNDIzNDQxIg9CDUFwcC5mc29jaWV0eTIqOyIJGgNVU0EiAm55cgdhbmRyb2lkogEkNTc4MTk2YTYtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4YghiYTUzNjg2MmoIYmE5MDAxMjOCAQliYTEwOTIyNjAQCRjh5QIg7cQHKAEwlqcB",
      "CnIKDmlkMDg4NjMtMzEwOTEzIg9CDUFwcC5mc29jaWV0eTMqOyIJGgNVU0EiAm55cgdBbmRyb2lkogEkNTc4MTk2YTYtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4YghiYTI5OTI0OGIIYmE0MDE1NzkQARi8byCulBAoATD15AE=",
      "CnQKDmlkMDc1MjktNzA5MTcxIg9CDUFwcC5mc29jaWV0eTQqOyIJGgNVU0EiAm55cgdhTmRyb2lkogEkNTc4MTk2YTYtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4YgliYTEwMzI0NDhqCWJhMTA1NDUzOBAEGMy4ASDX7A4oATC52wE=",
      "CnwKDmlkMDE4MjItMzQ1ODg4Ig9CDUFwcC5mc29jaWV0eTUqOyIJGgNVU0EiAk5ZcgdhbkRyb2lkogEkNTc4MTk2YTYtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4YghiYTM5NTY2N2IIYmE1NjA2NDhqCGJhNDY0ODA5EAIYy8UDILO0BygBMOnVAQ==",
      "Cl0KDmlkMDg2ODAtNjIwMjU1Ig5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDgQARi97AMg9ekIKAEw0oUB",
      "CnwKDmlkMDMxMTYtMjU3MzA1Ig5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCGJhNTg1MDg0aghiYTQ0MDM0MYIBCGJhMzE5MjY2EAEY274EIJjoDCgBMO3mAQ==",
      "Cl0KDmlkMDk3OTYtODU0NjAxIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDgQAhjUZyDk/wsoATCcFQ==",
      "Cn4KDmlkMDcwMTYtODA0ODUwIg5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCWJhMTEyNjU1MGIIYmE0NzI0ODSKAQliYTExOTc0MDkQAhio2wQg6+IPKAEwvOYB",
      "CmgKDmlkMDc3MDUtNDQ0MTYwIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCWJhMTAxMDQxOBADGPbXASCanA4oATD/Cg==",
      "CogBCg5pZDA1MTczLTE0MTY3NyIOQgxBcHAuYWJ1c2luZzIqOyIJGgNVU0EiAldBcgdhbmRyb2lkogEkNjc5ZDgyYjMtOGFhYi00OTk1LTllYWUtZWM3ZjkyMjUxOWQ4YgliYTEwNDQ2MzdqCGJhMjIzMzU1ggEIYmE4MjMxMzmKAQhiYTI5NzI4MhADGInvAyDsiQsoATDnMw==",
      "CnMKDmlkMDQzNTgtODczMjAyIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDiCAQhiYTI3Nzk2OYoBCGJhOTYxNDIyEAQY9dUBIL3XDSgBMJ1f",
      "CqkBCg5pZDAwMTcwLTM3NTc0MyIOQgxBcHAuYWJ1c2luZzIqOyIJGgNVU0EiAldBcgdhbmRyb2lkogEkNjc5ZDgyYjMtOGFhYi00OTk1LTllYWUtZWM3ZjkyMjUxOWQ4YghiYTQ1NDI4N2oIYmE5MTk3MDVqCWJhMTAwNzc3MYIBCGJhNDA0OTAzggEIYmEzNjk3MjOKAQliYTExMDgzMzaKAQhiYTM3OTIwOBAEGMToAyD50AooATDaPw==",
      "CnsKDmlkMDA4NDktMzkyMDIwIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiB2JhNTAwMDlqCGJhNjY4MDk4igEIYmExMDY0MTIQBRj6tAIgxvULKAEwziQ=",
      "CnIKDmlkMDU3NTktNDc3NzY3Ig5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhqCGJhNDAzMTUyigEIYmEzMDc0MTMQBRiA3wQg86MGKAEwql0=",
      "CnIKDmlkMDUwMDAtNDI0NjE5Ig5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCGJhODc2MzIyigEIYmE4NTM5OTcQBhjR9wIg8f0QKAEwul0=",
      "CmgKDmlkMDg1NDUtOTEwMDcxIg5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDiCAQhiYTcyODI2NxAGGPm7AyCtiwsoATDiCQ==",
      "CmgKDmlkMDc1OTYtMzU5MTIxIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDiKAQhiYTkyNTA5MxAHGPXDASCE9A4oATCS0gE=",
      "Cn0KDmlkMDU5ODYtNTE5MTA2Ig5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCGJhNTk4OTc2agliYTEwMDczODWKAQhiYTg4NTkxORAHGOzRAiDg7AwoATCbTA==",
      "CnMKDmlkMDYzNzEtNzIxODI0Ig5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCWJhMTA3MDI1MooBCGJhMTQ1Njc5EAgYlYIDILqsDygBMMDiAQ==",
      "Cn0KDmlkMDE5ODMtMDYzMTkzIg5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCGJhOTU2Mzk3aghiYTk1MDY5MoIBCWJhMTAwODY2NhAIGOPSAiD5hgkoATCqxAE=",
      "CnQKDmlkMDI1NzEtNDQyNzcwIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhqCWJhMTA2Njk1OIIBCWJhMTAyOTQ1MhAJGI79AyDimQ8oATCWJw==",
      "CnEKDmlkMDI5NTQtNzQ3Nzk5Ig5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiB2JhNjg4NDGKAQhiYTU0MTA2NRAJGIbaAyDCyBAoATCHaQ==",
      "CmgKDmlkMDE4ODAtNDYxMjUzIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDiKAQhiYTU1MDc4NBAKGMP9AyCD2AcoATDUCg==",
      "CnMKDmlkMDQ3MzgtOTA2MjQyIg5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCGJhNjY3ODAzigEJYmExMDg5Njc5EAoYpnkgnvINKAEwg14=",
      "Cn4KDmlkMDQyNzctNjgyNjkyIg5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDiCAQhiYTc1MzEwNYoBCGJhMzYzMzQ0igEIYmE1NzUzNDEQFRj5ZSDA0A4oATC6/gE=",
      "Cl0KDmlkMDYyMDItNTQwNDAyIg5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDgQFRiNjQQgpM0GKAEwt6sB",
      "CnIKDmlkMDUzMjEtMjAwMjU0Ig5CDEFwcC5hYnVzaW5nMSo7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDhiCGJhNDA3NjQwigEIYmE1MjM3NDAQFhjT4QEg8fwPKAEwotsB",
      "CmgKDmlkMDI0NDgtNDU4NzUxIg5CDEFwcC5hYnVzaW5nMio7IgkaA1VTQSICV0FyB2FuZHJvaWSiASQ2NzlkODJiMy04YWFiLTQ5OTUtOWVhZS1lYzdmOTIyNTE5ZDiKAQhiYTE3MjU2MBAWGLSTASD63QsoATC+ugE="};

  Function<String, DeviceProfile> dpParser = x -> {
    try {
      return getDpWithCanonicalId(DeviceProfile.parseFrom(Base64.getDecoder().decode(x)));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  };

  Function<String, AppProfile> apParser = x -> {
    try {
      return AppProfile.parseFrom(Base64.getDecoder().decode(x));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  };


  @Test
  public void testDeviceProfiles() {
    Set<DeviceProfile> expectedDeviceProfiles = Arrays.asList(
        "CigIAhIkMDEwNEYyRDAtOTI4My00QzI2LUJEODAtRDEyREI4N0ZFMzNDKh0KCGFwcC4xMjM0EKRnGOuaBTIECAIQAjIECBUQAioiCgxBcHAuZnNvY2lldHkQgaYBGIPSBDIECAIQATIECBUQAUikZ1DrmgVaCQoDdXNhEgJDQVoJCgN1c2ESAmNh",
        "CigIAhIkMTgzNUVENTMtMzA3Ri00QTQ1LThEODMtRkI0NDhBODVCOTI5KiIKDEFwcC5mc29jaWV0eRDbsgIY/NwCMgQIAhABMgQIFRABKh4KCGFwcC4xMjM0EIPcAxiPnAUyBAgCEAIyBAgVEAFI27ICUI+cBVoJCgNVU0ESAm55WgkKA3VzYRICbnlaCQoDdXNhEgJOWQ==",
        "CigIAhIkMzEwNDIxOUEtMTFBNi00RDRFLTk0REEtRThFMzdCMjdEMDNBKiEKDEFwcC5mc29jaWV0eRCYcRjYuQMyBAgJEAEyBAgVEAEqJAoIQXBwLjEyMzQQ7KsBGJrlBDIECAIQATIECAkQATIECBUQAUiYcVCa5QRaCQoDVVNBEgJueVoJCgN1c2ESAm55WgkKA3VzYRICTlk=",
        "CigIAhIkMkQ0MDVFOUEtNTZDQy00RDVBLThEQ0MtODU3NzhEOEJGN0Q3KiIKDEFwcC5mc29jaWV0eRDV8gQY1YoFMgQIAhABMgQIFhABKh4KCEFwcC4xMjM0ENXEAxikuwQyBAgWEAIyBAgCEAJI1cQDUNWKBVoJCgN1c2ESAkNBWgkKA3VzYRICY2E=",
        "CigIARIkNjc5RDgyQjMtOEFBQi00OTk1LTlFQUUtRUM3RjkyMjUxOUQ4Kl0KDEFwcC5hYnVzaW5nMRD5ZRjD/QMyBAgWEAEyBAgEEAEyBAgIEAEyBAgVEAEyBAgHEAEyBAgDEAEyBAgGEAEyBAgFEAEyBAgCEAEyBAgBEAEyBAgKEAEyBAgJEAEqXQoMQXBwLmFidXNpbmcyEKZ5GIDfBDIECBYQATIECBUQATIECAMQATIECAcQATIECAoQATIECAYQATIECAIQATIECAUQATIECAQQATIECAEQATIECAkQATIECAgQAUj5ZVCA3wRaCQoDVVNBEgJXQQ==",
        "CigIARIkNTc4MTk2QTYtNzIyRS00NDFBLUIwMTctREU2OTJDQ0RGNUI4Kh0KDUFwcC5mc29jaWV0eTIQ4eUCGOHlAjIECAkQASodCg1BcHAuZnNvY2lldHkxENyvBRjcrwUyBAgCEAEqHQoNQXBwLmZzb2NpZXR5NRDLxQMYy8UDMgQIAhABKh0KDUFwcC5mc29jaWV0eTQQzLgBGMy4ATIECAQQASobCg1BcHAuZnNvY2lldHkzELxvGLxvMgQIARABSLxvUNyvBVoJCgNVU0ESAk5ZWgkKA1VTQRICbnk=",
        "CigIARIkNDM1MTBCNUUtOEQ3Ny00NUQ2LUJBNUMtNkJGQ0ZBRkMyOUJFKjoKDEFwcC5mc29jaWV0eRDjhgEY+ZwFMgQICRABMgQIAhAJMgQIFhABMgQIFRABMgQIBBABMgQIARABSOOGAVD5nAVaCQoDREVOEgJmcloJCgNVc2ESAm55WgwKA0pQThIFa3lvdG9aCwoDS09SEgRqZWp1WgkKA1VTQRICbnlaCQoDQ0FOEgJPTloKCgNCUkESA3NhbloKCgNNRVgSA2NhbloMCgNJVEESBW1pbGFuWg0KA0NIThIGcGVraW5nWggKA0ZSQRIBcFoNCgNHQlISBmxvbmRvbloJCgN1c2ESAm55WgkKA3VzQRICbnk=")
        .stream().map(dpParser).collect(Collectors.toSet());

    PCollection<byte[]> rawData = tp.apply(Create.of(Arrays.asList(bidLogsB64)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((String b) -> Base64.getDecoder().decode(b)));

    PCollection<DeviceProfile> deviceProfiles = rawData.apply(new BidLog2DeviceProfile());

    PAssert.that(deviceProfiles).satisfies(out -> {
      assertEquals(7, Iterables.size(out));
      Set<DeviceProfile> actualDeviceProfiles =
          StreamSupport.stream(out.spliterator(), false).map(x -> getDpWithCanonicalId(x)).collect(Collectors.toSet());
      try {
        assertTrue(areDeviceProfileSetsEqual(expectedDeviceProfiles, actualDeviceProfiles));
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
      return null;
    });

    tp.run();
  }

  @Test
  public void testAppProfiles() {
    Set<AppProfile> expectedAppProfiles = Arrays.asList("Cg1BcHAuZnNvY2lldHk0EAEaBAgEEAE=",
        "CgxBcHAuYWJ1c2luZzIQARoECBUQARoECBYQARoECAIQARoECAMQARoECAQQARoECAUQARoECAkQARoECAEQARoECAoQARoECAgQARoECAYQARoECAcQAQ==",
        "Cg1BcHAuZnNvY2lldHkyEAEaBAgJEAE=", "CgxBcHAuZnNvY2lldHkQBRoECBUQBBoECBYQAhoECAkQAhoECAEQARoECAIQBBoECAQQAQ==",
        "Cg1BcHAuZnNvY2lldHk1EAEaBAgCEAE=", "Cg1BcHAuZnNvY2lldHkzEAEaBAgBEAE=", "CghhcHAuMTIzNBACGgQIFRACGgQIAhAC",
        "CghBcHAuMTIzNBACGgQIFRABGgQIFhABGgQIAhACGgQICRAB",
        "CgxBcHAuYWJ1c2luZzEQARoECAoQARoECAMQARoECAIQARoECAEQARoECAQQARoECBUQARoECAgQARoECAcQARoECAYQARoECAUQARoECAkQARoECBYQAQ==",
        "Cg1BcHAuZnNvY2lldHkxEAEaBAgCEAE=").stream().map(apParser).collect(Collectors.toSet());

    PCollection<byte[]> rawData = tp.apply(Create.of(Arrays.asList(bidLogsB64)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((String b) -> Base64.getDecoder().decode(b)));

    PCollection<AppProfile> appProfiles = rawData.apply(new BidLog2DeviceProfile()).apply(new ComputeAppProfiles());

    PAssert.that(appProfiles).satisfies(out -> {
      try {
        printJSONAll(out);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
      assertEquals(10, Iterables.size(out));
      assertEquals(expectedAppProfiles, StreamSupport.stream(out.spliterator(), false).collect(Collectors.toSet()));
      return null;
    });

    tp.run();
  }

  @Test
  public void testSuspiciousIDs() {

    PCollection<byte[]> rawData = tp.apply(Create.of(Arrays.asList(bidLogsB64)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((String b) -> Base64.getDecoder().decode(b)));

    PCollection<DeviceProfile> deviceProfiles = rawData.apply(new BidLog2DeviceProfile());
    PCollection<AppProfile> appProfiles = deviceProfiles.apply(new ComputeAppProfiles());

    PAssert
        .that(getSuspiciousIDs(deviceProfiles, appProfiles, 4, 3, 8, 10)
            .apply(MapElements.into(TypeDescriptor.of(DeviceId.class)).via((DeviceId id) -> getCanonicalId(id))))
        .containsInAnyOrder(
            DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid("679d82b3-8aab-4995-9eae-ec7f922519d8").build(), //
            DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid("43510b5e-8d77-45d6-ba5c-6bfcfafc29be").build(), //
            DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid("578196a6-722e-441a-b017-de692ccdf5b8").build());

    tp.run();
  }
}
