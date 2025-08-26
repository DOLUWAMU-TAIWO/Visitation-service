# Tenant Visit API Integration Guide

## Overview
This guide documents how to integrate with the tenant-facing visit management endpoints in the VisitController. These endpoints allow tenants to manage their property visit requests, view upcoming visits, check past visits, and more.

**Base URL**: `/api/visits`

## Authentication
All endpoints require the tenant to be authenticated. Include the tenant's authentication token in your requests.

## Data Models

### VisitResponseDto
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "propertyId": "550e8400-e29b-41d4-a716-446655440001",
  "visitorId": "550e8400-e29b-41d4-a716-446655440002",
  "landlordId": "550e8400-e29b-41d4-a716-446655440003",
  "scheduledAt": "2024-12-25T14:30:00Z",
  "durationMinutes": 60,
  "status": "PENDING",
  "rescheduledFromId": null,
  "createdAt": "2024-12-20T10:00:00Z",
  "updatedAt": "2024-12-20T10:00:00Z",
  "notes": "Looking for a 2-bedroom apartment",
  "slotId": "550e8400-e29b-41d4-a716-446655440004"
}
```

### Visit Status Enum
- `PENDING` - Visit request submitted, awaiting landlord approval
- `APPROVED` - Visit approved by landlord
- `REJECTED` - Visit rejected by landlord  
- `CANCELLED` - Visit cancelled by tenant or landlord
- `RESCHEDULED` - Visit has been rescheduled
- `COMPLETED` - Visit has been completed

### VisitCreateRequest
```json
{
  "propertyId": "550e8400-e29b-41d4-a716-446655440001",
  "landlordId": "550e8400-e29b-41d4-a716-446655440003",
  "visitorId": "550e8400-e29b-41d4-a716-446655440002",
  "scheduledAt": "2024-12-25T14:30:00Z",
  "notes": "Looking for a 2-bedroom apartment",
  "slotId": "550e8400-e29b-41d4-a716-446655440004"
}
```

## Tenant Visit Endpoints

### 1. Get All Tenant Visits
**Endpoint**: `GET /api/visits/tenant/{tenantId}`

**Description**: Retrieve all visits for a specific tenant with optional filtering and pagination.

**Parameters**:
- `tenantId` (path): UUID of the tenant
- `status` (query, optional): Filter by visit status (PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED, or "all")
- `page` (query, optional): Page number (default: 0)
- `size` (query, optional): Page size (default: 20)

**Example Request**:
```bash
GET /api/visits/tenant/550e8400-e29b-41d4-a716-446655440002?status=APPROVED&page=0&size=10
```

**Example Response**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "propertyId": "550e8400-e29b-41d4-a716-446655440001",
    "visitorId": "550e8400-e29b-41d4-a716-446655440002",
    "landlordId": "550e8400-e29b-41d4-a716-446655440003",
    "scheduledAt": "2024-12-25T14:30:00Z",
    "durationMinutes": 60,
    "status": "APPROVED",
    "createdAt": "2024-12-20T10:00:00Z",
    "updatedAt": "2024-12-20T10:00:00Z",
    "notes": "Looking for a 2-bedroom apartment"
  }
]
```

### 2. Get Upcoming Visits
**Endpoint**: `GET /api/visits/tenant/{tenantId}/upcoming`

**Description**: Get all future visits that are scheduled after the current time and have status APPROVED or PENDING.

**Parameters**:
- `tenantId` (path): UUID of the tenant
- `page` (query, optional): Page number (default: 0)
- `size` (query, optional): Page size (default: 20)

**Example Request**:
```bash
GET /api/visits/tenant/550e8400-e29b-41d4-a716-446655440002/upcoming
```

### 3. Get Past Visits
**Endpoint**: `GET /api/visits/tenant/{tenantId}/past`

**Description**: Get all completed, cancelled, or past visits (scheduled before current time).

**Parameters**:
- `tenantId` (path): UUID of the tenant
- `page` (query, optional): Page number (default: 0)
- `size` (query, optional): Page size (default: 20)

**Example Request**:
```bash
GET /api/visits/tenant/550e8400-e29b-41d4-a716-446655440002/past
```

### 4. Get Pending Visits
**Endpoint**: `GET /api/visits/tenant/{tenantId}/pending`

**Description**: Get all visits that are pending landlord approval.

**Parameters**:
- `tenantId` (path): UUID of the tenant
- `page` (query, optional): Page number (default: 0)
- `size` (query, optional): Page size (default: 20)

**Example Request**:
```bash
GET /api/visits/tenant/550e8400-e29b-41d4-a716-446655440002/pending
```

### 5. Get Visit Details
**Endpoint**: `GET /api/visits/tenant/{tenantId}/details/{visitId}`

**Description**: Get detailed information about a specific visit. Includes ownership verification.

**Parameters**:
- `tenantId` (path): UUID of the tenant
- `visitId` (path): UUID of the visit

**Example Request**:
```bash
GET /api/visits/tenant/550e8400-e29b-41d4-a716-446655440002/details/550e8400-e29b-41d4-a716-446655440000
```

**Error Response** (404 if visit not found or doesn't belong to tenant):
```json
{
  "error": "Visit not found"
}
```

## General Visit Management Endpoints

### 6. Request a New Visit
**Endpoint**: `POST /api/visits`

**Description**: Create a new visit request.

**Request Body**:
```json
{
  "propertyId": "550e8400-e29b-41d4-a716-446655440001",
  "landlordId": "550e8400-e29b-41d4-a716-446655440003",
  "visitorId": "550e8400-e29b-41d4-a716-446655440002",
  "scheduledAt": "2024-12-25T14:30:00Z",
  "notes": "Interested in viewing this property",
  "slotId": "550e8400-e29b-41d4-a716-446655440004"
}
```

### 7. Cancel a Visit
**Endpoint**: `PUT /api/visits/{visitId}/cancel`

**Description**: Cancel an existing visit.

**Example Request**:
```bash
PUT /api/visits/550e8400-e29b-41d4-a716-446655440000/cancel
```

## Frontend Implementation Examples

### React/JavaScript Example

```javascript
class TenantVisitService {
  constructor(baseUrl, authToken) {
    this.baseUrl = baseUrl;
    this.authToken = authToken;
  }

  async makeRequest(url, options = {}) {
    const response = await fetch(`${this.baseUrl}${url}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.authToken}`,
        ...options.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  // Get all visits with optional filtering
  async getAllVisits(tenantId, status = null, page = 0, size = 20) {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (status && status !== 'all') {
      params.append('status', status);
    }
    
    return this.makeRequest(`/api/visits/tenant/${tenantId}?${params}`);
  }

  // Get upcoming visits
  async getUpcomingVisits(tenantId, page = 0, size = 20) {
    const params = new URLSearchParams({ 
      page: page.toString(), 
      size: size.toString() 
    });
    
    return this.makeRequest(`/api/visits/tenant/${tenantId}/upcoming?${params}`);
  }

  // Get past visits
  async getPastVisits(tenantId, page = 0, size = 20) {
    const params = new URLSearchParams({ 
      page: page.toString(), 
      size: size.toString() 
    });
    
    return this.makeRequest(`/api/visits/tenant/${tenantId}/past?${params}`);
  }

  // Get pending visits
  async getPendingVisits(tenantId, page = 0, size = 20) {
    const params = new URLSearchParams({ 
      page: page.toString(), 
      size: size.toString() 
    });
    
    return this.makeRequest(`/api/visits/tenant/${tenantId}/pending?${params}`);
  }

  // Get visit details
  async getVisitDetails(tenantId, visitId) {
    return this.makeRequest(`/api/visits/tenant/${tenantId}/details/${visitId}`);
  }

  // Request a new visit
  async requestVisit(visitData) {
    return this.makeRequest('/api/visits', {
      method: 'POST',
      body: JSON.stringify(visitData),
    });
  }

  // Cancel a visit
  async cancelVisit(visitId) {
    return this.makeRequest(`/api/visits/${visitId}/cancel`, {
      method: 'PUT',
    });
  }
}

// Usage example
const visitService = new TenantVisitService('http://localhost:8080', 'your-auth-token');

// Get upcoming visits for a tenant
visitService.getUpcomingVisits('550e8400-e29b-41d4-a716-446655440002')
  .then(visits => {
    console.log('Upcoming visits:', visits);
  })
  .catch(error => {
    console.error('Error fetching visits:', error);
  });
```

### React Component Example

```jsx
import React, { useState, useEffect } from 'react';

const TenantVisitsDashboard = ({ tenantId, authToken }) => {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('upcoming');

  const visitService = new TenantVisitService('http://localhost:8080', authToken);

  useEffect(() => {
    loadVisits();
  }, [activeTab, tenantId]);

  const loadVisits = async () => {
    setLoading(true);
    try {
      let visitsData;
      switch (activeTab) {
        case 'upcoming':
          visitsData = await visitService.getUpcomingVisits(tenantId);
          break;
        case 'past':
          visitsData = await visitService.getPastVisits(tenantId);
          break;
        case 'pending':
          visitsData = await visitService.getPendingVisits(tenantId);
          break;
        default:
          visitsData = await visitService.getAllVisits(tenantId);
      }
      setVisits(visitsData);
    } catch (error) {
      console.error('Error loading visits:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelVisit = async (visitId) => {
    try {
      await visitService.cancelVisit(visitId);
      loadVisits(); // Reload visits
    } catch (error) {
      console.error('Error cancelling visit:', error);
    }
  };

  return (
    <div className="tenant-visits-dashboard">
      <div className="tabs">
        <button 
          className={activeTab === 'upcoming' ? 'active' : ''}
          onClick={() => setActiveTab('upcoming')}
        >
          Upcoming Visits
        </button>
        <button 
          className={activeTab === 'pending' ? 'active' : ''}
          onClick={() => setActiveTab('pending')}
        >
          Pending Approval
        </button>
        <button 
          className={activeTab === 'past' ? 'active' : ''}
          onClick={() => setActiveTab('past')}
        >
          Past Visits
        </button>
      </div>

      {loading ? (
        <div>Loading visits...</div>
      ) : (
        <div className="visits-list">
          {visits.length === 0 ? (
            <div>No visits found</div>
          ) : (
            visits.map(visit => (
              <div key={visit.id} className="visit-card">
                <h3>Property: {visit.propertyId}</h3>
                <p>Scheduled: {new Date(visit.scheduledAt).toLocaleString()}</p>
                <p>Status: {visit.status}</p>
                <p>Duration: {visit.durationMinutes} minutes</p>
                {visit.notes && <p>Notes: {visit.notes}</p>}
                
                {visit.status === 'APPROVED' && activeTab === 'upcoming' && (
                  <button 
                    onClick={() => handleCancelVisit(visit.id)}
                    className="cancel-button"
                  >
                    Cancel Visit
                  </button>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default TenantVisitsDashboard;
```

## Error Handling

### Common HTTP Status Codes
- `200 OK` - Request successful
- `404 Not Found` - Visit not found or doesn't belong to tenant
- `400 Bad Request` - Invalid parameters or request body
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "error": "Error description",
  "message": "Detailed error message",
  "timestamp": "2024-12-20T10:00:00Z"
}
```

## Pagination

All list endpoints support pagination:
- `page`: Zero-based page number (default: 0)
- `size`: Number of items per page (default: 20)

Example: `/api/visits/tenant/{tenantId}?page=1&size=10`

## Date Handling

- All dates are in ISO 8601 format with timezone: `2024-12-25T14:30:00Z`
- Use `OffsetDateTime` or equivalent in your frontend framework
- Consider user timezone for display purposes

## Best Practices

1. **Caching**: Cache visit data appropriately, especially for past visits
2. **Real-time Updates**: Consider WebSocket connections for real-time visit status updates
3. **Error Handling**: Implement robust error handling for network failures
4. **Loading States**: Show loading indicators during API calls
5. **Pagination**: Implement infinite scroll or pagination controls for large datasets
6. **Optimistic Updates**: Update UI immediately for user actions, with rollback on failure

## Integration Checklist

- [ ] Set up authentication headers
- [ ] Implement all tenant visit endpoints
- [ ] Add error handling and loading states
- [ ] Test pagination functionality
- [ ] Implement visit creation flow
- [ ] Add visit cancellation functionality
- [ ] Test with different visit statuses
- [ ] Verify date/timezone handling
- [ ] Add proper TypeScript interfaces (if using TypeScript)
- [ ] Test edge cases (empty lists, network failures)

## Services Used

The tenant visit endpoints use the following backend services:
- **VisitService**: Main service for visit operations (`visitService.getVisitsByVisitor()`, `visitService.getVisit()`)
- **No payment services**: These endpoints are purely for visit management, no payment logic involved

This completes the integration guide for the tenant visit management functionality.
