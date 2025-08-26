# Backend Requirements: Booking & Visiting Service

**✅ COMPREHENSIVE BOOKING ENDPOINTS INCLUDED**

## 🎯 Overview
This document outlines the complete backend requirements for implementing the ZenNest Booking & Visiting Service that supports both tenant visits (property viewings) and short-stay bookings.

**Key Booking Endpoints Covered:**
- 📋 **GET All Bookings**: `/api/shortlets/bookings` (admin/management)
- 🔍 **GET Booking by ID**: `/api/shortlets/bookings/{bookingId}`
- 👤 **Tenant Booking History**: `/api/shortlets/bookings/tenant/{tenantId}`
- 🏠 **Landlord Bookings**: `/api/shortlets/bookings/landlord/{landlordId}`
- ➕ **CREATE Booking**: `POST /api/shortlets/bookings`
- ✏️ **UPD---

## ✅ TENANT DASHBOARD COMPLETENESS

### With These Endpoints, The Tenant Dashboard Will Have:

#### 1. **Complete Booking Management** 🏠
- ✅ View all booking history with status badges
- ✅ Filter by booking status (pending, confirmed, completed, cancelled)
- ✅ See upcoming vs past bookings
- ✅ View detailed booking information (dates, property, amount)
- ✅ Cancel upcoming bookings
- ✅ Contact landlord for each booking

#### 2. **Visit Scheduling System** 📅
- ✅ Schedule property visits
- ✅ View upcoming visit appointments
- ✅ See visit history and status
- ✅ Cancel/reschedule visits
- ✅ Get visit confirmations from landlords

#### 3. **Dashboard Analytics** 📊
- ✅ Total bookings count
- ✅ Total spent on bookings
- ✅ Upcoming bookings count
- ✅ Visit statistics

#### 4. **Payment Tracking** 💳 (Phase 2)
- ✅ View payment history
- ✅ Outstanding payment alerts
- ✅ Payment status for bookings
- ✅ Download payment receipts

#### 5. **Favorites System** ⭐ (Phase 2)
- ✅ Save favorite properties
- ✅ Quick access to liked properties
- ✅ Get notifications on price changes

### Error Scenarios Handled:
- **Network failures** → Retry buttons & offline indicators
- **Authentication issues** → Auto-redirect to login
- **Permission errors** → Clear access denied messages
- **Data not found** → Helpful empty states
- **Booking conflicts** → Alternative date suggestions
- **Validation errors** → Field-specific error messages
- **Server errors** → Generic fallback with support contact

### Loading States Covered:
- **Initial page load** → Skeleton screens
- **Data fetching** → Loading spinners
- **Action processing** → Button loading states
- **Background updates** → Subtle progress indicators

### What Makes It Complete:
1. **Full CRUD operations** on bookings
2. **Real-time status updates** via WebSocket (optional)
3. **Comprehensive error handling** for all edge cases
4. **Responsive design** works on all devices
5. **Accessibility features** for screen readers
6. **Performance optimization** with pagination
7. **Search & filter** capabilities
8. **Export functionality** for booking data

---

## 🔧 Database Schema Suggestions Booking**: `PUT /api/shortlets/bookings/{bookingId}`
- 🔄 **Update Status**: `PUT /api/shortlets/bookings/{bookingId}/status`
- ❌ **Cancel/Confirm**: Role-based booking actions
- 🗑️ **DELETE Booking**: `DELETE /api/shortlets/bookings/{bookingId}`

## 📋 Current Implementation Status

### ✅ Already Working
- **User Management**: `GET /api/zennest/users/user-details`
- **Property Listings**: `GET /listing/api/listings/owner/{ownerId}`
- **Visit Slot Management**: `GET /api/visits?propertyId={id}&landlordId={id}`
- **Visit Slot Booking**: `POST /api/visits/book`
- **Landlord Booking Management**: `GET /api/shortlets/bookings/{landlordId}`

### 🚨 Missing/Broken Components
1. **Tenant Visit Tracking**: GraphQL query `visitsByVisitor` doesn't exist
2. **Tenant Booking History**: No endpoint for tenant's short-stay bookings
3. **Payment System**: No tenant payment tracking
4. **Favorites System**: No saved properties functionality

---

## 🏠 1. TENANT VISIT SYSTEM

### GraphQL Schema Extensions
**Endpoint**: `https://qorelabs.online/graphql/zennest`

```graphql
type Query {
  # Tenant-side visit queries
  visitsByVisitor(visitorId: String!): [Visit!]!
  
  # Landlord-side visit queries  
  visitsByProperty(propertyId: String!): [Visit!]!
  visitsByLandlord(landlordId: String!): [Visit!]!
}

type Mutation {
  # Visit management
  updateVisitStatus(
    visitId: String!, 
    status: VisitStatus!, 
    notes: String
  ): Visit!
  
  cancelVisit(
    visitId: String!, 
    reason: String
  ): Visit!
}

type Visit {
  id: ID!
  visitorId: String!           # Tenant who requested visit
  landlordId: String!          # Property owner
  propertyId: String!          # Property being visited
  status: VisitStatus!         # Current status
  scheduledDate: String        # Date of visit (YYYY-MM-DD)
  scheduledAt: String          # Time of visit (HH:MM)
  durationMinutes: Int         # Duration in minutes
  createdAt: String           # When request was made
  updatedAt: String           # Last status change
  
  # Enhanced display fields
  propertyTitle: String       # Property name/title
  propertyAddress: String     # Property address
  tenantName: String          # Visitor's name
  tenantEmail: String         # Visitor's email
  tenantPhone: String         # Visitor's phone
  landlordNotes: String       # Private landlord notes
  visitNotes: String          # Visit-specific notes
}

enum VisitStatus {
  PENDING                     # Awaiting landlord approval
  APPROVED                    # Confirmed by landlord
  REJECTED                    # Declined by landlord
  CANCELLED                   # Cancelled by tenant
  COMPLETED                   # Visit completed
  NO_SHOW                     # Tenant didn't show up
}
```

### Sample GraphQL Usage

**Frontend Query**:
```javascript
{
  query: `
    query GetVisitsByVisitor($visitorId: String!) {
      visitsByVisitor(visitorId: $visitorId) {
        id
        visitorId
        landlordId
        propertyId
        status
        scheduledDate
        scheduledAt
        durationMinutes
        createdAt
        propertyTitle
        propertyAddress
      }
    }
  `,
  variables: { visitorId: "tenant-123" }
}
```

**Expected Response**:
```json
{
  "data": {
    "visitsByVisitor": [
      {
        "id": "visit-456",
        "visitorId": "tenant-123",
        "landlordId": "landlord-789",
        "propertyId": "property-abc",
        "status": "PENDING",
        "scheduledDate": "2025-08-27",
        "scheduledAt": "14:30",
        "durationMinutes": 60,
        "createdAt": "2025-08-25T10:30:00Z",
        "propertyTitle": "Modern 2BR Apartment",
        "propertyAddress": "Lekki Phase 1, Lagos"
      }
    ]
  }
}
```

---

## 🏨 2. TENANT BOOKING SYSTEM

### REST API Endpoints
**Base URL**: `https://qorelabs.xyz/api/shortlets/`

#### New Endpoints Needed

```bash
# GET ALL BOOKINGS (Admin/Management)
GET /api/shortlets/bookings
Authorization: Bearer {token}
Query params: ?page=1&limit=20&status=all&dateFrom=2025-01-01&dateTo=2025-12-31

# GET BOOKING BY ID
GET /api/shortlets/bookings/{bookingId}
Authorization: Bearer {token}

# Response format for single booking
{
  "id": "booking-123",
  "tenantId": "tenant-456",
  "landlordId": "landlord-789", 
  "propertyId": "property-abc",
  "status": "confirmed|pending|cancelled|completed",
  "checkIn": "2025-08-27T15:00:00Z",
  "checkOut": "2025-08-30T11:00:00Z",
  "totalAmount": 45000,
  "currency": "NGN",
  "guestCount": 2,
  "guestFirstName": "John",
  "guestLastName": "Doe",
  "guestEmail": "john@email.com",
  "guestPhone": "+234123456789",
  "propertyTitle": "Luxury Villa in Lekki",
  "propertyAddress": "Lekki Phase 1, Lagos",
  "propertyImages": ["url1", "url2"],
  "landlordName": "Jane Smith",
  "landlordPhone": "+234987654321",
  "createdAt": "2025-08-25T10:30:00Z",
  "updatedAt": "2025-08-25T14:00:00Z"
}

# TENANT BOOKING HISTORY
GET /api/shortlets/bookings/tenant/{tenantId}
Authorization: Bearer {token}
Query params: ?page=1&limit=10&status=all

# Response format (array of bookings)
[
  {
    "id": "booking-123",
    "tenantId": "tenant-456",
    "landlordId": "landlord-789",
    "propertyId": "property-abc",
    "status": "confirmed|pending|cancelled|completed",
    "checkIn": "2025-08-27T15:00:00Z",
    "checkOut": "2025-08-30T11:00:00Z",
    "totalAmount": 45000,
    "currency": "NGN",
    "guestCount": 2,
    "guestFirstName": "John",
    "guestLastName": "Doe",
    "guestEmail": "john@email.com",
    "guestPhone": "+234123456789",
    "propertyTitle": "Luxury Villa in Lekki",
    "propertyAddress": "Lekki Phase 1, Lagos",
    "createdAt": "2025-08-25T10:30:00Z",
    "updatedAt": "2025-08-25T14:00:00Z"
  }
]

# LANDLORD BOOKING MANAGEMENT
GET /api/shortlets/bookings/landlord/{landlordId}
Authorization: Bearer {token}
Query params: ?page=1&limit=10&status=all&propertyId=optional

# CREATE NEW BOOKING
POST /api/shortlets/bookings
Content-Type: application/json
Authorization: Bearer {token}

{
  "tenantId": "tenant-456",
  "propertyId": "property-abc", 
  "checkIn": "2025-08-27T15:00:00Z",
  "checkOut": "2025-08-30T11:00:00Z",
  "guestCount": 2,
  "totalAmount": 45000,
  "currency": "NGN",
  "guestInfo": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@email.com",
    "phone": "+234123456789"
  }
}

# UPDATE BOOKING STATUS (General)
PUT /api/shortlets/bookings/{bookingId}/status
Content-Type: application/json
Authorization: Bearer {token}

{
  "status": "confirmed|cancelled|completed",
  "reason": "Optional cancellation reason"
}

# UPDATE BOOKING (Full update)
PUT /api/shortlets/bookings/{bookingId}
Content-Type: application/json
Authorization: Bearer {token}

{
  "checkIn": "2025-08-27T15:00:00Z",
  "checkOut": "2025-08-30T11:00:00Z", 
  "guestCount": 2,
  "totalAmount": 45000,
  "guestInfo": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@email.com",
    "phone": "+234123456789"
  }
}

# DELETE BOOKING
DELETE /api/shortlets/bookings/{bookingId}
Authorization: Bearer {token}

# TENANT-SPECIFIC ACTIONS
PUT /api/shortlets/bookings/{bookingId}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Change of plans"
}

# LANDLORD-SPECIFIC ACTIONS
PUT /api/shortlets/bookings/{bookingId}/confirm
Authorization: Bearer {token}

PUT /api/shortlets/bookings/{bookingId}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Property unavailable"
}
```

---

## 💳 3. PAYMENT SYSTEM

### REST API Endpoints
**Base URL**: `https://qorelabs.xyz/api/payments/`

```bash
# Tenant payment history
GET /api/payments/tenant/{tenantId}
Authorization: Bearer {token}

# Response format
[
  {
    "id": "payment-123",
    "tenantId": "tenant-456",
    "landlordId": "landlord-789",
    "propertyId": "property-abc",
    "bookingId": "booking-def",        # If related to booking
    "amount": 45000,
    "currency": "NGN",
    "type": "rent|deposit|booking|fee",
    "status": "pending|completed|failed|refunded",
    "paymentMethod": "card|bank|transfer",
    "description": "Monthly rent for Property ABC",
    "dueDate": "2025-08-30",
    "paidAt": "2025-08-25T10:30:00Z",
    "createdAt": "2025-08-25T09:00:00Z"
  }
]

# Make payment
POST /api/payments/tenant/{tenantId}/make-payment
Content-Type: application/json
Authorization: Bearer {token}

{
  "amount": 45000,
  "currency": "NGN",
  "type": "booking",
  "bookingId": "booking-def",
  "paymentMethod": "card",
  "description": "Payment for 3-night stay"
}

# Get payment details
GET /api/payments/{paymentId}
Authorization: Bearer {token}
```

---

## ⭐ 4. FAVORITES SYSTEM

### REST API Endpoints
**Base URL**: `https://qorelabs.xyz/api/favorites/`

```bash
# Get tenant favorites
GET /api/favorites/tenant/{tenantId}
Authorization: Bearer {token}

# Response format
[
  {
    "id": "fav-123",
    "tenantId": "tenant-456",
    "propertyId": "property-abc",
    "propertyTitle": "Modern 2BR Apartment",
    "propertyAddress": "Lekki Phase 1, Lagos",
    "propertyPrice": 500000,
    "propertyImages": ["image1.jpg", "image2.jpg"],
    "propertyType": "RENT|SHORT_STAY|SALE",
    "addedAt": "2025-08-25T10:30:00Z"
  }
]

# Add to favorites
POST /api/favorites/tenant/{tenantId}/add/{propertyId}
Authorization: Bearer {token}

# Remove from favorites
DELETE /api/favorites/tenant/{tenantId}/remove/{propertyId}
Authorization: Bearer {token}

# Check if property is favorited
GET /api/favorites/tenant/{tenantId}/check/{propertyId}
Authorization: Bearer {token}

# Response: { "isFavorite": true }
```

---

## 📊 5. DASHBOARD ANALYTICS

### REST API Endpoints
**Base URL**: `https://qorelabs.xyz/api/dashboard/`

```bash
# Tenant dashboard stats
GET /api/dashboard/tenant/{tenantId}/stats
Authorization: Bearer {token}

# Response format
{
  "totalVisits": 5,
  "pendingVisits": 2,
  "upcomingVisits": 1,
  "totalBookings": 3,
  "activeBookings": 1,
  "favoriteProperties": 8,
  "totalSpent": 125000,
  "nextPaymentDue": "2025-08-30",
  "nextVisitDate": "2025-08-27",
  "visitStats": {
    "approved": 3,
    "rejected": 1,
    "completed": 1
  },
  "bookingStats": {
    "confirmed": 2,
    "pending": 1,
    "completed": 0
  }
}
```

---

## 🚀 Implementation Priority

### Phase 1: Critical (Immediate)
1. **GraphQL visitsByVisitor query** - Fixes broken TenantVisits.jsx
2. **GraphQL visitsByProperty query** - Supports landlord views
3. **Tenant booking endpoint** - Enables TenantBookings.jsx

### Phase 2: High Priority
1. **Payment system endpoints** - Tenant payment tracking
2. **Favorites system** - Saved properties functionality
3. **Visit status management** - Update/cancel visits

### Phase 3: Enhancement
1. **Dashboard analytics** - Stats and insights
2. **Advanced filtering** - Search and filter bookings/visits
3. **Notification system** - Status change notifications

---

## � ERROR HANDLING & STATUS CODES

### Expected HTTP Status Codes

#### Success Responses
- **200 OK**: Successful GET requests, successful updates
- **201 Created**: Successful POST requests (new booking/visit created)
- **204 No Content**: Successful DELETE requests

#### Client Error Responses
- **400 Bad Request**: Invalid request data, validation errors
- **401 Unauthorized**: Missing or invalid authentication token
- **403 Forbidden**: User doesn't have permission for this resource
- **404 Not Found**: Booking/visit/property not found
- **409 Conflict**: Booking conflicts (dates unavailable, double booking)
- **422 Unprocessable Entity**: Business logic validation fails

#### Server Error Responses
- **500 Internal Server Error**: Database errors, unexpected server issues
- **503 Service Unavailable**: External service dependencies down

### Error Response Format
```json
{
  "error": {
    "code": "BOOKING_NOT_FOUND",
    "message": "Booking with ID 'booking-123' not found",
    "details": {
      "bookingId": "booking-123",
      "timestamp": "2025-08-26T10:30:00Z"
    }
  }
}
```

### Common Error Codes Expected

#### Authentication & Authorization
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired authentication token"
  }
}

{
  "error": {
    "code": "FORBIDDEN_ACCESS", 
    "message": "You don't have permission to access this tenant's bookings"
  }
}
```

#### Booking-Related Errors
```json
{
  "error": {
    "code": "BOOKING_NOT_FOUND",
    "message": "Booking with ID 'booking-123' not found"
  }
}

{
  "error": {
    "code": "BOOKING_CONFLICT",
    "message": "Property not available for selected dates",
    "details": {
      "conflictingBookings": ["booking-456", "booking-789"],
      "availableFrom": "2025-08-30T00:00:00Z"
    }
  }
}

{
  "error": {
    "code": "INVALID_BOOKING_STATUS",
    "message": "Cannot cancel a booking that is already completed",
    "details": {
      "currentStatus": "completed",
      "allowedTransitions": []
    }
  }
}

{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid booking data provided",
    "details": {
      "fields": {
        "checkIn": "Check-in date cannot be in the past",
        "guestCount": "Guest count must be between 1 and 10"
      }
    }
  }
}
```

#### Visit-Related Errors
```json
{
  "error": {
    "code": "VISIT_NOT_FOUND",
    "message": "Visit with ID 'visit-123' not found"
  }
}

{
  "error": {
    "code": "VISIT_SLOT_UNAVAILABLE",
    "message": "Requested visit slot is no longer available",
    "details": {
      "requestedDate": "2025-08-27",
      "requestedTime": "14:30",
      "availableSlots": [
        {"date": "2025-08-27", "time": "16:00"},
        {"date": "2025-08-28", "time": "10:00"}
      ]
    }
  }
}
```

#### Property-Related Errors
```json
{
  "error": {
    "code": "PROPERTY_NOT_FOUND", 
    "message": "Property with ID 'property-123' not found"
  }
}

{
  "error": {
    "code": "PROPERTY_INACTIVE",
    "message": "Property is not available for booking",
    "details": {
      "propertyStatus": "maintenance",
      "availableFrom": "2025-09-01T00:00:00Z"
    }
  }
}
```

### Frontend Error Handling Strategy

#### In TenantBookings.jsx Component
```javascript
const handleApiError = (error, context = 'booking') => {
  console.error(`${context} error:`, error);
  
  if (error.response?.status === 401) {
    // Redirect to login
    window.location.href = '/login';
    return;
  }
  
  const errorData = error.response?.data?.error;
  let userMessage = 'Something went wrong. Please try again.';
  
  switch (errorData?.code) {
    case 'BOOKING_NOT_FOUND':
      userMessage = 'This booking no longer exists.';
      break;
    case 'FORBIDDEN_ACCESS':
      userMessage = 'You don\'t have permission to view these bookings.';
      break;
    case 'BOOKING_CONFLICT':
      userMessage = 'These dates are no longer available.';
      break;
    case 'VALIDATION_ERROR':
      userMessage = errorData.details?.fields ? 
        Object.values(errorData.details.fields).join(', ') :
        'Please check your booking details.';
      break;
    default:
      userMessage = errorData?.message || userMessage;
  }
  
  setError(userMessage);
  
  // Show toast notification
  toast.error(userMessage);
};

// Usage in API calls
const fetchBookings = async () => {
  try {
    setLoading(true);
    setError(null);
    
    const response = await fetchWithAuth(`/api/shortlets/bookings/tenant/${tenantId}`);
    setBookings(response);
  } catch (error) {
    handleApiError(error, 'fetching bookings');
  } finally {
    setLoading(false);
  }
};
```

#### Loading States & Empty States
```javascript
// Loading state
if (loading) {
  return (
    <div className="flex justify-center items-center p-8">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <span className="ml-2">Loading your bookings...</span>
    </div>
  );
}

// Error state
if (error) {
  return (
    <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-center">
      <div className="text-red-600 mb-2">
        <ExclamationTriangleIcon className="h-8 w-8 mx-auto mb-2" />
        <h3 className="text-lg font-medium">Unable to load bookings</h3>
      </div>
      <p className="text-red-700 mb-4">{error}</p>
      <button 
        onClick={fetchBookings}
        className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
      >
        Try Again
      </button>
    </div>
  );
}

// Empty state  
if (bookings.length === 0) {
  return (
    <div className="text-center py-12">
      <HomeIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
      <h3 className="text-lg font-medium text-gray-900 mb-2">No bookings yet</h3>
      <p className="text-gray-600 mb-6">When you book a stay, it will appear here.</p>
      <Link 
        to="/properties" 
        className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
      >
        Browse Properties
      </Link>
    </div>
  );
}
```

---

## �🔧 Database Schema Suggestions

### Visits Table
```sql
CREATE TABLE visits (
  id VARCHAR(255) PRIMARY KEY,
  visitor_id VARCHAR(255) NOT NULL,
  landlord_id VARCHAR(255) NOT NULL,
  property_id VARCHAR(255) NOT NULL,
  status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'),
  scheduled_date DATE,
  scheduled_at TIME,
  duration_minutes INT DEFAULT 60,
  landlord_notes TEXT,
  visit_notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_visitor (visitor_id),
  INDEX idx_landlord (landlord_id),
  INDEX idx_property (property_id),
  INDEX idx_status (status),
  INDEX idx_scheduled_date (scheduled_date)
);
```

### Tenant Bookings Table Extension
```sql
-- Extend existing shortlet_bookings table or create view
ALTER TABLE shortlet_bookings ADD COLUMN tenant_id VARCHAR(255);
ALTER TABLE shortlet_bookings ADD INDEX idx_tenant (tenant_id);
```

### Favorites Table
```sql
CREATE TABLE tenant_favorites (
  id VARCHAR(255) PRIMARY KEY,
  tenant_id VARCHAR(255) NOT NULL,
  property_id VARCHAR(255) NOT NULL,
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE KEY unique_tenant_property (tenant_id, property_id),
  INDEX idx_tenant (tenant_id),
  INDEX idx_property (property_id)
);
```

### Payments Table
```sql
CREATE TABLE tenant_payments (
  id VARCHAR(255) PRIMARY KEY,
  tenant_id VARCHAR(255) NOT NULL,
  landlord_id VARCHAR(255),
  property_id VARCHAR(255),
  booking_id VARCHAR(255),
  amount DECIMAL(10,2) NOT NULL,
  currency VARCHAR(3) DEFAULT 'NGN',
  type ENUM('rent', 'deposit', 'booking', 'fee'),
  status ENUM('pending', 'completed', 'failed', 'refunded'),
  payment_method VARCHAR(50),
  description TEXT,
  due_date DATE,
  paid_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_tenant (tenant_id),
  INDEX idx_status (status),
  INDEX idx_type (type),
  INDEX idx_due_date (due_date)
);
```

---

## 📱 Frontend Components Affected

### Fixed by Backend Implementation
- **TenantVisits.jsx** ✅ (needs visitsByVisitor GraphQL query)
- **PendingVisits.jsx** ✅ (needs visitsByVisitor GraphQL query)
- **TenantBookings.jsx** ✅ (needs tenant bookings endpoint)
- **Payments.jsx** ✅ (needs payment endpoints)
- **SavedListings.jsx** ✅ (needs favorites endpoints)

### Already Working
- **UpcomingVisits.jsx** ✅
- **PastVisits.jsx** ✅
- **UpcomingBookings.jsx** ✅
- **PastBookings.jsx** ✅
- **MyVisits.jsx** ✅

---

## 🔒 Authentication & Authorization

All endpoints require:
- **Bearer token authentication** via `Authorization: Bearer {token}` header
- **Role-based access**: Tenants can only access their own data
- **Data isolation**: Proper tenant-landlord data separation

## 📚 Error Handling

Standard HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

Error response format:
```json
{
  "error": "INVALID_REQUEST",
  "message": "Visit not found",
  "timestamp": "2025-08-25T10:30:00Z"
}
```

This completes the comprehensive backend requirements for the ZenNest Booking & Visiting Service!
