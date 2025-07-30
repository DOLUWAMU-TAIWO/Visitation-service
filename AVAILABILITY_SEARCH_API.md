# Availability Search API Documentation

This document explains how frontend developers can use the `/api/shortlets/availability/search` endpoint to search for available listings by date range, and what data is returned for each listing. This endpoint integrates with the Listing Service to provide rich property details.

---

## Endpoint

**GET** `/api/shortlets/availability/search`

### Query Parameters
- `startDate` (required, format: `YYYY-MM-DD`): The start date of the desired availability window.
- `endDate` (required, format: `YYYY-MM-DD`): The end date of the desired availability window.

**Example:**
```
GET http://localhost:8181/api/shortlets/availability/search?startDate=2025-09-01&endDate=2025-09-10
```

---

## How It Works
1. The backend finds all property IDs with availability in the given date range.
2. It calls the Listing Service (via GraphQL) to fetch full details for those property IDs.
3. It returns a structured response with a list of listings and a status message.

---

## Response Structure
The response is a JSON object with the following fields:

| Field     | Type    | Description                                      |
|-----------|---------|--------------------------------------------------|
| success   | boolean | Whether the search was successful                |
| message   | string  | Human-readable status message                    |
| data      | array   | List of available listings (see below)           |

### Example Success Response
```
{
  "success": true,
  "message": "Available listings fetched successfully.",
  "data": [
    {
      "id": "9c8b15ca-3d75-48d3-a1fc-38e6217cd81a",
      "title": "Modern 2BR Apartment",
      "description": "A beautiful apartment in Lekki.",
      "price": 150000,
      "status": "ACTIVE",
      "address": {
        "street": "Admiralty Way",
        "city": "Lagos",
        "state": "Lagos",
        "neighborhood": "Lekki Phase 1",
        "sublocality": "",
        "formattedAddress": "Lekki Phase 1, Lagos, Nigeria"
      },
      "propertyType": "APARTMENT",
      "bedrooms": 2,
      "bathrooms": 2,
      "furnishingStatus": "FURNISHED",
      "amenities": ["WiFi", "AC", "Pool"],
      "imageKeys": ["listing-9c8b15ca/img1.jpg", "listing-9c8b15ca/img2.jpg"]
    },
    // ...more listings
  ]
}
```

### Example No Results
```
{
  "success": true,
  "message": "No available properties found for the given date range.",
  "data": []
}
```

### Example Error
```
{
  "success": false,
  "message": "Error occurred while searching for available listings: <error message>",
  "data": []
}
```

---

## Listing Object Fields
Each listing in the `data` array contains:
- `id` (string): Listing UUID
- `title` (string): Listing title
- `description` (string): Description
- `price` (number): Price
- `status` (string): Listing status (e.g., ACTIVE)
- `address` (object):
    - `street`, `city`, `state`, `neighborhood`, `sublocality`, `formattedAddress` (strings)
- `propertyType` (string): Property type (e.g., APARTMENT)
- `bedrooms` (number): Number of bedrooms
- `bathrooms` (number): Number of bathrooms
- `furnishingStatus` (string): FURNISHED, SEMI_FURNISHED, UNFURNISHED
- `amenities` (array): List of amenities
- `imageKeys` (array): S3 image keys for property images

---

## Usage Tips
- Use the `imageKeys` to construct image URLs for display.
- Use the `address` fields to show formatted addresses.
- Use `amenities`, `propertyType`, `bedrooms`, `bathrooms`, and `furnishingStatus` for property details.
- Always check the `success` and `message` fields before using the `data` array.

---

## Frontend Example (JS/React)
```js
fetch('http://localhost:8181/api/shortlets/availability/search?startDate=2025-09-01&endDate=2025-09-10')
  .then(res => res.json())
  .then(data => {
    if (data.success) {
      // Render data.data as listing cards
    } else {
      // Show error message
    }
  });
```

---

## Contact
For questions or issues, contact the backend team.

