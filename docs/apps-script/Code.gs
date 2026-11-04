/**
 * Excel Engineering College (Autonomous) - Admission Enquiries Apps Script
 *
 * HOW TO DEPLOY:
 * 1. Open your Google Sheet: https://docs.google.com/spreadsheets/d/1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA/edit
 * 2. Click "Extensions" > "Apps Script" in the top menu.
 * 3. Delete any code in the editor and paste this entire file.
 * 4. Click "Deploy" (blue button at top right) > "New deployment".
 * 5. Select type: "Web app" (click the gear icon next to "Select type").
 * 6. Set Description: "Excel Chatbot Admission Webhook"
 * 7. Set "Execute as": "Me" (your email).
 * 8. Set "Who has access": "Anyone" (CRITICAL: must be Anyone so your chatbot backend can send entries).
 * 9. Click "Deploy", authorize access when prompted.
 * 10. Copy the "Web app URL" (looks like https://script.google.com/macros/s/.../exec).
 * 11. Paste that URL into your .env file as:
 *     GOOGLE_APPS_SCRIPT_URL=https://script.google.com/macros/s/.../exec
 *     GOOGLE_APPS_SCRIPT_TOKEN=excel-admission-token-2026
 */

const HEADERS = [
  'EnquiryID',
  'SubmittedAt',
  'FullName',
  'Mobile',
  'Email',
  'District',
  'Board',
  'Stream',
  'TwelfthPercentage',
  'Maths',
  'Physics',
  'Chemistry',
  'Cutoff',
  'CutoffSource',
  'Programme1',
  'Programme2',
  'AdmissionType',
  'HostelRequired',
  'TransportRequired',
  'PreferredContactTime',
  'Message',
  'ConsentGiven',
  'Source',
  'Status',
  'DuplicateKey'
];

function doPost(e) {
  const out = (obj) => ContentService.createTextOutput(JSON.stringify(obj)).setMimeType(ContentService.MimeType.JSON);
  
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return out({ ok: false, error: 'empty_payload' });
    }

    const body = JSON.parse(e.postData.contents);
    const configuredToken = PropertiesService.getScriptProperties().getProperty('TOKEN');
    
    // Validate token if one is set in Script Properties
    if (configuredToken && body.token !== configuredToken) {
      return out({ ok: false, error: 'unauthorized' });
    }

    const lock = LockService.getScriptLock();
    lock.waitLock(10000);

    try {
      const ss = SpreadsheetApp.getActiveSpreadsheet();
      let sh = ss.getSheetByName('AdmissionEnquiries');
      
      // Auto-create tab if it doesn't exist
      if (!sh) {
        sh = ss.insertSheet('AdmissionEnquiries');
      }

      // Add headers if sheet is empty
      if (sh.getLastRow() === 0) {
        sh.appendRow(HEADERS);
        // Format header row: Navy background, White text, Bold
        const headerRange = sh.getRange(1, 1, 1, HEADERS.length);
        headerRange.setBackground('#050E3A');
        headerRange.setFontColor('#FFFFFF');
        headerRange.setFontWeight('bold');
        sh.setFrozenRows(1);
      }

      const record = body.record || body.enquiry || {};

      // Map record fields to headers
      const row = HEADERS.map(h => {
        if (record[h] !== undefined && record[h] !== null) return record[h];
        // Check camelCase fallback
        const camel = h.charAt(0).toLowerCase() + h.slice(1);
        if (record[camel] !== undefined && record[camel] !== null) return record[camel];
        return '';
      });

      sh.appendRow(row);
      return out({ ok: true, enquiryId: record['EnquiryID'] || record['enquiryId'] || '' });

    } finally {
      lock.releaseLock();
    }

  } catch (err) {
    return out({ ok: false, error: 'server_error', details: err.toString() });
  }
}

function doGet(e) {
  return ContentService.createTextOutput(JSON.stringify({
    status: 'online',
    service: 'Excel Engineering College Admission Webhook',
    timestamp: new Date().toISOString()
  })).setMimeType(ContentService.MimeType.JSON);
}
