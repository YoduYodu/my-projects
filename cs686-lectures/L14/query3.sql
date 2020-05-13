#standardSQL
#change table name to yours!
SELECT
  id,
  sha,
  ANY_VALUE(final_score) AS score
FROM (
  SELECT
    id,
    sha,
    score,
    updated_at,
    FIRST_VALUE(score) OVER (PARTITION BY id, sha ORDER BY updated_at DESC) AS final_score
  FROM
    `beer-spear.L14_demo.proj1_test` 
    )
GROUP BY
  1,
  2
