#standardSQL
# change the table name to yours!
WITH
  last_ts AS (
  SELECT
    id,
    sha,
    MAX(updated_at) AS latest
  FROM
    `beer-spear.L14_demo.proj1_test`
  GROUP BY
    1,
    2)
  #-
SELECT
  t1.id AS id,
  t1.sha AS sha,
  t2.score AS score
FROM (
  SELECT
    *
  FROM
    last_ts) AS t1
LEFT JOIN (
  SELECT
    id,
    sha,
    updated_at,
    score
  FROM
    `beer-spear.L14_demo.proj1_test` ) AS t2
ON
  t1.id = t2.id
  AND t1.sha = t2.sha
  AND t1.latest = t2.updated_at

