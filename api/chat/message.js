const { COLLEGE_RECORDS } = require('../_knowledge');

module.exports = async (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const { message = '', category: userCategory, context = {} } = req.body || {};
  const query = message.trim().toLowerCase();

  // 1. Greetings
  if (/^(hi|hello|hey|vanakkam|namaste|good\s*(morning|afternoon|evening))/i.test(query)) {
    return res.status(200).json({
      answer: "Hello! Welcome to Excel Engineering College (Autonomous). I'm your college assistant. How can I help you today? Ask me about courses, admissions, departments, facilities, or contact information.",
      category: "General",
      confidence: 1.0,
      followUpOptions: [
        { label: "Courses", category: "Courses", prompt: "What courses are available?" },
        { label: "Admissions", category: "Admissions", prompt: "What is the admission procedure?" },
        { label: "Departments", category: "Departments", prompt: "Tell me about the departments" },
        { label: "Contact Us", category: "Contact Information", prompt: "How to contact the college?" }
      ]
    });
  }

  // 2. Thank you
  if (/^(thanks|thank\s*you|nandri|great|super|helpful)/i.test(query)) {
    return res.status(200).json({
      answer: "You're very welcome! If you have any more questions about Excel Engineering College, feel free to ask. I'm here to help!",
      category: "General",
      confidence: 1.0,
      followUpOptions: []
    });
  }

  // 3. Admission Form Intent
  if (/\b(apply|admission form|register|enquiry form|enquire|seat|admission process|join excel)\b/i.test(query) && !/fee|dates|document|eligib/i.test(query)) {
    return res.status(200).json({
      answer: "I'm opening our step-by-step Admission Enquiry Assistant for you. Let's get started!",
      category: "Admissions",
      confidence: 0.95,
      startAdmissionForm: true,
      followUpOptions: []
    });
  }

  // 4. Record matching
  const words = query.replace(/[^a-z0-9\s]/g, ' ').split(/\s+/).filter(w => w.length > 2);
  let bestRecord = null;
  let bestScore = 0;

  for (const record of COLLEGE_RECORDS) {
    let score = 0;
    // Category match
    if (userCategory && userCategory.toLowerCase() === record.category.toLowerCase()) {
      score += 0.25;
    }
    // Subcategory in query
    if (query.includes(record.subcategory.toLowerCase())) {
      score += 0.40;
    }
    // Department in query
    if (query.includes(record.department.toLowerCase())) {
      score += 0.30;
    }
    // Keywords hit
    for (const kw of record.keywords) {
      if (query.includes(kw.toLowerCase())) {
        score += 0.35;
      }
    }
    // Word overlap in question
    const qWords = record.question.toLowerCase().split(/\s+/);
    for (const w of words) {
      if (qWords.includes(w)) score += 0.15;
    }

    if (score > bestScore) {
      bestScore = score;
      bestRecord = record;
    }
  }

  if (bestRecord && bestScore >= 0.35) {
    const confidence = Math.min(1.0, Math.round((bestScore / 1.2) * 100) / 100);
    return res.status(200).json({
      answer: `📌 **${bestRecord.subcategory}**: ${bestRecord.answer}`,
      category: bestRecord.category,
      confidence: confidence,
      matchedRecordId: bestRecord.id,
      followUpOptions: [
        { label: "Admission Enquiry", category: "Admissions", prompt: "I want to apply for admission" },
        { label: "Contact Info", category: "Contact Information", prompt: "What is the college phone number?" },
        { label: "Courses", category: "Courses", prompt: "What courses are available?" }
      ]
    });
  }

  // 5. Fallback
  return res.status(200).json({
    answer: "Sorry, I couldn't find verified information about this in our college records. Please contact the college office for assistance.",
    category: "General",
    confidence: 0.0,
    followUpOptions: [
      { label: "Courses", category: "Courses", prompt: "What courses are available?" },
      { label: "Admissions", category: "Admissions", prompt: "What is the admission procedure?" },
      { label: "Contact Information", category: "Contact Information", prompt: "What is the contact information?" }
    ]
  });
};
