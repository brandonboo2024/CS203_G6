// Local chatbot service for keyword recognition and user guidance
// No backend API needed - all logic runs in the browser

export class ChatbotService {
  constructor() {
    // Define keyword patterns for each function
    this.intents = {
      calculator: {
        keywords: [
          'tariff', 'calculate', 'calculator', 'cost', 'price', 'fee',
          'duty', 'tax', 'import', 'export', 'customs', 'charge',
          'how much', 'calculate cost', 'tariff rate', 'pricing'
        ],
        response: {
          type: 'calculator',
          message: "I can help you calculate tariffs! Here's how to use the calculator:\n\n1. Select your origin country\n2. Select your destination country\n3. Choose the product category\n4. Enter the quantity\n5. Toggle any additional fees (insurance, handling, etc.)\n6. Click 'Calculate' to see the breakdown\n\nWould you like me to take you to the calculator?",
          actions: [
            { label: 'Go to Calculator', path: '/tariffs' },
            { label: 'Learn More', action: 'explain_calculator' }
          ]
        }
      },
      simulation: {
        keywords: [
          'simulation', 'simulate', 'compare', 'comparison', 'profitability',
          'profit', 'scenario', 'forecast', 'predict', 'analyze',
          'which is better', 'best option', 'compare countries'
        ],
        response: {
          type: 'simulation',
          message: "I can help you run simulations! Our simulation tool offers:\n\n1. **Profitability Simulation**: Compare costs across different scenarios\n2. **Tariff Rate Comparison**: See side-by-side comparisons of different routes\n\nSimulations help you make better business decisions by visualizing the data.\n\nWould you like to start a simulation?",
          actions: [
            { label: 'Go to Simulation', path: '/simulation' },
            { label: 'Explain Simulations', action: 'explain_simulation' }
          ]
        }
      },
      history_calculations: {
        keywords: [
          'past calculation', 'previous calculation', 'my calculation',
          'calculation history', 'saved calculation', 'old calculation',
          'what did i calculate', 'show my calculations'
        ],
        response: {
          type: 'history_calculations',
          message: "You can view your past calculations in the History section!\n\nYour calculation history includes:\n- All previous tariff calculations\n- Detailed breakdowns of each calculation\n- Option to export to CSV\n- Ability to recalculate or modify past entries\n\nWould you like to view your calculation history?",
          actions: [
            { label: 'View History', path: '/history' },
            { label: 'Export History', action: 'export_history' }
          ]
        }
      },
      history_graph: {
        keywords: [
          'tariff history', 'rate history', 'historical rates', 'graph',
          'chart', 'trend', 'over time', 'price history', 'rate changes',
          'tariff trend', 'historical data', 'show graph'
        ],
        response: {
          type: 'history_graph',
          message: "You can view tariff rate history graphs in the History section!\n\nThe graph shows:\n- Tariff rate trends over time\n- Historical changes in rates\n- Visual comparison of different time periods\n\nThis helps you identify patterns and make informed decisions.\n\nWould you like to see the tariff history graph?",
          actions: [
            { label: 'View Graph', path: '/history' },
            { label: 'Explain Graph', action: 'explain_graph' }
          ]
        }
      },
      profile: {
        keywords: [
          'profile', 'edit profile', 'account', 'settings', 'personal info',
          'update profile', 'change password', 'my account', 'user settings',
          'avatar', 'profile picture'
        ],
        response: {
          type: 'profile',
          message: "You can manage your profile in the Profile section!\n\nProfile features include:\n- Update your personal information\n- Change your avatar/profile picture\n- View account details\n- Update preferences\n\nWould you like to go to your profile?",
          actions: [
            { label: 'Go to Profile', path: '/profile' },
            { label: 'View Account Info', action: 'account_info' }
          ]
        }
      }
    };

    // Greeting responses
    this.greetings = [
      'hello', 'hi', 'hey', 'greetings', 'good morning', 'good afternoon',
      'good evening', 'howdy', 'what\'s up', 'sup'
    ];

    // Help keywords
    this.helpKeywords = ['help', 'what can you do', 'features', 'commands', 'options'];
  }

  /**
   * Process user message and return chatbot response
   * @param {string} message - User's message
   * @returns {Object} - Chatbot response with message and actions
   */
  processMessage(message) {
    const normalizedMessage = message.toLowerCase().trim();

    // Check for greetings
    if (this.isGreeting(normalizedMessage)) {
      return this.getGreetingResponse();
    }

    // Check for help request
    if (this.isHelpRequest(normalizedMessage)) {
      return this.getHelpResponse();
    }

    // Check for intent matches
    const intent = this.detectIntent(normalizedMessage);
    if (intent) {
      return intent.response;
    }

    // No match found
    return this.getUnknownResponse();
  }

  /**
   * Detect user intent based on keywords
   * @param {string} message - Normalized message
   * @returns {Object|null} - Matched intent or null
   */
  detectIntent(message) {
    let bestMatch = null;
    let bestScore = 0;

    for (const intent of Object.values(this.intents)) {
      const score = this.calculateMatchScore(message, intent.keywords);
      if (score > bestScore) {
        bestScore = score;
        bestMatch = intent;
      }
    }

    // Return match if score is above threshold
    return bestScore > 0 ? bestMatch : null;
  }

  /**
   * Calculate match score based on keyword presence
   * @param {string} message - User message
   * @param {Array} keywords - Keywords to match
   * @returns {number} - Match score
   */
  calculateMatchScore(message, keywords) {
    let score = 0;
    for (const keyword of keywords) {
      if (message.includes(keyword.toLowerCase())) {
        // Longer keywords get higher scores (more specific)
        score += keyword.split(' ').length;
      }
    }
    return score;
  }

  /**
   * Check if message is a greeting
   * @param {string} message - Normalized message
   * @returns {boolean}
   */
  isGreeting(message) {
    return this.greetings.some(greeting =>
      message === greeting || message.startsWith(greeting + ' ')
    );
  }

  /**
   * Check if message is a help request
   * @param {string} message - Normalized message
   * @returns {boolean}
   */
  isHelpRequest(message) {
    return this.helpKeywords.some(keyword => message.includes(keyword));
  }

  /**
   * Get greeting response
   * @returns {Object}
   */
  getGreetingResponse() {
    return {
      type: 'greeting',
      message: "Hello! I'm your TariffKey assistant. I can help you with:\n\n" +
               "• **Calculator** - Calculate tariff costs\n" +
               "• **Simulation** - Compare scenarios and analyze profitability\n" +
               "• **History** - View past calculations and tariff trends\n" +
               "• **Profile** - Manage your account settings\n\n" +
               "What would you like to do?",
      actions: [
        { label: 'Calculate Tariff', path: '/tariffs' },
        { label: 'Run Simulation', path: '/simulation' },
        { label: 'View History', path: '/history' },
        { label: 'My Profile', path: '/profile' }
      ]
    };
  }

  /**
   * Get help response
   * @returns {Object}
   */
  getHelpResponse() {
    return {
      type: 'help',
      message: "Here's what I can help you with:\n\n" +
               "**1. Tariff Calculator**\n" +
               "   Try: 'calculate tariff', 'how much does it cost', 'tariff calculator'\n\n" +
               "**2. Simulation Tools**\n" +
               "   Try: 'run simulation', 'compare costs', 'profitability analysis'\n\n" +
               "**3. Calculation History**\n" +
               "   Try: 'my past calculations', 'calculation history', 'previous calculations'\n\n" +
               "**4. Tariff Rate History**\n" +
               "   Try: 'tariff history graph', 'rate trends', 'historical rates'\n\n" +
               "**5. Profile Management**\n" +
               "   Try: 'edit profile', 'my account', 'update profile'\n\n" +
               "Just type what you'd like to do!",
      actions: [
        { label: 'Calculate Tariff', path: '/tariffs' },
        { label: 'Run Simulation', path: '/simulation' },
        { label: 'View History', path: '/history' },
        { label: 'My Profile', path: '/profile' }
      ]
    };
  }

  /**
   * Get response for unrecognized input
   * @returns {Object}
   */
  getUnknownResponse() {
    return {
      type: 'unknown',
      message: "I'm not sure I understand. I can help you with:\n\n" +
               "• Calculate tariffs\n" +
               "• Run simulations\n" +
               "• View calculation history\n" +
               "• View tariff rate graphs\n" +
               "• Edit your profile\n\n" +
               "Try asking about one of these features, or type 'help' for more information.",
      actions: [
        { label: 'Show Help', action: 'help' }
      ]
    };
  }

  /**
   * Get detailed explanation for a feature
   * @param {string} feature - Feature to explain
   * @returns {Object}
   */
  getExplanation(feature) {
    const explanations = {
      explain_calculator: {
        type: 'explanation',
        message: "**Tariff Calculator Explained**\n\n" +
                 "The calculator helps you determine the total cost of importing/exporting goods.\n\n" +
                 "**Inputs needed:**\n" +
                 "- Origin Country: Where the goods are coming from\n" +
                 "- Destination Country: Where the goods are going\n" +
                 "- Product Category: Type of goods (electronics, textiles, etc.)\n" +
                 "- Quantity: Amount of goods\n\n" +
                 "**Optional fees:**\n" +
                 "- Insurance\n" +
                 "- Handling fees\n" +
                 "- Additional customs charges\n\n" +
                 "The calculator will show you a complete breakdown of all costs.",
        actions: [
          { label: 'Go to Calculator', path: '/tariffs' }
        ]
      },
      explain_simulation: {
        type: 'explanation',
        message: "**Simulation Tools Explained**\n\n" +
                 "Simulations help you make data-driven decisions by comparing different scenarios.\n\n" +
                 "**Profitability Simulation:**\n" +
                 "- Compare costs across different routes\n" +
                 "- Factor in various fees and charges\n" +
                 "- See which option maximizes profit\n\n" +
                 "**Tariff Rate Comparison:**\n" +
                 "- Side-by-side comparison of tariff rates\n" +
                 "- Visual charts for easy analysis\n" +
                 "- Identify the most cost-effective options\n\n" +
                 "Perfect for planning shipments and optimizing costs!",
        actions: [
          { label: 'Go to Simulation', path: '/simulation' }
        ]
      },
      explain_graph: {
        type: 'explanation',
        message: "**Tariff History Graph Explained**\n\n" +
                 "The graph shows how tariff rates have changed over time.\n\n" +
                 "**What you can see:**\n" +
                 "- Historical tariff rates for specific routes\n" +
                 "- Trends and patterns in rate changes\n" +
                 "- Comparison across different time periods\n\n" +
                 "**Why it's useful:**\n" +
                 "- Identify seasonal patterns\n" +
                 "- Predict future rate changes\n" +
                 "- Plan shipments during low-rate periods\n" +
                 "- Make informed business decisions",
        actions: [
          { label: 'View Graph', path: '/history' }
        ]
      }
    };

    return explanations[feature] || this.getHelpResponse();
  }
}

// Export singleton instance
export const chatbotService = new ChatbotService();
