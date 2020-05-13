# task 3B
  # this temporary function was adopted from StackOverflow answer:
  # https://stackoverflow.com/a/51218333/6913214
  # Feel free to use it or write one yourself.
  # This part is mainy to show you how URL-encoded strings can be safely decoded.
CREATE TEMP FUNCTION
  urldecode(url string) AS ((
    SELECT
      SAFE_CONVERT_BYTES_TO_STRING(ARRAY_TO_STRING(ARRAY_AGG(
          IF
            (STARTS_WITH(y, '%'),
              FROM_HEX(SUBSTR(y, 2)),
              CAST(y AS BYTES))
          ORDER BY
            i ), b''))
    FROM
      UNNEST(REGEXP_EXTRACT_ALL(url, r"%[0-9a-fA-F]{2}|[^%]+")) AS y
    WITH
    OFFSET
      AS i ));
  # This temporary function is partially implemented, and thus you need to complete it.
  # Also, you do NOT have to use this function in your SQL query (it's up to you).
CREATE TEMP FUNCTION
  flatten(params ARRAY<string>)
  RETURNS STRUCT<bundle string,
  os string,
  uuid string,
  amount float64>
  LANGUAGE js AS """
  var bundle = ""
  var os = ""
  var uuid = ""
  var amount = 0.1;
  for(var i = 0; i < params.length; i++) {
    kv = params[i].split("=");
    switch(kv[0]) {
      case "bundle":
        bundle = kv[1];
      break;
      case "ios_idfa":
        os = "iOS";
        uuid = kv[1];
      break;
      case "gps_adid":
        os = "Android";
        uuid = kv[1];
      break;
      case "amount":
        amount = kv[1];
      break;
      default:
      break;
    }
  }
  return {bundle: bundle, os:os, uuid:uuid, amount: amount}
""";
WITH
  # this query expression produces two columns (and many rows) where the first column is "date"
  # (since we are only interested in calendar days of timestamp info),
  # and the second column is an array of strings (where each string is in the form "param=value").
  # You can feed this "url_params" into "flatten" UDF above to obtain a flattened struct that contains bundle/os/uuid/amount.
  # This is useful because each row (record) now contains date/bundle/os/uuid/amount.
  # See the next query expression (parsed_data) which does exactly this.
  data_with_params AS (
  SELECT
    DATE(ts) AS date,
    REGEXP_EXTRACT_ALL(url,r'(?:\?|&)((?:[^=]+)=(?:[^&]*))') AS url_params
  FROM (
    SELECT
      urldecode(httpRequest.requestUrl) AS url,
      timestamp AS ts,
    FROM
      `project_03.large`)),
  # If you decide to use "flatten" UDF and complete its implementation,
  # you can complete all tasks quite easily by querying against this parsed_data query expression.
  parsed_data AS (
  SELECT
    date,
    url_params,
    flatten(url_params) AS info
  FROM
    data_with_params)
  #------------------------------------------------------------
SELECT
  DISTINCT os,
  uuid,
FROM (
  SELECT
    *,
    DATE_SUB(date, INTERVAL RANK() OVER (PARTITION BY os, uuid, bundle ORDER BY date) DAY) AS group_set
  FROM (
    SELECT
      DISTINCT info.os AS os,
      info.uuid AS uuid,
      info.bundle AS bundle,
      date
    FROM
      parsed_data
    WHERE
      info.bundle = "id486"
      OR info.bundle = "id686"))
GROUP BY
  os,
  uuid,
  bundle,
  group_set
HAVING
  DATE_DIFF(MAX(date), MIN(date), DAY) >= 3
