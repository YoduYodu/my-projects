#standardSQL
#chage table name to yours!

SELECT
  id,
  sha,
  SPLIT(ts_sc,'^')[
OFFSET
  (1)] AS score
FROM (
  SELECT
    id,
    sha,
    MAX( CONCAT(CAST(updated_at AS string), '^', score) )AS ts_sc
  FROM
    `beer-spear.L14_demo.proj1_test`
  GROUP BY
    1,
    2)
