# -------------------- Using Repeated Joins --------------------
# NOTE: This query was submitted by one of the students.
# Slot Time(ms): 349K-372K

# Read the instructions and write your query in standardSQL here.
# You should test your query on BigQuery, and compare the query results against the provided references.
# Then, paste your query here.
WITH yearly_total AS
  (SELECT YEAR,
          name,
          SUM(number) AS total
   FROM `bigquery-public-data.usa_names.usa_1910_2013`
   GROUP BY 1,
            2),

 Q1 AS
  (SELECT YEAR,
          MAX(total) AS pop1
   FROM yearly_total
   GROUP BY 1),

 Q1_name AS
  (SELECT t1.year,
          t2.name
   FROM
     (SELECT YEAR,
             pop1
      FROM Q1) AS t1
   LEFT JOIN
     (SELECT YEAR,
             name,
             total
      FROM yearly_total) AS t2 ON t1.year = t2.year
   AND t1.pop1 = t2.total),
 yearly_total_left_q1 AS
  (SELECT t1,
          t2
   FROM
     (SELECT YEAR,
             name,
             total
      FROM yearly_total) AS t1
   LEFT JOIN
     (SELECT YEAR,
             name
      FROM q1_name) AS t2 ON t1.year = t2.year
   AND t1.name = t2.name),

 Q2 AS
  (SELECT t1.year AS YEAR,
          MAX(t1.total) AS pop2
   FROM yearly_total_left_q1
   WHERE t2.year IS NULL
   GROUP BY 1),

 Q2_name AS
  (SELECT t1.year,
          t2.name
   FROM
     (SELECT YEAR,
             pop2
      FROM Q2) AS t1
   LEFT JOIN
     (SELECT YEAR,
             name,
             total
      FROM yearly_total) AS t2 ON t1.year = t2.year
   AND t1.pop2 = t2.total),

 yearly_total_left_q2 AS
  (SELECT p1,
          p2
   FROM
     (SELECT t1.year,
             t1.name,
             t1.total
      FROM yearly_total_left_q1
      WHERE t2.year IS NULL) AS p1
   LEFT JOIN
     (SELECT YEAR,
             name
      FROM Q2_name) AS p2 ON p1.year = p2.year
   AND p1.name = p2.name), # get third largest number per year.
 Q3 AS
  (SELECT p1.year AS YEAR,
          MAX(p1.total) AS pop3
   FROM yearly_total_left_q2
   WHERE p2.year IS NULL
   GROUP BY 1),

Q3_name AS
  (SELECT t1.year,
          t2.name
   FROM
     (SELECT YEAR,
             pop3
      FROM Q3) AS t1
   LEFT JOIN
     (SELECT YEAR,
             name,
             total
      FROM yearly_total) AS t2 ON t1.year = t2.year
   AND t1.pop3 = t2.total), # With all these, one option is to do this for Q2, Q2_name & Q3, Q3_name.
 # This approach is tedious, but you can shorten the queries above, actually.
 #------------------------------------------------------------------------------------ # here's a similar, but shorter alternative (beginning with yearly_total query expression from above).
 # QQ1 and QQ2 give you year and the number of newborns with the most/second-most popular name.
 # (You can do something similar to obtain QQ3).
 QQ1 AS
  (SELECT YEAR,
          MAX(total) AS pop1
   FROM yearly_total
   GROUP BY 1),
 QQ2 AS
  (SELECT YEAR,
          MAX(total) AS pop2
   FROM
     (SELECT YEAR,
             total
      FROM yearly_total
      WHERE YEAR NOT IN
          (SELECT YEAR
           FROM QQ1)
        OR total NOT IN
          (SELECT pop1
           FROM QQ1))
   GROUP BY 1),
 QQ3 AS
  (SELECT YEAR,
          MAX(total) AS pop3
   FROM
     (SELECT YEAR,
             total
      FROM yearly_total
      WHERE YEAR NOT IN
          (SELECT YEAR
           FROM QQ1)
        AND YEAR NOT IN
          (SELECT YEAR
           FROM QQ2)
        OR total NOT IN
          (SELECT pop1
           FROM QQ1)
        AND total NOT IN
          (SELECT pop2
           FROM QQ2) )
   GROUP BY 1),
 NAMES AS
  (SELECT t1.*,
          t2.ranking
   FROM
     (SELECT YEAR,
             name,
             total
      FROM yearly_total) AS t1
   JOIN
     (SELECT YEAR,
             pop1 AS COUNT,
             1 AS ranking
      FROM QQ1
      UNION ALL SELECT YEAR,
                       pop2 AS COUNT,
                       2 AS ranking
      FROM QQ2
      UNION ALL SELECT YEAR,
                       pop3 AS COUNT,
                       3 AS ranking
      FROM QQ3) AS t2 ON t1.year = t2.year
   AND t1.total = t2.count)
SELECT *
FROM NAMES
ORDER BY 1,
         4


# -------------------- Using ARRAY_AGG --------------------
# NOTE: This query was submitted by one of the students.
# Slot Time(ms): 16.8K - 29.1K 

# Read the instructions and write your query in standardSQL here.
# You should test your query on BigQuery, and compare the query results against the provided references.
# Then, paste your query here.
WITH
  sum_name AS (
  SELECT
    year,
    name,
    SUM(number) AS num_sum
  FROM
    `bigquery-public-data.usa_names.usa_1910_2013`
  GROUP BY
    1, 2
   ),

  most_name AS (
  SELECT year, num_sum, offset + 1 as rank FROM (
    SELECT year, ARRAY_AGG(num_sum ORDER BY num_sum DESC LIMIT 3) arr
    FROM sum_name GROUP BY year
  ), UNNEST(arr) num_sum WITH OFFSET AS offset)

SELECT
  t1.year AS year,
  t2.name AS name,
  t2.num_sum AS total,
  t1.rank AS ranking
FROM (
  SELECT
    *
  FROM
    most_name) AS t1
LEFT JOIN (
  SELECT
    year,
    name,
    num_sum,
  FROM
    sum_name ) AS t2
ON
  t1.year = t2.year
  AND t1.num_sum = t2.num_sum
  
# -------------------- Using EXCEPT DISTINCT --------------------
# NOTE: This query was submitted by one of the students.
# Slot Time(ms): 72.5K - 80.5K

# Read the instructions and write your query in standardSQL here.
# You should test your query on BigQuery, and compare the query results against the provided references.
# Then, paste your query here.

WITH year_name_sum_t AS (
  SELECT year, name, SUM(number) AS sum
  FROM `bigquery-public-data.usa_names.usa_1910_2013`
  GROUP BY year, name
)

# Rank 1, 2, and 3 using EXCEPT with name
SELECT t1.year, t2.name, t1.max AS total_newborns, t1.ranking
FROM
(
  SELECT year, max, ranking
  FROM
  ( # union set1 start
    SELECT year, MAX(sum) AS max, 1 AS ranking # rank1_t, start
    FROM year_name_sum_t
    GROUP BY year # rank1_t, end
  ) # union set1 end
  UNION ALL
  ( # union set2 start
    SELECT year, MAX(sum) AS max, 2 AS ranking # rank2_t, start
    FROM
    (
      SELECT year, sum
      FROM year_name_sum_t
      EXCEPT DISTINCT # **EXCEPT**
      SELECT year, max
      FROM
      (
        SELECT year, MAX(sum) AS max # rank1_t, start
        FROM year_name_sum_t
        GROUP BY year # rank1_t, end
      )
    )
    GROUP BY year # rank2_t, end
  ) # union set2 end
  UNION ALL
  ( # union set3 start
    SELECT year, MAX(sum) AS max, 3 AS ranking # rank3_t, start
    FROM
    (
      SELECT year, sum
      FROM year_name_sum_t
      EXCEPT DISTINCT # **EXCEPT**
      SELECT year, max
      FROM
      (
        (# union set3-1 start
          SELECT year, MAX(sum) AS max # rank2_t, start
          FROM
          (
            SELECT year, sum
            FROM year_name_sum_t
            EXCEPT DISTINCT # **EXCEPT**
            SELECT year, max
            FROM
            (
              SELECT year, MAX(sum) AS max # rank1_t, start
              FROM year_name_sum_t
              GROUP BY year # rank1_t, end
            )
          )
          GROUP BY year # rank2_t, end
        ) # union set3-1 end
        UNION ALL
        ( # union set3-2 start
          SELECT year, MAX(sum) AS max # rank1_t, start
          FROM year_name_sum_t
          GROUP BY year # rank1_t, end
        ) # union set3-2 end
      )
    )
    GROUP BY year # rank3_t, end
  ) # union set3 end
) AS t1
JOIN year_name_sum_t AS t2
ON t1.year = t2.year AND t1.max = t2.sum
ORDER BY t1.year, t1.ranking


# -------------------- Using JS UDF --------------------
# NOTE: This query intentionally has a bug in JS UDF which you should fix (as an exercise).
# Slot Time(ms): 12.7K-22.2K (this range is based on the "fixed, correct" query.)
CREATE TEMP FUNCTION
  find_top3(params ARRAY<STRUCT<name string,
    total int64>>)
  RETURNS ARRAY<STRUCT<name string,
  total int64,
  ranking int64>>
  LANGUAGE js AS """
  var idx = [-1, -1, -1];
  var output = [];
  for(var j = 0; j < 3; j++) { // we want to find the "max" 3 times.
    var max_val = 0, max_idx = -1;
    for(var i = 0; i < params.length; i++) {
      if(i == idx[0] || i == idx[1] || i == idx[2]) continue;
      if(params[i].total > max_val) {
        max_val = params[i].total;
        max_idx = i;
      }
    }
    output.push({name: params[max_idx].name, total: params[max_idx].total, ranking: (j+1)});
    idx[j] = max_idx;
  }
  return output;
""";
WITH
  yearly_total AS (
  SELECT
    year,
    name,
    SUM(number) AS total
  FROM
    `bigquery-public-data.usa_names.usa_1910_2013`
  GROUP BY
    1,
    2 )
  # ---------------------
SELECT
  year,
  x.name,
  x.total,
  x.ranking
FROM (
  SELECT
    year,
    find_top3(ARRAY_AGG(STRUCT(name AS name,
          total AS total))) AS output
  FROM
    yearly_total
  GROUP BY
    year),
  UNNEST(output) AS x
ORDER BY
  1,
  4
