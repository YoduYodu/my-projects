# task 3B
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
      case "gps_adid":
        uuid = kv[1];
        os = "android";
        break;
       case "ios_idfa":
         uuid = kv[1];
         os = "ios";
         break;
       case "amount":
        amount = kv[1];
      default:
        break;
    }
  }
  return {bundle: bundle, os:os, uuid:uuid, amount: amount}
""";

with data_with_params AS (
  SELECT
    DATE(ts) AS date,
    REGEXP_EXTRACT_ALL(url,r'(?:\?|&)((?:[^=]+)=(?:[^&]*))') AS url_params
  FROM (
    SELECT
      urldecode(httpRequest.requestUrl) AS url,
      timestamp AS ts,
    FROM
      `lab07.large`
    WHERE
      TRUE
      )),
 parsed_data AS (
  SELECT
    date,
    url_params,
    flatten(url_params) AS info
  FROM
    data_with_params)

, Q1 as(
SELECT DISTINCT
  date,
  info.os as os,
  info.uuid as id,
  info.amount as amount
FROM
  parsed_data
WHERE info.bundle = "id486" AND info.amount > 0
)
, Q2 as(
Select
   date,
   os,
   id,
  count(*) over (partition by id order by  date) as number
from Q1
Group by
1,
2,
3)
, Q3 as(
Select
   os,
   id,
  count(*) as num_active_days
from Q2
Group by
1,
2
Having num_active_days >= 4
)
, Q4 as (
SELECT DISTINCT
  date,
  Q1.os,
  Q1.id
From Q1
Inner join Q3
on Q1.os = Q3.os AND Q1.id = Q3.id
Order by id, date
)
, Q5 as(
Select
  date,
  os,
  id,
  row_number() OVER (Partition by id order by date) as row_num
from Q4
)
, Q6 as(
Select
date,
  os,
  id,
  date_sub(date, INTERVAL row_num DAY) as val
from Q5
)
, Q7 as(
Select DISTINCT
  os,
  id,
  Count(*) over(partition by id, val) as num_days
from Q6
)
, Q8 as(
Select Distinct
  os,
  id
From Q7
Where num_days >=4
)
 , Q01 as(
SELECT DISTINCT
  date,
  info.os as os,
  info.uuid as id,
  info.amount as amount
FROM
  parsed_data
WHERE info.bundle = "id686"
)
, Q02 as(
Select
   date,
   os,
   id,
  count(*) over (partition by id order by  date) as number
from Q01
Group by
1,
2,
3)
, Q03 as(
Select
   os,
   id,
  count(*) as num_active_days
from Q02
Group by
1,
2
Having num_active_days >= 4
)
, Q04 as (
SELECT DISTINCT
  date,
  Q01.os,
  Q01.id
From Q01
Inner join Q03
on Q01.os = Q03.os AND Q01.id = Q03.id
Order by id, date
)
, Q05 as(
Select
  date,
  os,
  id,
  row_number() OVER (Partition by id order by date) as row_num
from Q04
)
, Q06 as(
Select
date,
  os,
  id,
  date_sub(date, INTERVAL row_num DAY) as val
from Q05
)
, Q07 as(
Select DISTINCT
  os,
  id,
  Count(*) over(partition by id, val) as num_days
from Q06
)
, Q08 as(
Select Distinct
  os,
  id
From Q07
Where num_days >=4)
 SELECT os, id
 from Q8
 UNION DISTINCT
 SELECT os, id
 from Q08


