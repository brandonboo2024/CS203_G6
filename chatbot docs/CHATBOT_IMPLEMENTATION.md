# Chatbot Implementation Guide

## Overview
This is a **local chatbot implementation** that runs entirely in the browser with no backend API required. The chatbot uses keyword recognition to guide users to different features of the TariffKey application.

## Features

### 1. Keyword Recognition
The chatbot recognizes the following categories and keywords:

#### Calculator/Tariff Calculation
**Keywords**: tariff, calculate, calculator, cost, price, fee, duty, tax, import, export, customs, charge, how much, calculate cost, tariff rate, pricing

**Guides user to**: Tariff Calculator page (`/tariffs`)

**What it does**: Helps users calculate import/export tariff costs with detailed breakdowns

#### Simulation
**Keywords**: simulation, simulate, compare, comparison, profitability, profit, scenario, forecast, predict, analyze, which is better, best option, compare countries

**Guides user to**: Simulation page (`/simulation`)

**What it does**: Allows users to run profitability simulations and compare tariff rates

#### Past Calculations (History)
**Keywords**: past calculation, previous calculation, my calculation, calculation history, saved calculation, old calculation, what did i calculate, show my calculations

**Guides user to**: History page (`/history`)

**What it does**: Shows all previous tariff calculations with export options

#### Tariff Rate History (Graph)
**Keywords**: tariff history, rate history, historical rates, graph, chart, trend, over time, price history, rate changes, tariff trend, historical data, show graph

**Guides user to**: History page (`/history`)

**What it does**: Displays tariff rate trends over time in visual graphs

#### Profile Management
**Keywords**: profile, edit profile, account, settings, personal info, update profile, change password, my account, user settings, avatar, profile picture

**Guides user to**: Profile page (`/profile`)

**What it does**: Allows users to manage their account and personal information

### 2. Special Commands

#### Greetings
**Keywords**: hello, hi, hey, greetings, good morning, good afternoon, good evening

**Response**: Friendly greeting with overview of available features

#### Help
**Keywords**: help, what can you do, features, commands, options

**Response**: Comprehensive help message with all available features and example queries

## File Structure

```
frontend/
├── src/
│   ├── components/
│   │   └── Chatbot.jsx          # Main chatbot UI component
│   ├── services/
│   │   └── chatbotService.js    # Keyword recognition logic
│   ├── styles/
│   │   └── Chatbot.css          # Chatbot styling
│   └── App.jsx                  # Chatbot integration
```

## How It Works

### 1. ChatbotService (chatbotService.js)
- **Pure client-side logic** - no backend API calls
- Keyword matching algorithm that scores messages based on keyword presence
- Returns structured responses with:
  - Message text
  - Action buttons (navigate to pages or show explanations)
  - Response type

### 2. Chatbot Component (Chatbot.jsx)
- Floating action button (bottom-right corner)
- Chat window with message history
- Quick suggestion buttons for first-time users
- Action buttons for navigation
- Typing indicator for natural interaction
- Clear chat functionality

### 3. User Flow
1. User clicks chatbot toggle button
2. Chatbot window opens with greeting
3. User types a message (e.g., "calculate tariff")
4. ChatbotService processes the message and identifies intent
5. Bot responds with helpful information and action buttons
6. User clicks action button to navigate to the appropriate page

## Integration

The chatbot is integrated into `App.jsx` and only shows when user is logged in:

```jsx
{isLoggedIn && <Chatbot />}
```

## Navigation

The chatbot uses React Router's `useNavigate` hook to navigate between pages:

- `/tariffs` - Tariff Calculator
- `/simulation` - Simulation Tools
- `/history` - Calculation History & Graphs
- `/profile` - User Profile

When a user clicks a navigation action button, the chatbot:
1. Navigates to the target page
2. Automatically closes the chat window

## Customization

### Adding New Intents
To add new keyword recognition patterns, edit `chatbotService.js`:

```javascript
this.intents = {
  // ... existing intents
  new_feature: {
    keywords: ['keyword1', 'keyword2', 'phrase with spaces'],
    response: {
      type: 'new_feature',
      message: "Your response message here",
      actions: [
        { label: 'Button Text', path: '/route-path' }
      ]
    }
  }
};
```

### Modifying Styling
All chatbot styles are in `Chatbot.css`:
- Colors: Modify the gradient in `.chatbot-toggle` and `.chatbot-header`
- Size: Adjust width/height in `.chatbot-window`
- Position: Change bottom/right values in `.chatbot-toggle` and `.chatbot-window`

### Responsive Design
The chatbot is fully responsive:
- **Desktop**: 400px wide window, bottom-right corner
- **Tablet**: Full width minus margins
- **Mobile**: Full-screen overlay

## Example Conversations

### Example 1: Calculate Tariff
```
User: "How much does it cost to import electronics?"
Bot: [Explains calculator] [Go to Calculator button]
User: [Clicks button]
→ Navigates to /tariffs
```

### Example 2: View History
```
User: "show my past calculations"
Bot: [Explains history feature] [View History button]
User: [Clicks button]
→ Navigates to /history
```

### Example 3: Run Simulation
```
User: "I want to compare costs"
Bot: [Explains simulation tools] [Go to Simulation button]
User: [Clicks button]
→ Navigates to /simulation
```

## Features

### Current Features
- Keyword-based intent recognition
- Multi-turn conversations with context
- Action buttons for navigation
- Quick suggestions for new users
- Typing indicator
- Chat history (session-based)
- Clear chat functionality
- Fully responsive design
- Dark mode support (automatic)

### Limitations
- No backend integration (purely client-side)
- No persistent conversation history across sessions
- No AI/LLM integration (rule-based matching only)
- Limited to predefined keywords and responses

## Testing

### Manual Testing Checklist
1. **Login Check**: Chatbot only appears when logged in
2. **Toggle**: Click button to open/close chatbot
3. **Greetings**: Type "hello" → should show greeting
4. **Help**: Type "help" → should show all features
5. **Calculator**: Type "calculate tariff" → should offer navigation
6. **Simulation**: Type "compare costs" → should offer navigation
7. **History**: Type "my calculations" → should offer navigation
8. **Graph**: Type "tariff trends" → should offer navigation
9. **Profile**: Type "edit profile" → should offer navigation
10. **Navigation**: Click action buttons → should navigate and close chat
11. **Clear**: Click clear button → should reset conversation
12. **Responsive**: Test on mobile/tablet sizes

### Test Keywords
Try these sample queries:
- "calculate tariff"
- "how much does shipping cost"
- "run a simulation"
- "compare different countries"
- "show my past calculations"
- "tariff rate history graph"
- "edit my profile"
- "help"
- "hello"

## Performance

- **Bundle Impact**: ~15KB (service + component + styles)
- **Runtime**: No API calls, instant responses
- **Memory**: Minimal (only stores current session messages)

## Browser Compatibility

- Chrome/Edge: Full support
- Firefox: Full support
- Safari: Full support
- Mobile browsers: Full support

## Troubleshooting

### Chatbot not appearing
- Check if user is logged in
- Verify `Chatbot` component is imported in `App.jsx`
- Check browser console for errors

### Navigation not working
- Verify routes are defined in `main.jsx`
- Check `useNavigate` is working
- Ensure paths match exactly (e.g., `/tariffs` not `/tariff-calc`)

### Keywords not matching
- Check spelling in `chatbotService.js`
- Add more keyword variations
- Test with exact keyword phrases

### Styling issues
- Import `Chatbot.css` in the component
- Check CSS file path is correct
- Clear browser cache

## Future Enhancements (Optional)

Possible improvements:
- Backend integration for conversation persistence
- AI/LLM integration for natural language understanding
- Voice input support
- Multi-language support
- Analytics tracking for common queries
- Export chat transcripts
- Rich media responses (images, videos)
- Contextual help based on current page

## Developer Notes

### Why Local Implementation?
- Faster response times (no API latency)
- No backend dependencies
- Lower costs (no AI API fees)
- Privacy-friendly (no data sent to servers)
- Simpler deployment

### Architecture Decision
The chatbot uses a simple keyword-matching algorithm instead of NLP/AI because:
1. Limited scope (only 5 main features)
2. Predictable user queries
3. No need for complex understanding
4. Better performance and reliability
5. Easier to maintain and debug

If the application grows and requires more sophisticated understanding, consider integrating:
- OpenAI GPT API
- Anthropic Claude API
- Google Dialogflow
- Rasa (self-hosted)

---

**Implementation Date**: November 2025
**Version**: 1.0.0
**Status**: Production Ready
