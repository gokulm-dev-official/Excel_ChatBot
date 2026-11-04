const { COLLEGE_RECORDS } = require('../_knowledge');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  const deptRecords = COLLEGE_RECORDS.filter(r => r.category === 'Departments');
  return res.status(200).json({
    departments: deptRecords
  });
};
