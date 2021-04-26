Feature:  Lucky draw features
  As a person who believes in fate & fortune,
  I want to figure out if I am lucky, given a input number


  Scenario Outline: Lucky Draw API Query
    When the Lucky Draw API is queried with <number>
    Then the response status code is 200
    And the response shows <result>

    Examples: draws
      | number   | result                       |
      | 1        | Oh you are not so lucky      |
      | 2        | Geez you are extremely lucky |
      | 3        | Please try one more time     |
      | 4        | Please try one more time     |
