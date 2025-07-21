# Bulk Listing Retrieval Integration Guide (Spring Boot, Java)

## Overview
This guide explains how to use the `getListingsByIds` GraphQL query to efficiently fetch multiple listings from the Listing Service in a Spring Boot microservice (Java).

---

## Prerequisites
- Java 17+
- Spring Boot 3+
- GraphQL client library (e.g., [Spring GraphQL](https://docs.spring.io/spring-graphql/docs/current/reference/html/))
- Access to the Listing Service GraphQL endpoint (e.g., `http://localhost:6464/graphql`)

---

## Step 1: Configure GraphQL Client

Add a GraphQL client to your microservice. Example using [Spring GraphQL WebClient](https://docs.spring.io/spring-graphql/docs/current/reference/html/#webclient):

```java
WebClient webClient = WebClient.create("http://localhost:6464/graphql");
GraphQlClient graphQlClient = GraphQlClient.builder(webClient).build();
```flyway migratiopn

---

## Step 2: Construct the Bulk Query

Use the following GraphQL query to fetch multiple listings by their IDs:

```graphql
query getListingsByIds($ids: [ID!]!) {
  getListingsByIds(ids: $ids) {
    id
    title
    description
    price
    status
  }
}
```

---

## Step 3: Call the Query from Java

Example Java code to call the query:

```java
List<String> listingIds = List.of(
    "9c8b15ca-3d75-48d3-a1fc-38e6217cd81a",
    "1f53c272-9ec6-45ad-90d7-771a48684cee"
);

String query = """
    query getListingsByIds($ids: [ID!]!) {
      getListingsByIds(ids: $ids) {
        id
        title
        description
        price
        status
      }
    }
""";

Map<String, Object> variables = Map.of("ids", listingIds);

GraphQlResponse response = graphQlClient.document(query)
    .variables(variables)
    .retrieve()
    .toEntity(GraphQlResponse.class)
    .block();

List<ListingDto> listings = response.getData().get("getListingsByIds");
```

---

## Step 4: Handle the Response

Define a DTO for the listing data:

```java
public class ListingDto {
    private String id;
    private String title;
    private String description;
    private Double price;
    private String status;
    // getters and setters
}
```

Parse the response and use the listing data as needed in your microservice.

---

## Step 5: Error Handling & Testing
- Ensure you handle possible errors (e.g., invalid IDs, network issues).
- Test with various sets of IDs to verify correct results.

---

## Summary
- Use the `getListingsByIds` query for efficient bulk listing retrieval.
- Integrate with Spring Boot using a GraphQL client.
- Pass a list of IDs and handle the response in your service logic.

---

For more details, see the [Spring GraphQL documentation](https://docs.spring.io/spring-graphql/docs/current/reference/html/).
