# CS686 Lab 07

Up-to-date [README file](https://github.com/cs-rocks/cs686-lectures/blob/master/labs/Lab07-README.md)

 - (v0): Released around 9pm on Mar 3 (Tue).
 - (v1): Added a clarification for Task 1-B (around 5pm on Mar 4 Wed):
 
> **Clarification (1-B)**: In case For Task 1-B, you should NOT ignore purchases with `amount=0`, unlike in Task 3. 
> Also, as you *filter out* the rows with average amount less than 10,000, you will *keep* the rows with average amount `>= 10,000` (otherwise, you'll end up with 7 rows instead of 5).
> Please feel free to ask on Piazza if you find problem statements ambiguous!

 - (v2): (Mar 13 at 11:44am) See Piazza post [183](https://piazza.com/class/k5ad4g8m2jf6t6?cid=183) about case-sensitivity in `__shareable__2A()`. If your code was not passing "2A" test because your query returns os/uuid with mixed-case or upper-case, then it shall pass now. The grading system will now `lower` your results before comparing with the correct results. For local tests, you can fix the lines 120-122 of `__TestSample.Java` as follows:

> ```Java
> // For Task 2-A, grading system will round the values to 1 decimal place.
>  static __helperInterface formatter2A =
>      (FieldValueList fvl) -> String.format("%s\t%s\t%d\t%.1f", fvl.get(0).getStringValue().toLowerCase(),
>          fvl.get(1).getStringValue().toLowerCase(), fvl.get(2).getLongValue(), fvl.get(3).getDoubleValue());
> ```


# Task Context and Initial Setup #

There are three main tasks in this lab, which are logically equivalent to the three `PTransform`s in project 03. 
In this lab, you'll implement them in BigQuery instead.

Specifically, you'll use the same log files (text files containing newline delimited JSON logs) by first loading them to BigQuery (use the four text files in `java/dataflow/resources` from your project 03 repo). The file names indicate the size of data (tiny, small, medium, and large).

Create a BQ table by loading each of the four files mentioned; we'll call each table `tiny`, `small`, `medium`, and `large` in this README, but you can name your datasets/tables as whatever you like.

[SEE HERE](https://github.com/hadenlee/lab07-judge/blob/master/Screenshot%202020-03-01%2017.15.54.png)

**We saw how you can load these files into BQ tables in L17, so please review the lecture if you missed it.**


# BigQuery/SQL Tasks #

## Task 0 - Java Setup and Parsing in SQL ##
Make sure you've configured Cloud SDK and such according to Lab 06. Lab 07 will be graded in the same way as Lab 06.

After you load the data into BQ tables, each table will have six columns (`httpRequest`, `timestamp`, and four more columns that you will not need). `httpRequest` is a record type, and we need to parse `requestUrl` and extract URL parameters (parameter names and values; for instance, `bundle=id686486`). You can use the following template code that handles most of the work (but one UDF is only partially implemented); you also have an option to implement everything from scratch (which will help you practice more). Hence, use of the provided code is optional.

**Note that in class we went over the custom UDFs and the template query shown below, so if you missed the class, you should probably review L17 materials before attempting this lab.**

### About Data ###
The most important fields the String field that conatins httpUrl.
Using UDFs, you should be able to parse it, and use a more useful (structured) message (that is, `info.os`, `info.uuid`, `info.bundle`, and `info.amount`). For `timestamp` field, it's only needed in Task C (use `DATE(timestamp)` to convert it to date). 

Note that you will need to parse `ios_idfa` or `gps_adid` to obtain `uuid` (and from this you can infer the OS where `ios_idfa` implies `iOS` and `gps_adid` implies `Android`). Recall that `uuid` is case-insensitive. I believe that every JSON log contains either `ios_idfa` or `gps_adid` but not both (if you find that it's not true, please report the error on Piazza).


<details><summary>Template Query for Parsing (CLICK)</summary>
<p>
 
```
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
      // TODO - complete this.
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
      `beer-spear.Lab07.d02_small` # Change this to your table name.
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
SELECT
  info.bundle AS bundle,
  COUNT(DISTINCT CONCAT(info.os,':', info.uuid)) AS cnt_unique_users
FROM
  parsed_data
GROUP BY
  1
ORDER BY
  2 DESC
```

</p>
</details>

## Task 1 - Per-bundle Stats ##

### Task 1-A ###
Reading from the `small` data/table, compute for each bundle the number of unique purchasers and the total amount of purchases (do not round up/down) -- AND filter out bundles with fewer than 10 unique users (purchasers).

Your query should produce 4 rows (see the reference table later).


### Task 1-B ###
Reading from the `medium` data/table, compute the average amount of purchases (per person) for each bundle.
Let's say for some bundle `x`, the number of unique users is `5` and the total amount of purchases is `12345.05`; then the average amount of purhcases per person would be `12345.05/5 = 2469.01` for this bundle. Note that this is not necessarily the same as computing the average amount of purchases per bundle (why?).

Lastly, filter out any rows (bundles) whose per-person average amount is less than 10,000.0.

Your query should produce 5 rows (see the reference table later).

(Note that Your query for 1-B is likely to be very similar to that for 1-A.)


## Task 2 - High Spenders ##

### Task 2-A ###
Reading from the `medium` data/table, identify the four high spenders who made 4 or more purchases (and total amount of purchases over 1,000) for the `id686` bundle. (Ignore all other purchases). 
(Grading system will round the "total" values to 1 decimal place, but you don't have to round the values yourself; see the unit tests for how it's being done.)

Your query should produce 4 rows (see the reference table later).

### Task 2-B ###
For this task, let's assume that a user is a high spender if there is some bundle X where this user made 4 or more purhcases (in bundle X) AND spent 5,000 or more (in bundle X); there may exist multiple such bundles for some users.

Using the `large` data/table, identify the 13 high spenders. 

(If you are getting 22 high spenders instead of 13, you probably need to remove duplicates in your answer.
If you are getting 686 high spenders instead of 13, you should probably re-read the problem statement carefully.)

Your query should produce 13 rows (see the reference table later).


## Task 3 - Addicts ##

### Task 3-A ###
Using the `medium` data/table, identify the two addicts who made at least one (non-zero amount) purchase each day (in UTC) for 3 or more consecutive days for the bundle `id486686` (ignore all other purchases).

Here, you can use one of the analytic functions or you can use a UDF (as long as your query runs under 10 seconds, that's fine).

Your query should produce two rows (see the reference table later).

### Task 3-B ###
Using the `large` data/table, identify one addict who made at least one (non-zero amount) purchase each day (in UTC) for 4 or more consecutive days for the bundle `id486` (ignore all other purchases) and the other addict who made at least one (non-zero amount) purchase each day (in UTC) for 4 or more consecutive days for the bundle `id686` (ignore all other purchases).
In other words, repeat Task 3-A for `id486` and `id686` (independently) with the threshold being 4 or more consecutive days.

Here, you can use one of the analytic functions or you can use a UDF, or something completely different. 

Your query should produce two rows (see the reference table later).

## Reference Table ##
You can check the expected results for each task [HERE](https://docs.google.com/spreadsheets/d/1M-U671GvifUZO-7_09-_IP6w0PDpU4WA2o1_1VTv-Do/edit#gid=0).
As far as grading goes, your query results need not be sorted (row-wise), and your columns (of the final output) can be named in any way, but column-order must be consistent with the provided reference file. For instance, in Task 1-A, the first column must contain `bundle`s, second column the number of unique purchasers, and third column the total amount of purchases.
 
## How to submit queries ##
 - For each task, there is a blank file under `java/dataflow/resources` directory.
 - Copy and paste your working query (including all UDFs; ALL of it!) into the appropriate file.
 - You can do whatever you want to do in your query, but its execution time should NOT exceed 10 seconds (or it'll time out) and it should NOT process more than 2.5MB (which is sufficient even for the large table).
 - **Grading System** will string-replace `` `your-project.your-dataset.your-table` ``, so to ensure your queries are properly graded, you should NOT use backticks (grave accents, `` ` ``), just in case.

## Grading ##
 - As usual, failing on sample tests will penalize your overall score dramatically!
 - Hidden tests are worth 10% and shareable tests are worth 90%.
 - This **90%** is further divided as follows:
   - CS 486: Task A and B are worth 40% each, and Task C is worth 10% (total 90%).
   - CS 686: Each task is worth 30% (total 90%).
   - CS 4+1: Each task is worth 30% (total 90%).
 - In this lab, you are NOT limited to specific functions, so you are free to do whatever you want to, as long as your queries are efficient (your queries should run under 10 seconds, really).
 - In this lab, all shareable tests are shared in advance for your convenience.
 - Since this lab requires you to run queries on BigQuery (rather than writing Java code), it's expected that you use GCP credits to run your queries until you get the query that produces correct results.

# Status Dashboard #
https://www.cs.usfca.edu/~hlee84/cs686/lab07-status.html (this will become available once the grading system begins grading.)
