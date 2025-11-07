/**
 * Input validation utilities to prevent dangerous values
 * Implements both whitelisting and blacklisting approaches
 */

// Dangerous patterns to blacklist (general)
const DANGEROUS_PATTERNS = [
  // Script injection patterns
  /<script[^>]*>.*?<\/script>/gi,
  /javascript:/gi,
  /on\w+\s*=/gi,
  /<iframe[^>]*>.*?<\/iframe>/gi,
  /<object[^>]*>.*?<\/object>/gi,
  /<embed[^>]*>.*?<\/embed>/gi,
  /<link[^>]*>.*?<\/link>/gi,
  /<meta[^>]*>.*?<\/meta>/gi,
  /<style[^>]*>.*?<\/style>/gi,
  
  // SQL injection patterns
  /(\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT)\b)/gi,
  /(\b(OR|AND)\s+\d+\s*=\s*\d+)/gi,
  /(\b(OR|AND)\s+'.*'\s*=\s*'.*')/gi,
  /(\b(OR|AND)\s+".*"\s*=\s*".*")/gi,
  /(UNION\s+SELECT)/gi,
  /(DROP\s+TABLE)/gi,
  /(DELETE\s+FROM)/gi,
  /(INSERT\s+INTO)/gi,
  /(UPDATE\s+SET)/gi,
  
  // Path traversal patterns
  /\.\.\//g,
  /\.\.\\/g,
  /%2e%2e%2f/gi,
  /%2e%2e%5c/gi,
  
  // XSS patterns
  /(alert|confirm|prompt)\s*\(/gi,
  /document\./gi,
  /window\./gi,
  /eval\s*\(/gi,
  /Function\s*\(/gi,
  /setTimeout\s*\(/gi,
  /setInterval\s*\(/gi,
  
  // File system patterns
  /(\/etc\/|\/proc\/|\/sys\/|\/dev\/)/gi,
  /(C:\\|D:\\|E:\\)/gi,
  
  // Special characters that could be dangerous
  /[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, // Control characters
];

// Dangerous patterns for command injection (more restrictive)
const COMMAND_INJECTION_PATTERNS = [
  /[;&|`$(){}[\]]/g,
  /(\b(cat|ls|pwd|whoami|id|uname|ps|netstat|ifconfig|ping|nslookup|dig|wget|curl|nc|telnet|ssh|ftp|telnet)\b)/gi,
];

// Whitelist patterns for specific input types
const WHITELIST_PATTERNS = {
  // Country/partner codes - allow alphanumeric codes like 702, SG, EU
  countryCode: /^[A-Z0-9_-]{2,6}$/,
  
  // Username - alphanumeric, underscore, hyphen, and common special chars, 3-20 chars
  username: /^[a-zA-Z0-9_\-.;&|`$(){}[\]@#!%^*+=~]{3,20}$/,
  
  // Email - standard email format
  email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  
  // Product codes (HS codes, aggregate labels)
  product: /^[A-Za-z0-9_\-]{1,20}$/,
  
  // Quantity - positive integers only
  quantity: /^[1-9]\d*$/,
  
  // Password - at least 8 chars, must contain letter and number, allows special chars
  password: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&.;&|`$(){}[\]^+=~]{8,}$/,
  
  // File extensions for images
  imageFile: /\.(jpg|jpeg|png|gif|bmp|webp)$/i,
};

/**
 * Check if input contains dangerous patterns
 * @param {string} input - Input to validate
 * @param {boolean} allowCommandChars - Whether to allow command injection characters
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateInput = (input, allowCommandChars = false) => {
  if (!input || typeof input !== 'string') {
    return { isValid: false, errors: ['Input is required and must be a string'] };
  }

  const errors = [];
  
  // Check for dangerous patterns
  for (const pattern of DANGEROUS_PATTERNS) {
    if (pattern.test(input)) {
      errors.push(`Input contains potentially dangerous content: ${pattern.source}`);
    }
  }
  
  // Check for command injection patterns (only if not allowing command chars)
  if (!allowCommandChars) {
    for (const pattern of COMMAND_INJECTION_PATTERNS) {
      if (pattern.test(input)) {
        errors.push(`Input contains potentially dangerous content: ${pattern.source}`);
      }
    }
  }
  
  // Check for excessive length (prevent DoS)
  if (input.length > 1000) {
    errors.push('Input is too long (maximum 1000 characters)');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate country code using whitelist
 * @param {string} countryCode - Country code to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateCountryCode = (countryCode) => {
  const baseValidation = validateInput(countryCode);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  const normalized = countryCode.toUpperCase();
  if (!WHITELIST_PATTERNS.countryCode.test(normalized)) {
    errors.push('Country code must be 2-6 characters using letters, numbers, underscores, or hyphens');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate username using whitelist
 * @param {string} username - Username to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateUsername = (username) => {
  // Allow command characters for usernames (they're safe in this context)
  const baseValidation = validateInput(username, true);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  if (!WHITELIST_PATTERNS.username.test(username)) {
    errors.push('Username must be 3-20 characters, alphanumeric, underscore, hyphen, or common special characters');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate email using whitelist
 * @param {string} email - Email to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateEmail = (email) => {
  const baseValidation = validateInput(email);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  if (!WHITELIST_PATTERNS.email.test(email)) {
    errors.push('Please enter a valid email address');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate product name using whitelist
 * @param {string} product - Product to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateProduct = (product) => {
  const baseValidation = validateInput(product);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  if (!WHITELIST_PATTERNS.product.test(product)) {
    errors.push('Product code can only contain letters, numbers, underscores, or hyphens (max 20 chars)');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate quantity using whitelist
 * @param {string|number} quantity - Quantity to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateQuantity = (quantity) => {
  const quantityStr = String(quantity);
  const baseValidation = validateInput(quantityStr);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  if (!WHITELIST_PATTERNS.quantity.test(quantityStr)) {
    errors.push('Quantity must be a positive integer (1 or greater)');
  }
  
  const numValue = parseInt(quantity);
  if (numValue > 10000) {
    errors.push('Quantity cannot exceed 10,000');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate password using whitelist
 * @param {string} password - Password to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validatePassword = (password) => {
  // Allow command characters for passwords (they're safe in this context)
  const baseValidation = validateInput(password, true);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  if (!WHITELIST_PATTERNS.password.test(password)) {
    errors.push('Password must be at least 8 characters with at least one letter and one number, special characters allowed');
  }
  
  if (password.length > 128) {
    errors.push('Password is too long (maximum 128 characters)');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate datetime input
 * @param {string} datetime - Datetime string to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateDateTime = (datetime) => {
  const baseValidation = validateInput(datetime);
  if (!baseValidation.isValid) {
    return baseValidation;
  }
  
  const errors = [...baseValidation.errors];
  
  // Check if it's a valid datetime-local format
  const date = new Date(datetime);
  if (isNaN(date.getTime())) {
    errors.push('Please enter a valid date and time');
  }
  
  // Check if date is not too far in the past or future
  const now = new Date();
  const minDate = new Date(now.getFullYear() - 10, 0, 1); // 10 years ago
  const maxDate = new Date(now.getFullYear() + 10, 11, 31); // 10 years from now
  
  if (date < minDate || date > maxDate) {
    errors.push('Date must be within the last 10 years and next 10 years');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate file upload
 * @param {File} file - File to validate
 * @returns {object} - {isValid: boolean, errors: string[]}
 */
export const validateFileUpload = (file) => {
  const errors = [];
  
  if (!file) {
    errors.push('No file selected');
    return { isValid: false, errors };
  }
  
  // Check file type
  if (!WHITELIST_PATTERNS.imageFile.test(file.name)) {
    errors.push('Only image files are allowed (jpg, jpeg, png, gif, bmp, webp)');
  }
  
  // Check file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    errors.push('File size must be less than 5MB');
  }
  
  // Check file name for dangerous patterns
  const nameValidation = validateInput(file.name);
  if (!nameValidation.isValid) {
    errors.push(...nameValidation.errors);
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Sanitize input by removing dangerous characters
 * @param {string} input - Input to sanitize
 * @returns {string} - Sanitized input
 */
export const sanitizeInput = (input) => {
  if (!input || typeof input !== 'string') {
    return '';
  }
  
  // Remove control characters
  let sanitized = input.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '');
  
  // Remove dangerous patterns
  for (const pattern of DANGEROUS_PATTERNS) {
    sanitized = sanitized.replace(pattern, '');
  }
  
  // Trim whitespace
  sanitized = sanitized.trim();
  
  return sanitized;
};

/**
 * Validate form data comprehensively
 * @param {object} formData - Form data to validate
 * @param {string} formType - Type of form ('login', 'register', 'tariff', 'profile')
 * @returns {object} - {isValid: boolean, errors: object}
 */
export const validateForm = (formData, formType) => {
  const errors = {};
  let isValid = true;
  
  switch (formType) {
    case 'login':
      if (formData.username) {
        const usernameValidation = validateUsername(formData.username);
        if (!usernameValidation.isValid) {
          errors.username = usernameValidation.errors;
          isValid = false;
        }
      }
      if (formData.password) {
        const passwordValidation = validatePassword(formData.password);
        if (!passwordValidation.isValid) {
          errors.password = passwordValidation.errors;
          isValid = false;
        }
      }
      break;
      
    case 'register':
      if (formData.username) {
        const usernameValidation = validateUsername(formData.username);
        if (!usernameValidation.isValid) {
          errors.username = usernameValidation.errors;
          isValid = false;
        }
      }
      if (formData.email) {
        const emailValidation = validateEmail(formData.email);
        if (!emailValidation.isValid) {
          errors.email = emailValidation.errors;
          isValid = false;
        }
      }
      if (formData.password) {
        const passwordValidation = validatePassword(formData.password);
        if (!passwordValidation.isValid) {
          errors.password = passwordValidation.errors;
          isValid = false;
        }
      }
      if (formData.confirmPassword && formData.password !== formData.confirmPassword) {
        errors.confirmPassword = ['Passwords do not match'];
        isValid = false;
      }
      break;
      
    case 'tariff':
      if (formData.fromCountry) {
        const fromCountryValidation = validateCountryCode(formData.fromCountry);
        if (!fromCountryValidation.isValid) {
          errors.fromCountry = fromCountryValidation.errors;
          isValid = false;
        }
      }
      if (formData.toCountry) {
        const toCountryValidation = validateCountryCode(formData.toCountry);
        if (!toCountryValidation.isValid) {
          errors.toCountry = toCountryValidation.errors;
          isValid = false;
        }
      }
      if (formData.product) {
        const productValidation = validateProduct(formData.product);
        if (!productValidation.isValid) {
          errors.product = productValidation.errors;
          isValid = false;
        }
      }
      if (formData.quantity) {
        const quantityValidation = validateQuantity(formData.quantity);
        if (!quantityValidation.isValid) {
          errors.quantity = quantityValidation.errors;
          isValid = false;
        }
      }
      if (formData.calculationFrom) {
        const fromDateValidation = validateDateTime(formData.calculationFrom);
        if (!fromDateValidation.isValid) {
          errors.calculationFrom = fromDateValidation.errors;
          isValid = false;
        }
      }
      if (formData.calculationTo) {
        const toDateValidation = validateDateTime(formData.calculationTo);
        if (!toDateValidation.isValid) {
          errors.calculationTo = toDateValidation.errors;
          isValid = false;
        }
      }
      break;
      
    case 'profile':
      if (formData.file) {
        const fileValidation = validateFileUpload(formData.file);
        if (!fileValidation.isValid) {
          errors.file = fileValidation.errors;
          isValid = false;
        }
      }
      break;
      
    default:
      errors.general = ['Unknown form type'];
      isValid = false;
  }
  
  return { isValid, errors };
};
