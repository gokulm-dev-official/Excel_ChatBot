const path = require('path');
const fs = require('fs');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  try {
    const filePath = path.join(process.cwd(), 'src', 'main', 'resources', 'admission-form.json');
    if (fs.existsSync(filePath)) {
      const data = fs.readFileSync(filePath, 'utf8');
      return res.status(200).send(data);
    }
  } catch (e) {
    // Fallback if file read fails
  }

  // Fallback inline config
  return res.status(200).json([
    { "step": 1, "field": "FullName", "question": "May I know your full name?", "control": "text", "required": true, "placeholder": "Enter your full name" },
    { "step": 2, "field": "Mobile", "question": "What is your 10-digit mobile number?", "control": "tel", "required": true, "placeholder": "10-digit mobile number" },
    { "step": 3, "field": "Email", "question": "What is your email address? (Optional)", "control": "email", "required": false, "placeholder": "student@example.com" },
    { "step": 4, "field": "District", "question": "Which district or city are you from?", "control": "text", "required": true, "placeholder": "e.g., Salem, Erode, Namakkal, Coimbatore" },
    { "step": 5, "field": "Board", "question": "Which board did you study 12th under?", "control": "chips", "required": true, "options": ["State Board", "CBSE", "ICSE", "Other"] },
    { "step": 6, "field": "Stream", "question": "Which stream did you choose in 12th?", "control": "chips", "required": true, "options": ["General Maths (PCM)", "Computer Science", "Biology", "Vocational", "Diploma / Polytech"] },
    { "step": 7, "field": "TwelfthPercentage", "question": "What is your overall 12th / Diploma percentage?", "control": "number", "required": false, "placeholder": "e.g., 85.5" },
    { "step": 8, "field": "Maths", "question": "What is your Mathematics mark? (out of 100)", "control": "number", "required": false, "placeholder": "e.g., 90" },
    { "step": 9, "field": "Physics", "question": "What is your Physics mark? (out of 100)", "control": "number", "required": false, "placeholder": "e.g., 85" },
    { "step": 10, "field": "Chemistry", "question": "What is your Chemistry mark? (out of 100)", "control": "number", "required": false, "placeholder": "e.g., 80" },
    { "step": 11, "field": "Programme1", "question": "Which B.E. / B.Tech course is your First Preference?", "control": "select", "required": true, "options": ["B.E. Computer Science and Engineering (CSE)", "B.Tech. Artificial Intelligence & Data Science (AI & DS)", "B.Tech. Information Technology (IT)", "B.E. Electronics and Communication Engineering (ECE)", "B.E. Electrical and Electronics Engineering (EEE)", "B.E. Mechanical Engineering", "B.E. Biomedical Engineering", "B.E. Aeronautical Engineering", "B.E. Civil Engineering", "B.Tech. Food Technology", "B.Tech. Computer Science & Business Systems (CSBS)"] },
    { "step": 12, "field": "Programme2", "question": "Which course is your Second Preference? (Optional)", "control": "select", "required": false, "options": ["None", "B.E. Computer Science and Engineering (CSE)", "B.Tech. Artificial Intelligence & Data Science (AI & DS)", "B.Tech. Information Technology (IT)", "B.E. Electronics and Communication Engineering (ECE)", "B.E. Electrical and Electronics Engineering (EEE)", "B.E. Mechanical Engineering"] },
    { "step": 13, "field": "AdmissionType", "question": "Are you applying through Management Quota or Government (TNEA) Counselling?", "control": "chips", "required": true, "options": ["Government (TNEA) Counselling", "Management Quota", "Lateral Entry (Direct 2nd Year)"] },
    { "step": 14, "field": "HostelRequired", "question": "Do you require campus hostel accommodation?", "control": "chips", "required": true, "options": ["Yes, Hostel Required", "No, Day Scholar"] },
    { "step": 15, "field": "TransportRequired", "question": "Do you need college bus transport facility?", "control": "chips", "required": true, "options": ["Yes, Bus Transport Required", "No, Self Travel"] },
    { "step": 16, "field": "ContactTime", "question": "When is the best time for our admissions team to call you?", "control": "chips", "required": true, "options": ["Morning (9 AM - 12 PM)", "Afternoon (12 PM - 3 PM)", "Evening (3 PM - 6 PM)", "Anytime"] },
    { "step": 17, "field": "Message", "question": "Any specific questions or scholarship requests? (Optional)", "control": "text", "required": false, "placeholder": "Type your question or tap Skip" },
    { "step": 18, "field": "ConsentGiven", "question": "I confirm that the details provided are correct and authorize Excel Engineering College admissions team to contact me.", "control": "consent", "required": true }
  ]);
};
