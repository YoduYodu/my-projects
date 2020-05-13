# this temporary function was adopted from StackOverflow answer:
  # https://stackoverflow.com/a/51218333/6913214
  # Feel free to use it or write one yourself.
  # This part is mainy to show you how URL-encoded strings can be safely decoded.
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
      case "amount":
        amount = kv[1];
      break;
      case "gps_adid":
        os = "Android";
        uuid = kv[1];
      break;
      case "ios_idfa":
        os = "iOS";
        uuid = kv[1];
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
      `lab07.sample_large` # Change this to your table name.
    WHERE
      TRUE
      --       AND httprequest.remoteip LIKE '10%' # an arbitrary filter to reduce the size of output (useful for debugging).
      )),
  # If you decide to use "flatten" UDF and complete its implementation,
  # you can complete all tasks quite easily by querying against this parsed_data query expression.
  parsed_data AS (
  SELECT
    date,
    flatten(url_params) AS info
  FROM
    data_with_params),
  #------------------------------------------------------------
  # Here is a sample query that produces the number of unique users (purchasers) per bundle.
  # If you have not completed "flatten" UDF, this query's result may be incorrect.
id486 as (
select distinct date, os, uuid
from (
SELECT
  date,
  info.os,
  info.uuid
FROM
  parsed_data
where info.bundle = 'id486' and info.amount > 0.0
)),

id686 as (
select distinct date, os, uuid
from (
SELECT
  date,
  info.os,
  info.uuid
FROM
  parsed_data
where info.bundle = 'id686' and info.amount > 0.0
))

select * from (
select os, uuid from (
select os, uuid, MAX(c_days) c_days from (
select os, uuid, grp, count(1) c_days from (
select os, uuid, countif(step > 1) Over(partition by os, uuid order by date) grp from (
SELECT os, uuid, date,
  DATE_DIFF(date, LAG(date) OVER(PARTITION BY os, uuid ORDER BY date), DAY) step
from id486
)
) group by os, uuid, grp
) group by os, uuid
) where c_days >= 4 )

union all

select * from (
select os, uuid from (
select os, uuid, MAX(c_days) c_days from (
select os, uuid, grp, count(1) c_days from (
select os, uuid, countif(step > 1) Over(partition by os, uuid order by date) grp from (
SELECT os, uuid, date,
  DATE_DIFF(date, LAG(date) OVER(PARTITION BY os, uuid ORDER BY date), DAY) step
from id686
)
) group by os, uuid, grp
) group by os, uuid
) where c_days >= 4 )