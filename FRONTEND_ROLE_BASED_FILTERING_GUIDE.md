# Frontend Guide: Role-Based Issue Filtering

## Overview
The backend now implements role-based filtering for issues:
- **RESIDENT** users can only see issues they reported
- **ADMIN** users can see all issues

The backend automatically filters issues based on the authenticated user's role. The frontend doesn't need to implement additional filtering logic, but should ensure proper authentication.

## Backend Implementation Summary

### What Changed
1. **JwtService** - Now stores user ID with tokens for authentication
2. **IssueService** - Added role-based filtering methods
3. **IssueController** - Extracts user from Authorization header and filters accordingly
4. **IssueRepository** - Added paginated method for user-specific issues

### API Behavior
- `GET /api/issues` - Returns filtered issues based on user role
- `GET /api/issues/search?q=query` - Returns filtered search results based on user role

## Frontend Compliance Guide

### 1. Ensure Authentication Token is Sent

The frontend **must** send the authentication token in the `Authorization` header for all issue-related requests.

**Current Implementation Check:**
Your `api.js` already does this correctly:
```javascript
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  ...
);
```

✅ **This is already correct!** No changes needed.

### 2. No Additional Frontend Filtering Required

The backend handles all filtering automatically. You **do NOT need** to filter issues on the frontend based on user role.

**What the backend does:**
- Extracts user from JWT token
- Checks user role (RESIDENT or ADMIN)
- If RESIDENT: Returns only issues where `reportedBy.id === currentUser.id`
- If ADMIN: Returns all issues

### 3. Frontend Display Considerations

While the backend filters the data, you may want to show different UI elements based on role:

#### Option A: Show Role-Specific Messages (Recommended)
```jsx
// In your Issues.jsx component
import { useAuth } from '../context/AuthContext';

const Issues = () => {
  const { user } = useAuth();
  const isResident = user?.role === 'RESIDENT';
  
  return (
    <div>
      {isResident && (
        <Alert variant="info">
          You are viewing only the issues you have reported.
        </Alert>
      )}
      {/* Rest of your component */}
    </div>
  );
};
```

#### Option B: Update Page Title/Header
```jsx
const pageTitle = user?.role === 'RESIDENT' 
  ? 'My Issues' 
  : 'All Issues';
```

### 4. Testing the Implementation

#### Test as RESIDENT User:
1. Log in with a RESIDENT account
2. Navigate to Issues page
3. Verify you only see issues you reported
4. Try creating a new issue - it should appear in your list
5. Try searching - should only return your issues

#### Test as ADMIN User:
1. Log in with an ADMIN account
2. Navigate to Issues page
3. Verify you see ALL issues from all users
4. Try searching - should return all matching issues

### 5. Error Handling

If authentication fails or token is missing:
- Backend returns empty page (no issues)
- Frontend should handle this gracefully
- Consider redirecting to login if 401 error occurs

**Your current error handling:**
```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

✅ **This is already correct!** No changes needed.

### 6. Issue Creation

When creating issues, ensure the `reportedById` matches the current user:

```javascript
// In your issue creation form
const createIssue = async (issueData) => {
  const user = authService.getCurrentUser();
  
  const payload = {
    ...issueData,
    reportedById: user.id  // Ensure this is set
  };
  
  await issueService.create(payload);
};
```

### 7. Issue Detail Page

For issue detail pages (`/issues/:id`), the backend will:
- Allow RESIDENT users to view their own issues
- Allow ADMIN users to view any issue
- Return 404 if RESIDENT tries to access another user's issue

**Frontend handling:**
```jsx
// In IssueDetail.jsx
const IssueDetail = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const [issue, setIssue] = useState(null);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    issueService.getById(id)
      .then(response => setIssue(response.data))
      .catch(error => {
        if (error.response?.status === 404) {
          setError('Issue not found or you do not have permission to view it');
        }
      });
  }, [id]);
  
  if (error) {
    return <Alert variant="error">{error}</Alert>;
  }
  
  // Rest of component
};
```

## Summary

### ✅ What You DON'T Need to Change:
1. API interceptor (already sends token correctly)
2. Error handling (already redirects on 401)
3. Issue service calls (backend handles filtering)

### ✅ What You MAY Want to Add:
1. Role-specific UI messages
2. Different page titles based on role
3. Better error messages for unauthorized access

### 🔍 How to Verify It's Working:

1. **As RESIDENT:**
   - Create an issue → Should appear in your list
   - View issues page → Should only see your issues
   - Try to access another user's issue → Should get 404

2. **As ADMIN:**
   - View issues page → Should see all issues from all users
   - Search issues → Should return all matching issues

## Backend API Details

### Request Format
```
GET /api/issues?page=0&size=10&sort=dateReported&direction=DESC
Headers:
  Authorization: Bearer <token>
```

### Response Format
```json
{
  "content": [...],  // Filtered issues based on role
  "totalElements": 10,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### Role-Based Behavior
- **RESIDENT**: `content` contains only issues where `reportedBy.id === currentUser.id`
- **ADMIN**: `content` contains all issues
- **No Auth**: `content` is empty array

## Troubleshooting

### Issue: RESIDENT sees all issues
- **Check**: Is the Authorization header being sent?
- **Check**: Is the token valid?
- **Check**: Does the user have the correct role in the database?

### Issue: Empty results for authenticated user
- **Check**: Does the user have any issues?
- **Check**: For RESIDENT, are the issues assigned to them?
- **Check**: Backend logs for errors

### Issue: 401 Unauthorized
- **Check**: Token expiration
- **Check**: Token format (should be "Bearer <token>")
- **Check**: User still exists in database


