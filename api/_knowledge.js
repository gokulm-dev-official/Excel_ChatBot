const COLLEGE_RECORDS = [
  {
    id: "C1",
    category: "Contact Information",
    department: "Administration",
    subcategory: "College Address",
    question: "What is the college address?",
    answer: "Excel Engineering College (Autonomous), NH-544, Salem Main Road, Sankari West, Pallakkapalayam, Komarapalayam, Namakkal District, Tamil Nadu - 637303, India.",
    keywords: ["address", "location", "where", "reach", "campus", "place", "directions", "map"]
  },
  {
    id: "C2",
    category: "Contact Information",
    department: "Admission Office",
    subcategory: "Phone Number",
    question: "What is the college phone number?",
    answer: "+91 98427 13789 / +91 99655 23789",
    keywords: ["phone", "call", "mobile", "number", "contact", "telephone", "helpline"]
  },
  {
    id: "C3",
    category: "Contact Information",
    department: "Admission Office",
    subcategory: "Email Address",
    question: "What is the official email address?",
    answer: "admission@excelcolleges.com / info@excelengg.com",
    keywords: ["email", "mail", "mail id", "gmail", "inquiry"]
  },
  {
    id: "C4",
    category: "Contact Information",
    department: "General",
    subcategory: "Official Website",
    question: "What is the official website?",
    answer: "https://excelinstitutions.com",
    keywords: ["website", "site", "web", "url", "link", "portal"]
  },
  {
    id: "C5",
    category: "Contact Information",
    department: "Administrative Office",
    subcategory: "Office Timings",
    question: "What are the office timings?",
    answer: "Monday to Saturday: 9:00 AM to 5:00 PM (Closed on Sundays and Public Holidays)",
    keywords: ["timings", "hours", "timing", "working hours", "open", "time"]
  },
  {
    id: "CR1",
    category: "Courses",
    department: "Undergraduate",
    subcategory: "Available Courses",
    question: "What are the undergraduate courses available?",
    answer: "Excel Engineering College offers B.E. in CSE, ECE, EEE, Mechanical, Civil, Biomedical, Aeronautical, Agriculture, and B.Tech. in AI & DS, Information Technology, Food Technology, and Computer Science & Business Systems.",
    keywords: ["courses", "programmes", "ug", "undergraduate", "b.e", "b.tech", "branches", "degree", "be", "btech"]
  },
  {
    id: "CR2",
    category: "Courses",
    department: "Postgraduate",
    subcategory: "Available Courses",
    question: "What postgraduate courses are offered?",
    answer: "Postgraduate programs include M.E. in Structural Engineering, Embedded Systems, Computer Science, Aeronautical, as well as MBA (Master of Business Administration) and MCA (Master of Computer Applications).",
    keywords: ["pg", "postgraduate", "m.e", "mba", "mca", "me", "master"]
  },
  {
    id: "CR3",
    category: "Courses",
    department: "General",
    subcategory: "Course Duration",
    question: "What is the duration of engineering courses?",
    answer: "B.E. and B.Tech programs have a duration of 4 years (8 semesters). Lateral entry duration is 3 years (6 semesters). M.E. and MBA/MCA programs have a duration of 2 years (4 semesters).",
    keywords: ["duration", "years", "semesters", "how long", "period"]
  },
  {
    id: "CR4",
    category: "Courses",
    department: "Undergraduate",
    subcategory: "Course Eligibility",
    question: "What is the eligibility for B.E. / B.Tech courses?",
    answer: "Candidates must have passed 10+2 / HSC Examination with Physics, Chemistry, and Mathematics as mandatory subjects with minimum qualifying marks as prescribed by Anna University / DOTE norms.",
    keywords: ["eligibility", "qualification", "criteria", "requirements", "marks", "pass"]
  },
  {
    id: "A1",
    category: "Admissions",
    department: "Admission Office",
    subcategory: "Admission Eligibility",
    question: "What is admission eligibility criteria?",
    answer: "Admission eligibility criteria: For B.E./B.Tech programs, candidates must have passed 10+2 (PCM) with minimum 45% for general category and 40% for reserved categories. For Lateral Entry, a Diploma in Engineering or B.Sc. with Mathematics is required.",
    keywords: ["admission eligibility", "join criteria", "cutoff", "percentage", "hsc", "12th"]
  },
  {
    id: "A2",
    category: "Admissions",
    department: "Admission Office",
    subcategory: "Application Procedure",
    question: "How do I apply for admission?",
    answer: "Candidates can apply online through the official Excel Institutions admission portal (excelinstitutions.com) or visit the Admission Office directly on campus at Komarapalayam, Tamil Nadu.",
    keywords: ["apply", "how to apply", "procedure", "application", "form", "register", "join"]
  },
  {
    id: "A3",
    category: "Admissions",
    department: "Admission Office",
    subcategory: "Required Documents",
    question: "What documents are required for admission?",
    answer: "Required documents include: 10th & 12th Mark sheets, Transfer Certificate (TC), Community Certificate, Migration Certificate (for other state students), Conduct Certificate, and Passport size photographs.",
    keywords: ["documents", "certificate", "certificates", "tc", "mark sheet", "photos"]
  },
  {
    id: "A4",
    category: "Admissions",
    department: "Admission Office",
    subcategory: "Admission Fees",
    question: "What is the admission fee structure?",
    answer: "Tuition and hostel fees are structured as per government norms and institutional scholarships. Contact the Admission Cell at +91 98427 13789 for current academic year fee details and merit scholarship eligibility.",
    keywords: ["fees", "fee", "tuition", "cost", "charges", "scholarship", "scholarships", "fee structure"]
  },
  {
    id: "A5",
    category: "Admissions",
    department: "Admission Office",
    subcategory: "Important Admission Dates",
    question: "What are the important admission dates?",
    answer: "Admissions for the upcoming academic year are currently ongoing for Management and TNEA counselling quotas. Contact the admission office for quota deadlines.",
    keywords: ["dates", "deadline", "last date", "schedule", "when", "ongoing", "tnea"]
  },
  {
    id: "D1",
    category: "Departments",
    department: "Computer Science and Engineering",
    subcategory: "Overview",
    question: "Tell me about the Computer Science and Engineering department",
    answer: "The Department of Computer Science & Engineering (CSE) offers modern laboratories, high-speed computing facilities, industry-aligned curriculum in Cloud, AI, and Full Stack development with excellent placement records.",
    keywords: ["cse", "computer science", "coding", "software", "computing", "it"]
  },
  {
    id: "D2",
    category: "Departments",
    department: "Artificial Intelligence & Data Science",
    subcategory: "Overview",
    question: "Tell me about AI & DS department",
    answer: "The Department of Artificial Intelligence and Data Science prepares students in Machine Learning, Deep Learning, Big Data Analytics, and Natural Language Processing with cutting-edge AI computing labs.",
    keywords: ["ai", "data science", "aids", "ai&ds", "machine learning", "ml"]
  },
  {
    id: "D3",
    category: "Departments",
    department: "Electronics and Communication Engineering",
    subcategory: "Overview",
    question: "Tell me about ECE department",
    answer: "The ECE Department features advanced laboratories in VLSI Design, Embedded Systems, IoT, Microwave and Optical Communication, and Robotics.",
    keywords: ["ece", "electronics", "communication", "vlsi", "embedded", "iot"]
  },
  {
    id: "D4",
    category: "Departments",
    department: "Mechanical Engineering",
    subcategory: "Overview",
    question: "Tell me about Mechanical Engineering",
    answer: "The Department of Mechanical Engineering is equipped with modern CAD/CAM software, CNC machining centres, Mechatronics and Thermal Engineering laboratories, and strong industry partnerships.",
    keywords: ["mechanical", "mech", "cad", "cam", "cnc", "automobile"]
  },
  {
    id: "F1",
    category: "Facilities",
    department: "Hostel",
    subcategory: "Hostel Facilities",
    question: "What hostel facilities are available?",
    answer: "Separate secure hostel facilities are provided for boys and girls with 24/7 security, Wi-Fi connectivity, RO purified drinking water, hygienic mess serving South and North Indian cuisines, gym, and recreational halls.",
    keywords: ["hostel", "accommodation", "stay", "mess", "food", "rooms", "room", "boarding"]
  },
  {
    id: "F2",
    category: "Facilities",
    department: "Library",
    subcategory: "Central Library",
    question: "What are the central library facilities?",
    answer: "The Central Library houses over 50,000 volumes of books, international and national journals, digital e-library with IEEE/DELNET access, multimedia computers, and spacious air-conditioned reading halls.",
    keywords: ["library", "books", "journals", "reading", "delnet", "ieee"]
  },
  {
    id: "F3",
    category: "Facilities",
    department: "Transportation",
    subcategory: "Bus Routes",
    question: "What transportation facilities are provided?",
    answer: "Excel Institutions operates a fleet of over 100 college buses covering Salem, Erode, Tiruchengode, Sankari, Bhavani, Namakkal, and surrounding districts for safe daily commuting of students and staff.",
    keywords: ["bus", "transport", "transportation", "routes", "vehicle", "travel", "commute"]
  },
  {
    id: "F4",
    category: "Facilities",
    department: "Laboratories",
    subcategory: "Campus Labs",
    question: "What laboratory facilities are available?",
    answer: "State-of-the-art laboratories for each department including AI research lab, IoT Centre of Excellence, Advanced Manufacturing Lab, Robotics Lab, and Language Communication Labs.",
    keywords: ["lab", "labs", "laboratory", "laboratories", "equipment", "practical"]
  },
  {
    id: "F5",
    category: "Facilities",
    department: "Sports",
    subcategory: "Sports & Fitness",
    question: "What sports facilities are available?",
    answer: "Expansive sports grounds for cricket, football, basketball courts, volleyball, indoor badminton courts, and fully equipped modern fitness centres.",
    keywords: ["sports", "games", "ground", "fitness", "gym", "cricket", "football", "basketball"]
  }
];

module.exports = { COLLEGE_RECORDS };
