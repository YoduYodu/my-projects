# CS686 Lab 06

Up-to-date [README file](https://github.com/cs-rocks/cs686-lectures/blob/master/labs/Lab06-README.md)

 - (v0): Lab 06 became available on Canvas around 3:15pm, Feb 25th (Tuesday). 
 - (v1): The wording about not using `Partition BY` was a bit vague, so it's been updated (see the "diff" of the latest commit). Specifically, for the "join" tasks, do NOT use any analytic functions listed [HERE](https://cloud.google.com/bigquery/docs/reference/standard-sql/analytic-function-concepts). For instance, your query for "join" subtask should NOT use `RANK` or `ROW_NUMBER`; you can use those for the `partition` subtask (yet, I don't recommend using `ROW_NUMBER` because it's not efficient).
 - Please Report any issues/errors on Piazza.

# Java Task #
**This will not be graded, BUT you must complete this for future labs/projects.**

**This is required for everyone.**

If you run `main()` method of `Main` class in `java/dataflow`, you're likely to get error messages.
Resolve all of them (Google / StackOverflow / Piazza are your best friends).
Here are some high-level instructions:
 - First, check if `System.out.println(query); // This should print a few lines, a SQL query.` this actually prints out the query as intended. If not, something is wrong in terms of local file path.
 -  `// TODO: Change the project id below to YOUR GCP project id.` Check this line in `BqUtils.java` and change it to your GCP project name. `beer-spear` is [MINE](https://thumbs.worthpoint.com/zoom/images1/1/0717/16/disneyland-finding-nemo-hoodie_1_2c0d9cfab1ba050d1f79359e798d00ca.jpg "Mine")! 
 - `TableResult result = BqUtils.getQueryResult(query);` This line will likely to throw an exception if your `default GCP credential` is not configured on your local machine. Follow this [documentation](https://cloud.google.com/sdk/gcloud/reference/auth/application-default/login), and resolve the issue: 
 - If your java program correctly prints `num_rows = 5552452, col_str = dummy, col_boolean = true, col_int = ?` to the console, then you're all set. It means your local environment is now able to access GCP using your GCP account, which we'll use in future labs/projects. 
 - For this part only, you can publicly share your code and issues that you run into.


# BigQuery/SQL Tasks #
We'll use the public dataset here: `bigquery-public-data.usa_names.usa_1910_2013`

[reference file](https://docs.google.com/spreadsheets/d/1OYTBf7ebsGMdjvMcec5rtkgdvv9WwCRSiJpUUNe3IyU/edit#gid=0) for BQ tasks

For each of the following tasks, do it once using `join` (but no other analytic functions listed [HERE](https://cloud.google.com/bigquery/docs/reference/standard-sql/analytic-function-concepts)) and once using `partition by`.

Of course, you can try one more time using neither (but that won't be graded).


## Task A ##
Write a SQL query, which produces 104 rows that contain three columns:
 - `year`: 4-digit year
 - `name`: The most popular name for newborns during the year (based on the sum of `number`s)
 - `total_newborns`: The number of newborns given the name.
 - Your results should be identical to [this](https://docs.google.com/spreadsheets/d/1OYTBf7ebsGMdjvMcec5rtkgdvv9WwCRSiJpUUNe3IyU/edit#gid=0)
 - **NOTE** As you can see from the shared unit tests, grading system will not care what your column names are or how you order the rows. As such, don't worry about those, but rather make sure they produce the same results. The only thing that matters is the order of your columns. You're expected to figure that out by reading the provided code of unit tests.

## Task B ##
Write a SQL query, which produces 208 rows that contain four columns:
 - `year`: 4-digit year
 - `gender`: either `M` or `F`
 - `name`: The most popular name for newborns of the gender during the year (based on the sum of `number`s)
 - `total_newborns`: The number of newborns given the name.
 - Your results should be identical to [this](https://docs.google.com/spreadsheets/d/1OYTBf7ebsGMdjvMcec5rtkgdvv9WwCRSiJpUUNe3IyU/edit#gid=800936309)
 - **NOTE** As you can see from the shared unit tests, grading system will not care what your column names are or how you order the rows. As such, don't worry about those, but rather make sure they produce the same results. The only thing that matters is the order of your columns. You're expected to figure that out by reading the provided code of unit tests.


## Task C ##
**This is optional for CS486 students, but required for CS686 & 4+1 students.**
Write a SQL query, which produces 312 rows that contain four columns:
 - `year`: 4-digit year
 - `name`: The **three** most popular name for newborns during the year (based on the sum of `number`s), regardless of gender.
 - `total_newborns`: The number of newborns given the name.
 - `ranking`: Either 1, 2, or 3 (indicating most popular, second most popular, or third most popular within that year).
 - Your results should be identical to [this](https://docs.google.com/spreadsheets/d/1OYTBf7ebsGMdjvMcec5rtkgdvv9WwCRSiJpUUNe3IyU/edit#gid=1761399196)
  - **NOTE** As you can see from the shared unit tests, grading system will not care what your column names are or how you order the rows. As such, don't worry about those, but rather make sure they produce the same results. The only thing that matters is the order of your columns. You're expected to figure that out by reading the provided code of unit tests.

 
## How to submit queries ##
 - For each task, there are two blank files under `java/dataflow/resources` directory.
 - If your query uses `join`, copy and paste the query in the file whose name contains `join` (again, no analytic functions such as `RANK`, `ROW_NUMBER`, etc.)
 - If your query uses `partition by`, copy and paste the query in the file whose name contains `partition`.
 - **Your query should NOT use both, but only one**. This will be manually checked after the deadline.
 - If you have a query that uses *neither*, feel free to share it publicly on Piazza **after the deadline**.

## Grading ##
 - As usual, failing on sample tests will penalize your overall score dramatically!
 - One hidden test is worth 10% and six shareable tests are worth 90%.
 - This **90%** is further divided as follows:
   - CS 486: Task A is worth 30% and task B is worth 60% (total 90%).
   - CS 686: Task A is worth 15%, task B 30%, and task C 45% (total 90%).
   - CS 4+1: Task A is worth 15%, task B 30%, and task C 45% (total 90%).
 - This is about "correctness" of your query; if your query violates the conditions stated above, then you will receive 0 points for that subtask.
 - In this lab, all shareable tests are shared in advance for your convenience.
 - Since this lab requires you to run queries on BigQuery (rather than writing Java code), it's expected that you use GCP credits to run your queries until you get the query that produces correct results.
 - The three sample queries (from Lecture 14) can be slightly modified to solve all tasks in this lab (aka, you really should get 100% for this lab).

# Status Dashboard #
https://www.cs.usfca.edu/~hlee84/cs686/lab06-status.html (this will become available once the grading system begins grading.)



