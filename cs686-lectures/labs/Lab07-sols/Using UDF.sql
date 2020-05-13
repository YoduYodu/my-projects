# task 3B

CREATE TEMP FUNCTION queryParams(url string)
   RETURNS STRUCT<bundle string,os string,uuid string, amount float64>
  LANGUAGE js AS """
  let bundle = ""
  let os = ""
  let uuid = ""
  let amount = 0.1;

  let queryStr = decodeURIComponent(url).split("?")[1];
  let paramsList = queryStr.split('&');

  for (let params of paramsList) {
    kv = params.split("=");
    switch(kv[0]) {
        case "bundle":
          bundle = kv[1];
        break;
        case "ios_idfa":
          uuid = kv[1].toLowerCase();
          os = "ios";
        break;
        case "gps_adid":
          uuid = kv[1].toLowerCase();
          os = "android";
        break;
        case "amount":
          amount = kv[1];
        break;
        default:
        break;
   }
}
    return {bundle, os, uuid, amount}
""";
CREATE TEMP FUNCTION checkConseqDays(dates ARRAY<STRING>)
   RETURNS BOOL
  LANGUAGE js AS """

  let counter = 0;
  let result = false;

  for(let i = 0; i < dates.length-1; i++){
    if(Date.parse(dates[i+1]) - Date.parse(dates[i]) === 86400000){
        counter++;
        if(counter>2)
          return true;
      }
      else{
        counter=0;
      }
  }
    return false;
""";
WITH canonical as (
  SELECT
      queryParams(httpRequest.requestUrl) AS info, DATE(timestamp) as dt,
      FROM `Lab07.d04_large`
  )
  , dates_combined as (
  SELECT
    info.bundle as bundle,
    CONCAT(info.os,':', info.uuid) as device_id, dt, sum(info.amount) as amt
    from canonical
    where info.bundle ='id486' OR info.bundle ='id686'
    GROUP BY 1,2,3
    order by 1, 2, 3
)
,  dates_combined_sorted as(
  SELECT bundle
  ,device_id,
  CAST(dt AS STRING) as date_str
  from dates_combined
  )
,result_rows as (
     SELECT bundle,
     device_id,
    checkConseqDays(array_agg(date_str)) as result_bool
    from dates_combined_sorted
  group by 1,2
  )
  SELECT split(device_id,':')[OFFSET(0)] as os,
      split(device_id,':')[OFFSET(1)] as uuid
  FROM
      result_rows
      where result_bool = true;

