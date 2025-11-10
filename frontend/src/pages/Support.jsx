import { Link, useNavigate } from "react-router-dom";

const accountFaqs = [
  {
    question: "How do I create a TariffKey account?",
    answer:
      "Head to the Register page and fill in your company email plus a secure password. Once submitted, you'll receive a confirmation email—open it to activate your workspace before logging in.",
  },
  {
    question: "Can I switch between multiple accounts?",
    answer:
      "Yes. Log out, then sign back in with the other credential set. Multi-workspace switching is on the roadmap, but for now each login session belongs to a single workspace.",
  },
];

const functionFaqs = [
  {
    question: "What does the Function/Tariff page do?",
    answer:
      "The Tariff Calculator lets you pick an origin, destination and HS product, apply optional fees, and instantly view duties plus suggested landed costs. Every run is stored in your session history.",
  },
  {
    question: "Where can I test scenarios before booking shipments?",
    answer:
      "Use the Simulation space to compare multiple countries side-by-side, adjust price assumptions, and visualise profitability under different tariff regimes.",
  },
  {
    question: "How do I review past calculations or trends?",
    answer:
      "The History view combines your saved quotes with WITS rate history, so you can export prior runs and see how a code's percentages have moved over time.",
  },
];

export default function Support() {
  const navigate = useNavigate();

  return (
    <div className="container">
      <div className="page support-page">
        <div className="support-header">
          <div>
            <p className="hero-eyebrow">Need a hand?</p>
            <h1>Support & Quick Q&A</h1>
            <p>Everything you need to spin up an account and make sense of the core TariffKey functions.</p>
          </div>
          <div className="support-header-actions">
            <button className="pill-button" onClick={() => navigate(-1)}>
              ← Back
            </button>
            <Link className="pill-button" to="/register">
              Create an account
            </Link>
          </div>
        </div>

        <div className="support-grid">
          <section className="support-card">
            <h2>Account creation essentials</h2>
            {accountFaqs.map((item) => (
              <article className="support-faq" key={item.question}>
                <h3>{item.question}</h3>
                <p>{item.answer}</p>
              </article>
            ))}
          </section>

          <section className="support-card">
            <h2>Function page walkthrough</h2>
            {functionFaqs.map((item) => (
              <article className="support-faq" key={item.question}>
                <h3>{item.question}</h3>
                <p>{item.answer}</p>
              </article>
            ))}
          </section>
        </div>

        <section className="support-card">
          <h2>Jump into Tariff Calculations!</h2>
          <div className="quick-actions">
            <Link className="pill-button" to="/tariffs">
              Open Tariff Calculator
            </Link>
            <Link className="pill-button" to="/simulation">
              Explore Simulation
            </Link>
            <Link className="pill-button" to="/history">
              View History & Trends
            </Link>
          </div>
        </section>

        <section className="support-card">
          <h2>Still stuck?</h2>
          <p>
            Drop us a note at <a href="mailto:support@tariffkey.app">support@tariffkey.app</a> or ping the chatbot with the keyword
            <strong> "help"</strong> for guided prompts.
          </p>
        </section>
      </div>
    </div>
  );
}
