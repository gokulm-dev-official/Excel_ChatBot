const { COLLEGE_RECORDS } = require('../_knowledge');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  return res.status(200).json({
    total: COLLEGE_RECORDS.length,
    records: COLLEGE_RECORDS
  });
};
