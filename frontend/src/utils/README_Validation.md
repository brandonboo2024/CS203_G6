# Frontend Input Validation Implementation

## Overview

This implementation provides comprehensive input validation for the frontend application to prevent dangerous values and ensure data integrity. The validation system uses both **blacklisting** (rejecting known dangerous patterns) and **whitelisting** (only allowing specific safe patterns) approaches.

## Security Features

### 1. Blacklisting Dangerous Patterns

The validation system blocks the following dangerous patterns:

#### Script Injection
- `<script>` tags
- `javascript:` protocols
- Event handlers (`onclick`, `onload`, etc.)
- `<iframe>`, `<object>`, `<embed>` tags
- `<link>`, `<meta>`, `<style>` tags

#### SQL Injection
- SQL keywords (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `DROP`, etc.)
- SQL operators (`OR`, `AND` with injection patterns)
- Union-based attacks
- Comment-based attacks (`--`, `/*`)

#### Command Injection
- Shell metacharacters (`;`, `&`, `|`, `` ` ``, `$`, `()`, `{}`, `[]`)
- Dangerous commands
- Path traversal (`../`)

#### XSS Prevention
- `alert()`, `confirm()`, `prompt()` functions
- `document.` and `window.` access
- `eval()`, `Function()` constructors
- `setTimeout()`, `setInterval()` with code

#### File System Protection
- Path traversal patterns
- System directory access
- Control characters

### 2. Whitelisting Safe Patterns

The system only allows specific, safe patterns for each input type:

#### Country Codes
- Exactly 2 uppercase letters
- Must be from predefined whitelist

#### Usernames
- 3-20 characters
- Alphanumeric, underscore, hyphen only

#### Email Addresses
- Standard email format validation
- Domain validation

#### Product Names
- 1-50 characters
- Letters, numbers, spaces, hyphens only
- Must be from predefined product list

#### Quantities
- Positive integers only
- Maximum value limits

#### Passwords
- Minimum 8 characters
- Must contain letters and numbers
- Maximum 128 characters

#### File Uploads
- Only image file types
- Maximum 5MB file size
- Filename validation

## Implementation Details

### Core Functions

#### `validateInput(input)`
- Base validation function
- Checks for dangerous patterns
- Enforces length limits
- Returns validation result with errors

#### `validateCountryCode(countryCode)`
- Validates country code format
- Checks against whitelist
- Prevents injection attacks

#### `validateUsername(username)`
- Enforces username format rules
- Prevents special characters
- Length validation

#### `validateEmail(email)`
- Standard email format validation
- Prevents email injection

#### `validateProduct(product)`
- Product name format validation
- Whitelist-based product validation

#### `validateQuantity(quantity)`
- Numeric validation
- Range checking
- Prevents overflow attacks

#### `validatePassword(password)`
- Strong password requirements
- Character set validation
- Length limits

#### `validateDateTime(datetime)`
- Date format validation
- Range checking (10 years past/future)
- Prevents date-based attacks

#### `validateFileUpload(file)`
- File type validation
- Size limits
- Filename security

#### `sanitizeInput(input)`
- Removes dangerous characters
- Cleans control characters
- Trims whitespace

#### `validateForm(formData, formType)`
- Comprehensive form validation
- Type-specific validation
- Returns consolidated results

### Integration

The validation is integrated into all major form components:

1. **TariffCalc.jsx** - Validates country codes, products, quantities, dates
2. **Login.jsx** - Validates usernames and passwords
3. **RegisterPage.jsx** - Validates all registration fields
4. **Profile.jsx** - Validates file uploads

### Error Handling

- Real-time validation feedback
- User-friendly error messages
- Visual error indicators
- Prevents form submission with invalid data

## Usage Examples

### Basic Validation
```javascript
import { validateInput } from '../utils/inputValidation';

const result = validateInput(userInput);
if (!result.isValid) {
  console.log('Errors:', result.errors);
}
```

### Form Validation
```javascript
import { validateForm } from '../utils/inputValidation';

const formData = { username, email, password };
const validation = validateForm(formData, 'register');
if (!validation.isValid) {
  setErrors(validation.errors);
}
```

### Input Sanitization
```javascript
import { sanitizeInput } from '../utils/inputValidation';

const cleanInput = sanitizeInput(dangerousInput);
```

## Security Benefits

1. **XSS Prevention** - Blocks script injection attacks
2. **SQL Injection Protection** - Prevents database attacks
3. **Command Injection Prevention** - Blocks shell command execution
4. **Path Traversal Protection** - Prevents file system access
5. **Data Integrity** - Ensures only valid data is processed
6. **Input Length Limits** - Prevents DoS attacks
7. **File Upload Security** - Validates file types and sizes

## Testing

The validation system includes comprehensive test cases covering:
- Dangerous input patterns
- Valid input acceptance
- Sanitization effectiveness
- Edge cases and boundary conditions

Run the test suite to verify validation functionality:
```javascript
import './validationTest.js';
```

## Maintenance

- Regularly update dangerous patterns list
- Add new whitelist entries as needed
- Monitor for new attack vectors
- Update validation rules based on security requirements

## Best Practices

1. Always validate on both client and server side
2. Use whitelisting when possible
3. Sanitize inputs before processing
4. Provide clear error messages
5. Log validation failures
6. Monitor for attack patterns
7. Keep validation rules updated
