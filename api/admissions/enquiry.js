module.exports = async (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const enquiry = req.body || {};
  if (!enquiry.fullName || enquiry.fullName.trim().length < 2) {
    return res.status(400).json({ success: false, error: 'Please provide your full name (at least 2 letters).' });
  }
  if (!enquiry.mobile || !/^[6-9]\d{9}$/.test(enquiry.mobile.trim())) {
    return res.status(400).json({ success: false, error: 'Please provide a valid 10-digit Indian mobile number.' });
  }

  let isPartial = false;
  if (!enquiry.programme1 || !enquiry.programme1.trim()) {
    enquiry.programme1 = 'General Admission Enquiry / Counselling Pending';
    isPartial = true;
  }
  enquiry.district = enquiry.district || 'Not specified';
  enquiry.board = enquiry.board || 'Not specified';
  enquiry.stream = enquiry.stream || 'Not specified';
  enquiry.hostelRequired = enquiry.hostelRequired || 'Not specified';
  enquiry.transportRequired = enquiry.transportRequired || 'Not specified';
  enquiry.contactTime = enquiry.contactTime || 'Anytime';
  enquiry.admissionType = enquiry.admissionType || 'Regular';

  let calculatedCutoff = null;
  if (enquiry.maths != null && enquiry.physics != null && enquiry.chemistry != null) {
    const m = Number(enquiry.maths);
    const p = Number(enquiry.physics);
    const c = Number(enquiry.chemistry);
    if (!isNaN(m) && !isNaN(p) && !isNaN(c)) {
      calculatedCutoff = Math.round((m + p / 2 + c / 2) * 100) / 100;
    }
  }

  const enquiryId = 'EXCEL-' + (Date.now() % 10000000);
  const nowStr = new Date().toISOString().replace('T', ' ').substring(0, 19);

  const appsScriptUrl = process.env.GOOGLE_APPS_SCRIPT_URL;
  const appsScriptToken = process.env.GOOGLE_APPS_SCRIPT_TOKEN || 'excel-admission-token-2026';
  const enquirySheetId = process.env.GOOGLE_ENQUIRY_SHEET_ID || '1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA';

  if (appsScriptUrl) {
    try {
      const record = {
        EnquiryID: enquiryId,
        SubmittedAt: nowStr,
        FullName: enquiry.fullName,
        Mobile: enquiry.mobile,
        Email: enquiry.email || '',
        District: enquiry.district,
        Board: enquiry.board,
        Stream: enquiry.stream,
        TwelfthPercentage: enquiry.twelfthPercentage || '',
        Maths: enquiry.maths || '',
        Physics: enquiry.physics || '',
        Chemistry: enquiry.chemistry || '',
        Cutoff: calculatedCutoff != null ? calculatedCutoff : '',
        CutoffSource: calculatedCutoff != null ? 'Calculated' : 'N/A',
        Programme1: enquiry.programme1,
        Programme2: enquiry.programme2 || '',
        AdmissionType: enquiry.admissionType,
        HostelRequired: enquiry.hostelRequired,
        TransportRequired: enquiry.transportRequired,
        PreferredContactTime: enquiry.contactTime,
        Message: enquiry.message || '',
        ConsentGiven: enquiry.consentGiven != null ? enquiry.consentGiven : false,
        Source: 'AI Chatbot',
        Status: 'New',
        DuplicateKey: ''
      };

      await fetch(appsScriptUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action: 'submitEnquiry',
          token: appsScriptToken,
          sheetId: enquirySheetId,
          record: record
        })
      });
    } catch (e) {
      console.warn('Failed to forward to Google Apps Script:', e.message);
    }
  }

  return res.status(200).json({
    success: true,
    enquiryId: enquiryId,
    isPartial: isPartial,
    calculatedCutoff: calculatedCutoff,
    message: 'Your admission enquiry has been submitted successfully! Our admissions counsellor will contact you at your preferred time.'
  });
};
