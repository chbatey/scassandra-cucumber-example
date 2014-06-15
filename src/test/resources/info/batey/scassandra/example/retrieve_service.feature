Feature: Retrieve User Information

  Scenario: User information is retrieved from the company wide repository
    Given The company wide user repository is available
    When User information is requested
    Then The user information is returned

  Scenario: Company wide information store is down
    Given The company wide user repository is unavailable
    When User information is requested
    Then Unable to retrieve user information is returned

  Scenario: Company wide information store is down but the information has been saved previously
    Given The user had been retrieved previously
    And The company wide user repository is unavailable
    When User information is requested
    Then The user information is returned

  Scenario: Company wide information store is up but data store down
    Given The data store is down
    And The company wide user repository is available
    When User information is requested
    Then The user information is returned

  Scenario: Data store being slow should not cause slow down user transactions
    Given The data store has problems writing
    And The company wide user repository is available
    When User information is requested
    Then The user information is returned

  Scenario: Company wide information store is down and the data store is down
    Given The company wide user repository is unavailable
    And The data store is down
    When User information is requested
    Then Unable to retrieve user information is returned