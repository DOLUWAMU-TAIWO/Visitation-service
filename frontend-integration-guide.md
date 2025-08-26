Cancel a visit (available for both tenants and landlords).

**REST Endpoint:**
```http
PUT /api/visits/{visitId}/cancel
```

**Frontend Usage:**
```javascript
const cancelVisit = async (visitId) => {
  try {
    const response = await fetch(`/api/visits/${visitId}/cancel`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error cancelling visit:', error);
    throw error;
  }
};

// Usage
await cancelVisit('visit-uuid-here');
```

### **GraphQL: Cancel Visit**
Cancel a visit using GraphQL.

**GraphQL Mutation:**
```graphql
mutation CancelVisit($id: ID!) {
  cancelVisit(id: $id) {
    id
    status
    updatedAt
  }
}
```

**Frontend Usage:**
```javascript
const cancelVisitGraphQL = async (visitId) => {
  const mutation = `
    mutation CancelVisit($id: ID!) {
      cancelVisit(id: $id) {
        id
        status
        updatedAt
      }
    }
  `;
  
  try {
    const response = await fetch('/graphql', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query: mutation,
        variables: { id: visitId }
      })
    });
    
    const result = await response.json();
    if (result.errors) {
      throw new Error(result.errors[0].message);
    }
    
    return result.data.cancelVisit;
  } catch (error) {
    console.error('Error cancelling visit:', error);
    throw error;
  }
};
```

---

## 📊 4. DASHBOARD ANALYTICS ENDPOINTS

### **REST: Tenant Dashboard Stats**
Get comprehensive analytics for a tenant's dashboard.

**REST Endpoint:**
```http
GET /api/dashboard/tenant/{tenantId}/stats
```

**Frontend Usage:**
```javascript
const fetchTenantDashboardStats = async (tenantId) => {
  try {
    const response = await fetch(`/api/dashboard/tenant/${tenantId}/stats`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching tenant dashboard stats:', error);
    throw error;
  }
};
```

**Response Format:**
```json
{
  "totalVisits": 5,
  "pendingVisits": 2,
  "upcomingVisits": 1,
  "totalBookings": 3,
  "activeBookings": 1,
  "favoriteProperties": 0,
  "totalSpent": 125000.0,
  "nextPaymentDue": "2025-08-30",
  "nextVisitDate": "2025-08-27",
  "visitStats": {
    "approved": 3,
    "rejected": 1,
    "completed": 1,
    "cancelled": 0
  },
  "bookingStats": {
    "confirmed": 2,
    "pending": 1,
    "completed": 0,
    "cancelled": 0
  }
}
```

### **GraphQL: Tenant Dashboard Stats**
Get tenant dashboard stats using GraphQL.

**GraphQL Query:**
```graphql
query GetTenantDashboardStats($tenantId: ID!) {
  tenantDashboardStats(tenantId: $tenantId) {
    totalVisits
    pendingVisits
    upcomingVisits
    totalBookings
    activeBookings
    favoriteProperties
    totalSpent
    nextPaymentDue
    nextVisitDate
    visitStats {
      approved
      rejected
      completed
      cancelled
    }
    bookingStats {
      confirmed
      pending
      completed
      cancelled
    }
  }
}
```

**Frontend Usage:**
```javascript
const fetchTenantDashboardStatsGraphQL = async (tenantId) => {
  const query = `
    query GetTenantDashboardStats($tenantId: ID!) {
      tenantDashboardStats(tenantId: $tenantId) {
        totalVisits
        pendingVisits
        upcomingVisits
        totalBookings
        activeBookings
        totalSpent
        nextPaymentDue
        nextVisitDate
        visitStats {
          approved
          rejected
          completed
          cancelled
        }
        bookingStats {
          confirmed
          pending
          completed
          cancelled
        }
      }
    }
  `;
  
  try {
    const response = await fetch('/graphql', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query,
        variables: { tenantId }
      })
    });
    
    const result = await response.json();
    if (result.errors) {
      throw new Error(result.errors[0].message);
    }
    
    return result.data.tenantDashboardStats;
  } catch (error) {
    console.error('Error fetching tenant dashboard stats:', error);
    throw error;
  }
};
```

### **REST: Landlord Dashboard Stats**
Get comprehensive analytics for a landlord's dashboard.

**REST Endpoint:**
```http
GET /api/dashboard/landlord/{landlordId}/stats
```

**Frontend Usage:**
```javascript
const fetchLandlordDashboardStats = async (landlordId) => {
  try {
    const response = await fetch(`/api/dashboard/landlord/${landlordId}/stats`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching landlord dashboard stats:', error);
    throw error;
  }
};
```

### **GraphQL: Landlord Dashboard Stats**

**GraphQL Query:**
```graphql
query GetLandlordDashboardStats($landlordId: ID!) {
  landlordDashboardStats(landlordId: $landlordId) {
    totalVisits
    pendingVisits
    upcomingVisits
    totalBookings
    activeBookings
    totalSpent
    nextPaymentDue
    nextVisitDate
    visitStats {
      approved
      rejected
      completed
      cancelled
    }
    bookingStats {
      confirmed
      pending
      completed
      cancelled
    }
  }
}
```

---

## 🛠 5. GRAPHQL SCHEMA UPDATES

### **New Types Added**
```graphql
type DashboardStats {
  totalVisits: Int
  pendingVisits: Int
  upcomingVisits: Int
  totalBookings: Int
  activeBookings: Int
  favoriteProperties: Int
  totalSpent: Float
  nextPaymentDue: String
  nextVisitDate: String
  visitStats: VisitStatsBreakdown
  bookingStats: BookingStatsBreakdown
}

type VisitStatsBreakdown {
  approved: Int
  rejected: Int
  completed: Int
  cancelled: Int
}

type BookingStatsBreakdown {
  confirmed: Int
  pending: Int
  completed: Int
  cancelled: Int
}
```

### **Enhanced Visit Type**
The Visit type now includes all fields from your Java model:
```graphql
type Visit {
  id: ID!
  propertyId: ID!
  visitorId: ID!
  landlordId: ID!
  scheduledAt: String        # ✅ Added - matches Java OffsetDateTime
  status: VisitStatus!
  durationMinutes: Int       # ✅ Added
  notes: String             # ✅ Added
  slotId: ID                # ✅ Added
  rescheduledFromId: ID     # ✅ Added
  createdAt: String         # ✅ Added
  updatedAt: String         # ✅ Added
  scheduledDate: String     # ✅ Kept for backward compatibility
}
```

### **Enhanced Booking Type**
The Booking type now includes all fields from your Java model:
```graphql
type Booking {
  id: ID!
  tenantId: ID!
  landlordId: ID!
  propertyId: ID!
  tenant: User
  landlord: User
  property: Property
  startDate: String!
  endDate: String!
  status: BookingStatus!
  nights: Int!
  firstName: String!         # ✅ Added
  lastName: String!          # ✅ Added
  phoneNumber: String!       # ✅ Added
  paymentStatus: PaymentStatus # ✅ Added
  paymentReference: String   # ✅ Added
  paymentAmount: Float       # ✅ Added
  reminder24hSent: Boolean!  # ✅ Added
  reminder1hSent: Boolean!   # ✅ Added
}
```

### **Fixed Enums**
```graphql
enum BookingStatus {
  PENDING
  ACCEPTED
  REJECTED
  CANCELLED
  RESCHEDULED
  NO_SHOW      # ✅ Added - was missing
}

enum VisitStatus {
  PENDING
  APPROVED
  REJECTED
  CANCELLED
  RESCHEDULED
  COMPLETED    # ✅ Now matches your Java Status enum exactly
}
```

---

## 🚨 6. ERROR HANDLING

### **Standard HTTP Status Codes**
- `200 OK` - Successful requests
- `404 Not Found` - Resource not found
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Access denied
- `400 Bad Request` - Invalid request data
- `500 Internal Server Error` - Server errors

### **Error Response Format**
```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Booking with ID 'booking-123' not found",
  "timestamp": "2025-08-26T10:30:00Z"
}
```

### **Frontend Error Handling Example**
```javascript
const handleApiError = (error, context = 'API') => {
  console.error(`${context} error:`, error);
  
  if (error.message.includes('401') || error.message.includes('Unauthorized')) {
    // Redirect to login
    window.location.href = '/login';
    return;
  }
  
  // Show user-friendly error message
  const userMessage = error.message.includes('404') 
    ? 'The requested resource was not found.'
    : 'Something went wrong. Please try again.';
  
  // Show toast notification or update UI
  showErrorToast(userMessage);
};
```

---

## 📝 7. USAGE EXAMPLES FOR COMMON SCENARIOS

### **Tenant Dashboard Implementation**
```javascript
// TenantDashboard.jsx
import React, { useState, useEffect } from 'react';

const TenantDashboard = ({ tenantId }) => {
  const [stats, setStats] = useState(null);
  const [visits, setVisits] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setLoading(true);
        
        // Load dashboard stats
        const dashboardStats = await fetchTenantDashboardStats(tenantId);
        setStats(dashboardStats);
        
        // Load recent visits
        const recentVisits = await fetchTenantVisits(tenantId);
        setVisits(recentVisits);
        
        // Load recent bookings
        const recentBookings = await fetchTenantBookings(tenantId, 0, 5);
        setBookings(recentBookings);
        
      } catch (error) {
        handleApiError(error, 'loading dashboard');
      } finally {
        setLoading(false);
      }
    };

    if (tenantId) {
      loadDashboardData();
    }
  }, [tenantId]);

  if (loading) return <div>Loading...</div>;

  return (
    <div className="tenant-dashboard">
      <h1>My Dashboard</h1>
      
      {/* Stats Overview */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Visits</h3>
          <p>{stats?.totalVisits || 0}</p>
        </div>
        <div className="stat-card">
          <h3>Total Bookings</h3>
          <p>{stats?.totalBookings || 0}</p>
        </div>
        <div className="stat-card">
          <h3>Total Spent</h3>
          <p>₦{stats?.totalSpent?.toLocaleString() || 0}</p>
        </div>
      </div>
      
      {/* Recent Visits */}
      <section>
        <h2>Recent Visits</h2>
        {visits.map(visit => (
          <div key={visit.id} className="visit-item">
            <p>Status: {visit.status}</p>
            <p>Scheduled: {visit.scheduledAt}</p>
            {visit.status === 'APPROVED' && (
              <button onClick={() => cancelVisit(visit.id)}>
                Cancel Visit
              </button>
            )}
          </div>
        ))}
      </section>
      
      {/* Recent Bookings */}
      <section>
        <h2>Recent Bookings</h2>
        {bookings.map(booking => (
          <div key={booking.id} className="booking-item">
            <p>Dates: {booking.startDate} to {booking.endDate}</p>
            <p>Status: {booking.status}</p>
            <p>Amount: ₦{booking.paymentAmount?.toLocaleString()}</p>
            {booking.status === 'PENDING' && (
              <button onClick={() => updateBookingStatus(booking.id, 'cancelled')}>
                Cancel Booking
              </button>
            )}
          </div>
        ))}
      </section>
    </div>
  );
};

export default TenantDashboard;
```

### **Visit Cancellation Component**
```javascript
// VisitCancellation.jsx
import React, { useState } from 'react';

const VisitCancellation = ({ visit, onCancel, onClose }) => {
  const [isLoading, setIsLoading] = useState(false);

  const handleCancel = async () => {
    try {
      setIsLoading(true);
      await cancelVisit(visit.id);
      onCancel(visit.id);
      onClose();
    } catch (error) {
      handleApiError(error, 'cancelling visit');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="modal">
      <div className="modal-content">
        <h3>Cancel Visit</h3>
        <p>Are you sure you want to cancel your visit scheduled for {visit.scheduledAt}?</p>
        <div className="modal-actions">
          <button onClick={onClose} disabled={isLoading}>
            Keep Visit
          </button>
          <button 
            onClick={handleCancel} 
            disabled={isLoading}
            className="cancel-button"
          >
            {isLoading ? 'Cancelling...' : 'Cancel Visit'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default VisitCancellation;
```

---

## 🔧 8. TESTING COMMANDS

### **cURL Commands for Testing**

**Test Tenant Dashboard Stats:**
```bash
curl -X POST http://localhost:8181/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ tenantDashboardStats(tenantId: \"eb22925c-1201-48b0-894d-373c825e2778\") { totalVisits pendingVisits totalBookings activeBookings totalSpent visitStats { approved rejected completed cancelled } bookingStats { confirmed pending completed cancelled } } }"
  }'
```

**Test Tenant Bookings:**
```bash
curl -X GET "http://localhost:8181/api/shortlets/bookings/tenant/eb22925c-1201-48b0-894d-373c825e2778?page=0&limit=10" \
  -H "Content-Type: application/json"
```

**Test Visit Cancellation:**
```bash
curl -X PUT http://localhost:8181/api/visits/{visit-id}/cancel \
  -H "Content-Type: application/json"
```

---

## ✅ 9. IMPLEMENTATION CHECKLIST

For frontend developers implementing these features:

### **Tenant Dashboard**
- [ ] Implement dashboard stats display
- [ ] Add booking history table with pagination
- [ ] Add visit history with status indicators
- [ ] Implement visit cancellation modal
- [ ] Add booking status update functionality
- [ ] Handle loading states and error scenarios

### **Landlord Dashboard**  
- [ ] Implement landlord dashboard stats
- [ ] Add booking management interface
- [ ] Add visit request management
- [ ] Implement approve/reject visit functionality

### **Common Features**
- [ ] Add proper error handling for all endpoints
- [ ] Implement authentication token management
- [ ] Add loading indicators
- [ ] Add empty state handling
- [ ] Implement proper date formatting
- [ ] Add status badge components

### **Testing**
- [ ] Test all new endpoints with valid data
- [ ] Test error scenarios (404, 401, 403, 500)
- [ ] Test pagination functionality
- [ ] Test filtering capabilities
- [ ] Test GraphQL queries and mutations

---

## 🎯 10. KEY BENEFITS FOR FRONTEND

With these new endpoints, the frontend can now:

1. **Complete Tenant Experience**: Full booking history, visit tracking, and dashboard analytics
2. **Enhanced Landlord Tools**: Comprehensive booking and visit management
3. **Real-time Data**: Access to all database fields through aligned GraphQL schema
4. **Flexible Querying**: Both REST and GraphQL options for different use cases
5. **Proper Error Handling**: Standardized error responses
6. **Performance**: Pagination and filtering support for large datasets

This implementation provides everything needed for a complete tenant and landlord dashboard experience as specified in the requirements document.
# Frontend Integration Guide: New Tenant & Landlord Endpoints

## 🎯 Overview
This document outlines all the new and updated endpoints for tenant-specific functionality, landlord-specific functionality, visit cancellation, and dashboard analytics that were implemented to align with the requirements document.

---

## 🏠 1. TENANT-SPECIFIC BOOKING ENDPOINTS

### **GET Tenant Booking History**
Retrieve all bookings for a specific tenant with filtering and pagination.

**REST Endpoint:**
```http
GET /api/shortlets/bookings/tenant/{tenantId}
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number for pagination
- `limit` (optional, default: 10) - Number of results per page  
- `status` (optional) - Filter by booking status: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `RESCHEDULED`, `NO_SHOW`

**Frontend Usage:**
```javascript
// Fetch all tenant bookings
const fetchTenantBookings = async (tenantId, page = 0, limit = 10, status = null) => {
  try {
    let url = `/api/shortlets/bookings/tenant/${tenantId}?page=${page}&limit=${limit}`;
    if (status) {
      url += `&status=${status}`;
    }
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching tenant bookings:', error);
    throw error;
  }
};

// Usage examples
const allBookings = await fetchTenantBookings('tenant-uuid-here');
const pendingBookings = await fetchTenantBookings('tenant-uuid-here', 0, 10, 'PENDING');
const page2Bookings = await fetchTenantBookings('tenant-uuid-here', 2, 5);
```

**Response Format:**
```json
[
  {
    "id": "booking-123",
    "tenantId": "tenant-456",
    "landlordId": "landlord-789",
    "propertyId": "property-abc",
    "startDate": "2025-08-27",
    "endDate": "2025-08-30",
    "status": "ACCEPTED",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+234123456789",
    "paymentStatus": "PAID",
    "paymentReference": "PAY-123456",
    "paymentAmount": 45000.0,
    "reminder24hSent": false,
    "reminder1hSent": false,
    "nights": 3
  }
]
```

### **GET Single Booking Details**
Retrieve detailed information about a specific booking.

**REST Endpoint:**
```http
GET /api/shortlets/bookings/details/{bookingId}
```

**Frontend Usage:**
```javascript
const fetchBookingDetails = async (bookingId) => {
  try {
    const response = await fetch(`/api/shortlets/bookings/details/${bookingId}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.status === 404) {
      throw new Error('Booking not found');
    }
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching booking details:', error);
    throw error;
  }
};

// Usage
const bookingDetails = await fetchBookingDetails('booking-uuid-here');
```

### **UPDATE Booking Status**
Update the status of a booking (useful for tenant cancellations).

**REST Endpoint:**
```http
PUT /api/shortlets/bookings/{bookingId}/status
```

**Request Body:**
```json
{
  "status": "cancelled|confirmed|rejected"
}
```

**Frontend Usage:**
```javascript
const updateBookingStatus = async (bookingId, status) => {
  try {
    const response = await fetch(`/api/shortlets/bookings/${bookingId}/status`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ status })
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error updating booking status:', error);
    throw error;
  }
};

// Usage examples
await updateBookingStatus('booking-123', 'cancelled');  // Tenant cancellation
await updateBookingStatus('booking-123', 'confirmed');  // Landlord confirmation
```

---

## 🏢 2. LANDLORD-SPECIFIC BOOKING ENDPOINTS

### **GET Landlord Bookings**
Retrieve all bookings for a specific landlord (existing endpoint, documented for completeness).

**REST Endpoint:**
```http
GET /api/shortlets/bookings/{landlordId}
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Results per page

**Frontend Usage:**
```javascript
const fetchLandlordBookings = async (landlordId, page = 0, size = 20) => {
  try {
    const response = await fetch(`/api/shortlets/bookings/${landlordId}?page=${page}&size=${size}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching landlord bookings:', error);
    throw error;
  }
};
```

---

## 📅 3. VISIT MANAGEMENT ENDPOINTS

### **GraphQL: Get Visits by Visitor (Tenant)**
Retrieve all visits for a specific tenant.

**GraphQL Query:**
```graphql
query GetVisitsByVisitor($visitorId: ID!) {
  visitsByVisitor(visitorId: $visitorId) {
    id
    propertyId
    visitorId
    landlordId
    scheduledAt
    status
    durationMinutes
    notes
    slotId
    rescheduledFromId
    createdAt
    updatedAt
    scheduledDate
  }
}
```

**Frontend Usage:**
```javascript
const fetchTenantVisits = async (tenantId) => {
  const query = `
    query GetVisitsByVisitor($visitorId: ID!) {
      visitsByVisitor(visitorId: $visitorId) {
        id
        propertyId
        visitorId
        landlordId
        scheduledAt
        status
        durationMinutes
        notes
        createdAt
        updatedAt
      }
    }
  `;
  
  try {
    const response = await fetch('/graphql', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query,
        variables: { visitorId: tenantId }
      })
    });
    
    const result = await response.json();
    if (result.errors) {
      throw new Error(result.errors[0].message);
    }
    
    return result.data.visitsByVisitor;
  } catch (error) {
    console.error('Error fetching tenant visits:', error);
    throw error;
  }
};
```

### **GraphQL: Get Visits by Landlord**
Retrieve all visits for a specific landlord.

**GraphQL Query:**
```graphql
query GetVisitsByLandlord($landlordId: ID!) {
  visitsByLandlord(landlordId: $landlordId) {
    id
    propertyId
    visitorId
    landlordId
    scheduledAt
    status
    durationMinutes
    notes
    slotId
    rescheduledFromId
    createdAt
    updatedAt
  }
}
```

### **REST: Cancel Visit**
