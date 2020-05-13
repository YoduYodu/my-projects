# DF-BQ example query.
SELECT
  year,
  name,
  cnt as total_newborns,
  rk as ranking
FROM (
  SELECT
    year,
    name,
    sum(number) as cnt,
    RANK() OVER (PARTITION BY year ORDER BY SUM(number) DESC) AS rk
  FROM
    `bigquery-public-data.usa_names.usa_1910_2013`
  GROUP BY
    1,
    2
)
WHERE
  rk <= 1