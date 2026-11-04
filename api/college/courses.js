const { COLLEGE_RECORDS } = require('../_knowledge');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  const courseRecords = COLLEGE_RECORDS.filter(r => r.category === 'Courses');
  return res.status(200).json({
    courses: courseRecords
  });
};
