# Search Functionality Documentation

## Overview

The SearchService provides comprehensive search capabilities for the JiraLite application.

## Features

-   **Basic text search** - Search issues and projects by keywords
-   **Advanced filtering** - Filter by status, assignee, and other criteria
-   **Autocomplete suggestions** - Smart suggestions while typing
-   **Permission-based results** - Only shows items user has access to

## Usage

### Basic Search

```java
SearchService searchService = new SearchService(issueRepo, projectRepo, authService);
List<Issue> results = searchService.searchIssues("bug fix");
```

### Advanced Search

```java
List<Issue> results = searchService.searchIssuesAdvanced(
    "performance",
    Status.IN_PROGRESS,
    assignedUser
);
```

### Autocomplete

```java
List<String> suggestions = searchService.getSearchSuggestions("per");
// Returns: ["performance", "permissions", "persistence"]
```

## Security

-   All search operations require user authentication
-   Results are filtered based on user permissions
-   Users can only see projects they are members of

## Future Enhancements

-   Full-text search with Elasticsearch
-   Search history and saved searches
-   Advanced query syntax support
