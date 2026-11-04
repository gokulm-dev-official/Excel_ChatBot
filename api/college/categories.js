module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  return res.status(200).json({
    categories: ['Admissions', 'Contact Information', 'Courses', 'Departments', 'Facilities'],
    subcategories: {
      'Courses': ['Available Courses', 'Course Duration', 'Course Eligibility'],
      'Admissions': ['Admission Eligibility', 'Application Procedure', 'Required Documents', 'Admission Fees', 'Important Admission Dates'],
      'Departments': ['Overview'],
      'Facilities': ['Hostel Facilities', 'Central Library', 'Bus Routes', 'Campus Labs', 'Sports & Fitness'],
      'Contact Information': ['College Address', 'Phone Number', 'Email Address', 'Official Website', 'Office Timings']
    },
    departments: {
      'all': [
        'Administration', 'Administrative Office', 'Admission Office',
        'Artificial Intelligence & Data Science', 'Computer Science and Engineering',
        'Electronics and Communication Engineering', 'General', 'Hostel',
        'Laboratories', 'Library', 'Mechanical Engineering', 'Postgraduate',
        'Sports', 'Transportation', 'Undergraduate'
      ]
    }
  });
};
