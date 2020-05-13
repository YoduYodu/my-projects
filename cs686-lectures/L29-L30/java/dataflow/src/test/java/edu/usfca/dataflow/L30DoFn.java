package edu.usfca.dataflow;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class L30DoFn {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  /**
   * (1) Before you begin: Visit: https://developer.github.com/v3/
   *
   * (2) Get Github API Token: https://developer.github.com/v3/auth/ And change "MY_TOKEN" below (it should be 40-char
   * long; see this:
   * https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line#creating-a-token).
   *
   * (3) Pick one of your previous lab/proj repos, and create dummy commits (at least 10-12 commits). And change
   * "MY_REPO" below.
   *
   * (4) Obtain SHAs of those commits (try something like "git log | grep 'commit ' | cut -d' ' -f2" from command-line
   * if you are on Mac/Linux; I don't use Windows...). And change "MY_COMMITS" below.
   * 
   */
  final static String MY_TOKEN = "TODO"; // <- TODO
  final static String MY_REPO = "cs686-lab06-TODO"; // <- TODO
  final static List<String> MY_COMMITS = Arrays.asList("c60d67957dafed4fae92ed9c90ee3ce42c66aaca",
      "35ac2a1b4c960dfc656207f8a00b763ee6a52538", "75c9cc6129361b1608df42747fdcf3e585bfe7b3",
      "404ec673a764a5ef77be0c522b2c05290615950c", "628fa759b91efc6343412df852a447282be3c8d6",
      "45ce9c3d8d5d4b780064f25f88576f157e3113c7", "d94e5ba28595be27d934978f2fca089b3d4d0f9a",
      "b043411d883fe8ac311d278a46cf40744486df73", "e1a79cf7854d3cef2eeb97a7fc4dbc02b67f45b5"); // <- TODO

  final static String HOST_API = "api.github.com";

  static final String _USER_AGENT = "USF/CS-ROCKS"; //$NON-NLS-1$

  protected static final String METHOD_GET = "GET"; //$NON-NLS-1$

  static final String AUTH_TOKEN = "token"; //$NON-NLS-1$
  protected static final String HEADER_AUTHORIZATION = "Authorization"; //$NON-NLS-1$
  protected static final String HEADER_USER_AGENT = "User-Agent"; //$NON-NLS-1$
  protected static final String HEADER_ACCEPT = "Accept"; //$NON-NLS-1$

  // Adopted from: https://github.com/eclipse/egit-github
  static InputStream getStream(HttpURLConnection request) throws IOException {
    if (request.getResponseCode() < HTTP_BAD_REQUEST)
      return request.getInputStream();
    else {
      InputStream stream = request.getErrorStream();
      return stream != null ? stream : request.getInputStream();
    }
  }

  // Adopted from: https://github.com/eclipse/egit-github
  static String getNextUri(List<String> links) {
    String nextUri = "";
    if (links != null && links.size() > 0) {
      System.out.format("\t%s\n", links.get(0));
      String linkHeader = links.get(0);
      if (linkHeader.contains("rel=\"next\"")) {
        String[] toks = linkHeader.split(",");
        for (String tok : toks) {
          if (tok.contains("rel=\"next\"")) {
            tok = tok.substring(0, tok.indexOf('>'));
            tok = tok.substring(tok.indexOf('<') + 1);
            nextUri = tok;
            break;
          }
        }
      }
    }
    return nextUri;
  }

  public static class GetCommitDate01 extends DoFn<String, KV<String, KV<String, Long>>> {

    @ProcessElement
    public void process(ProcessContext c) throws IOException, URISyntaxException {
      Instant start = Instant.now();
      // GET /repos/:owner/:repo/commits
      final String targetSha = c.element();

      String uri = String.format("https://%s/repos/cs-rocks/%s/commits", HOST_API, MY_REPO);
      URL url = new URL(uri);
      while (true) {
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod(METHOD_GET);

        request.setRequestProperty(HEADER_AUTHORIZATION, AUTH_TOKEN + " " + MY_TOKEN);
        request.setRequestProperty(HEADER_USER_AGENT, _USER_AGENT);
        request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta+json"); //$NON-NLS-1$

        if (request.getResponseCode() != 200) {
          System.out.format("response: %s (%d)     %s\n", request.getResponseMessage(), request.getResponseCode(),
              url.toURI().toString());
        }

        InputStream stream = getStream(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 8192);
        JsonParser jsonParser = new JsonParser();
        JsonElement root = jsonParser.parse(reader);
        if (request.getResponseCode() != 200) {
          System.out.format("%s\n", root.toString());
        } else {
          for (Iterator<JsonElement> it = root.getAsJsonArray().iterator(); it.hasNext();) {
            JsonObject item = it.next().getAsJsonObject();
            final String sha = item.getAsJsonPrimitive("sha").getAsString();
            if (targetSha.equals(sha)) {
              JsonObject commit = item.getAsJsonObject("commit");
              c.output(KV.of(sha, KV.of(commit.getAsJsonObject("author").getAsJsonPrimitive("date").getAsString(),
                  Instant.now().getMillis() - start.getMillis())));
              return;
            }
          }
        }
        String nextUri = getNextUri(request.getHeaderFields().get("Link"));
        if ("".equals(nextUri)) {
          break;
        }
        url = new URL(nextUri);
      }
    }
  }

  public static class GetCommitDate02 extends DoFn<String, KV<String, KV<String, Long>>> {

    Map<String, String> shaToDate = new HashMap<>();
    long startMillis;

    @Setup
    public void setup() throws IOException, URISyntaxException {
      startMillis = Instant.now().getMillis();
      // GET /repos/:owner/:repo/commits
      String uri = String.format("https://%s/repos/cs-rocks/%s/commits", HOST_API, MY_REPO);
      URL url = new URL(uri);
      while (true) {
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod(METHOD_GET);

        request.setRequestProperty(HEADER_AUTHORIZATION, AUTH_TOKEN + " " + MY_TOKEN);
        request.setRequestProperty(HEADER_USER_AGENT, _USER_AGENT);
        request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta+json"); //$NON-NLS-1$

        if (request.getResponseCode() != 200) {
          System.out.format("response: %s (%d)     %s\n", request.getResponseMessage(), request.getResponseCode(),
              url.toURI().toString());
        }

        InputStream stream = getStream(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 8192);
        JsonParser jsonParser = new JsonParser();
        JsonElement root = jsonParser.parse(reader);
        if (request.getResponseCode() != 200) {
          System.out.format("%s\n", root.toString());
        } else {
          for (Iterator<JsonElement> it = root.getAsJsonArray().iterator(); it.hasNext();) {
            JsonObject item = it.next().getAsJsonObject();
            final String sha = item.getAsJsonPrimitive("sha").getAsString();
            JsonObject commit = item.getAsJsonObject("commit");
            shaToDate.put(sha, commit.getAsJsonObject("author").getAsJsonPrimitive("date").getAsString());
          }
        }
        String nextUri = getNextUri(request.getHeaderFields().get("Link"));
        if ("".equals(nextUri)) {
          break;
        }
        url = new URL(nextUri);
      }
    }

    @ProcessElement
    public void process(ProcessContext c) {
      final String targetSha = c.element();
      c.output(KV.of(c.element(), KV.of(shaToDate.get(targetSha), Instant.now().getMillis() - startMillis)));
    }
  }

  @Test
  public void test01() {
    PAssert.that(tp.apply(Create.of(MY_COMMITS)).apply(ParDo.of(new GetCommitDate01()))).satisfies(out -> {
      for (KV<String, KV<String, Long>> kv : out) {
        System.out.format("%s at %s (%d)\n", kv.getKey(), kv.getValue().getKey(), kv.getValue().getValue());
      }
      return null;
    });

    tp.run();
  }

  @Test
  public void test02() {
    PAssert.that(tp.apply(Create.of(MY_COMMITS)).apply(ParDo.of(new GetCommitDate02()))).satisfies(out -> {
      for (KV<String, KV<String, Long>> kv : out) {
        System.out.format("%s at %s (%d)\n", kv.getKey(), kv.getValue().getKey(), kv.getValue().getValue());
      }
      return null;
    });

    tp.run();
  }
}
