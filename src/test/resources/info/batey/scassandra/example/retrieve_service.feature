Feature: Retrieve User Information

  Scenario: User information is retrieved from the company wide repository
    Given The company wide user repository is available
    When User information is requested
    Then The user information is returned

  Scenario: Company wide information store is down
    Given The company wide user repository is unavailable
    When User information is requested
    Then Unable to retrieve user information us returned

  Scenario: Company wide information store is down but the information has been saved previously
    Given The user had been retrieved previously
    And The company wide user repository is unavailable
    When User information is requested
    Then The user information is returned