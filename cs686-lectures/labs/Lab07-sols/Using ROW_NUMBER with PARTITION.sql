# task 3B

#This function was made possible due to this thread: https://stackoverflow.com/questions/26117179/sql-count-consecutive-days, I copied the idea and without it I would be lost
CREATE TEMP FUNCTION
  urldecode(url string) AS ((
    SELECT
      SAFE_CONVERT_BYTES_TO_STRING( ARRAY_TO_STRING(ARRAY_AGG(
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
        os = "ios";
        uuid = kv[1];
      break;
      case "gps_adid":
        os = "android";
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
      `sample_tiny.sample_large` # Change this to your table name.
    WHERE
      TRUE
      --       AND httprequest.remoteip LIKE '10%' # an arbitrary filter to reduce the size of output (useful for debugging).
      )),
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
  # Here is a sample query that produces the number of unique users (purchasers) per bundle.
  # If you have not completed "flatten" UDF, this query's result may be incorrect.

SELECT os, uuid
  FROM (SELECT  uuid,
          MIN(date) AS StartDate,
          MAX(date) AS EndDate,
          GroupingSet,
          os,
          bundle,
          COUNT(NULLIF(amount, 0)) AS Result
  FROM (SELECT
          uuid,
          os,
          MAX(amount) AS amount,
          date,
          bundle,
          date_add(date, INTERVAL -ROW_NUMBER() OVER(PARTITION BY uuid, os, bundle ORDER BY date) DAY) AS GroupingSet
          FROM (SELECT
                 info.os,
                 info.uuid,
                 date,
                 info.amount,
                 info.bundle,
                FROM  parsed_data)
          WHERE bundle = "id686" or bundle = "id486"
          GROUP BY uuid, os, bundle, date)
  GROUP BY uuid, os, GroupingSet, bundle
  HAVING Result >= 4
  ORDER BY uuid, StartDate)
