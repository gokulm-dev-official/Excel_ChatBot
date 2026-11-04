/**
 * Excel Engineering College - AI Assistant & Admission Form Engine
 * Reference Replica Edition – Ultra-robust Form Flow & Half-Filled Enquiry Support
 */

(function() {
  'use strict';

  const state = {
    currentCategory: null,
    waiting: false,
    formMode: false,
    formSteps: [],
    formCurrentStep: 0,
    formData: {},
  };

  const CATEGORIES = {
    'Courses': {
      label: 'Courses',
      options: ['Available Courses', 'Course Duration', 'Course Eligibility', 'Course Details', 'PG Programmes']
    },
    'Admissions': {
      label: 'Admissions',
      options: ['📝 Apply / Admission Enquiry', 'Admission Eligibility', 'Application Procedure', 'Required Documents', 'Admission Fees', 'Important Admission Dates']
    },
    'Departments': {
      label: 'Departments',
      options: ['Computer Science and Engineering', 'Artificial Intelligence & Data Science', 'Information Technology', 'Electronics and Communication Engineering', 'Mechanical Engineering']
    },
    'Facilities': {
      label: 'Facilities',
      options: ['Hostel Facilities', 'Central Library', 'Transportation', 'Campus Labs', 'Sports & Fitness']
    },
    'Contact Information': {
      label: 'Contact Information',
      options: ['College Address', 'Phone Number', 'Email Address', 'Official Website', 'Office Timings']
    }
  };

  const POPULAR_DISTRICTS = ['Salem', 'Erode', 'Namakkal', 'Coimbatore', 'Tiruppur', 'Chennai', 'Dharmapuri', 'Karur'];

  let messagesCanvas, chatInput, sendBtn, categoryGrid, formProgressBar, formProgressFill, formStepLabel, cancelFormBtn;

  document.addEventListener('DOMContentLoaded', init);

  async function init() {
    messagesCanvas = document.getElementById('chatbotMessages');
    chatInput = document.getElementById('chatInput');
    sendBtn = document.getElementById('chatSendBtn');
    categoryGrid = document.getElementById('categoryGrid');
    formProgressBar = document.getElementById('formProgressBar');
    formProgressFill = document.getElementById('formProgressFill');
    formStepLabel = document.getElementById('formStepLabel');
    cancelFormBtn = document.getElementById('cancelFormBtn');

    // Set greeting timestamp
    const greetingTime = document.getElementById('greetingTime');
    if (greetingTime) greetingTime.textContent = getTimeString();

    // Input handlers
    if (chatInput && sendBtn) {
      chatInput.addEventListener('input', () => {
        sendBtn.disabled = !chatInput.value.trim();
      });

      chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          if (!sendBtn.disabled) onSend();
        }
      });

      sendBtn.addEventListener('click', onSend);
    }

    // Category Grid Clicks
    if (categoryGrid) {
      categoryGrid.querySelectorAll('.category-grid__card').forEach(card => {
        card.addEventListener('click', () => {
          const cat = card.getAttribute('data-category');
          if (cat) onSelectCategory(cat);
        });
      });
    }

    // Cancel Form
    if (cancelFormBtn) {
      cancelFormBtn.addEventListener('click', handleCancelForm);
    }

    // Check URL parameters for category
    const urlParams = new URLSearchParams(window.location.search);
    const categoryParam = urlParams.get('category');
    if (categoryParam && CATEGORIES[categoryParam]) {
      setTimeout(() => onSelectCategory(categoryParam), 300);
    }
  }

  function getTimeString() {
    const d = new Date();
    let h = d.getHours();
    const m = d.getMinutes().toString().padStart(2, '0');
    const ampm = h >= 12 ? 'PM' : 'AM';
    h = h % 12 || 12;
    return `${h}:${m} ${ampm}`;
  }

  // ==================== CHAT ACTIONS ====================

  async function onSend() {
    const text = chatInput.value.trim();
    if (!text || state.waiting) return;

    chatInput.value = '';
    sendBtn.disabled = true;

    // Hide initial category grid after first interaction
    if (categoryGrid) categoryGrid.style.display = 'none';

    // In form mode, delegate to form handler
    if (state.formMode) {
      handleFormTextInput(text);
      return;
    }

    addUserBubble(text);
    await askAssistant(text);
  }

  async function onSelectCategory(cat) {
    state.currentCategory = cat;
    if (categoryGrid) categoryGrid.style.display = 'none';

    addUserBubble(cat);

    const conf = CATEGORIES[cat];
    if (conf && conf.options) {
      showTyping();
      setTimeout(() => {
        hideTyping();
        addBotBubble(`Here are popular questions regarding **${cat}**. Tap an option or type your own question:`, true);
        renderOptionChips(conf.options);
      }, 350);
    }
  }

  async function askAssistant(text) {
    state.waiting = true;
    showTyping();

    try {
      const response = await API.sendMessage(text, state.currentCategory);
      hideTyping();

      if (response.category) {
        state.currentCategory = response.category;
      }

      // Check if admission form should start
      if (response.startAdmissionForm) {
        addBotBubble(response.answer, true);
        startAdmissionFormFlow();
        return;
      }

      addBotBubble(response.answer, response.verified);

      // Render suggestions or follow-up options
      const chips = [];
      if (response.followUpOptions && response.followUpOptions.length > 0) {
        response.followUpOptions.forEach(opt => chips.push(typeof opt === 'string' ? opt : opt.label));
      } else if (response.suggestions && response.suggestions.length > 0) {
        chips.push(...response.suggestions);
      }

      if (chips.length > 0) {
        setTimeout(() => renderOptionChips(chips), 200);
      }
    } catch (e) {
      hideTyping();
      addBotBubble("Sorry, I couldn't reach the college records service right now. Please try again shortly or contact the college office directly at +91 98427 13789.", false);
    } finally {
      state.waiting = false;
    }
  }

  function addUserBubble(text) {
    const row = document.createElement('div');
    row.className = 'chat-row chat-row--user';
    row.innerHTML = `
      <div class="chat-row__bubble chat-row__bubble--user">
        <p>${escapeHtml(text)}</p>
        <span class="chat-row__time">${getTimeString()}</span>
      </div>
    `;
    messagesCanvas.appendChild(row);
    scrollToBottom();
  }

  function addBotBubble(text, verified = null) {
    const row = document.createElement('div');
    row.className = 'chat-row chat-row--bot';

    let verifiedBadge = '';
    if (verified === true) {
      verifiedBadge = `
        <div class="message-verified-badge">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
          Verified from College Records
        </div>
      `;
    }

    const formatted = formatMarkdown(text);

    row.innerHTML = `
      <div class="chat-row__avatar">
        <img src="assets/robot-mascot.svg" alt="Mascot">
      </div>
      <div class="chat-row__bubble chat-row__bubble--bot">
        ${verifiedBadge}
        <div>${formatted}</div>
        <span class="chat-row__time">${getTimeString()}</span>
      </div>
    `;
    messagesCanvas.appendChild(row);
    scrollToBottom();
  }

  function renderOptionChips(options) {
    const container = document.createElement('div');
    container.className = 'chips-container';

    const label = document.createElement('div');
    label.className = 'chips-label';
    label.textContent = 'Need more details?';
    container.appendChild(label);

    options.forEach(opt => {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'chip-btn';
      if (opt.includes('Apply') || opt.includes('Enquiry')) {
        btn.classList.add('chip-btn--primary');
      }
      btn.textContent = opt;

      btn.addEventListener('click', () => {
        container.remove();
        if (opt.includes('Apply') || opt.includes('Admission Enquiry')) {
          addUserBubble(opt);
          startAdmissionFormFlow();
        } else if (opt === 'Back to Menu' || opt === 'Main Menu') {
          addUserBubble(opt);
          resetChat();
        } else {
          addUserBubble(opt);
          askAssistant(opt);
        }
      });
      container.appendChild(btn);
    });

    messagesCanvas.appendChild(container);
    scrollToBottom();
  }

  function showTyping() {
    const typing = document.createElement('div');
    typing.className = 'chat-row chat-row--bot typing-row';
    typing.id = 'typingIndicator';
    typing.innerHTML = `
      <div class="chat-row__avatar">
        <img src="assets/robot-mascot.svg" alt="Mascot">
      </div>
      <div class="typing-bubble">
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
      </div>
    `;
    messagesCanvas.appendChild(typing);
    scrollToBottom();
  }

  function hideTyping() {
    const t = document.getElementById('typingIndicator');
    if (t) t.remove();
  }

  function resetChat() {
    state.currentCategory = null;
    state.formMode = false;
    state.formCurrentStep = 0;
    state.formData = {};
    state.formSteps = [];
    formProgressBar.style.display = 'none';
    messagesCanvas.innerHTML = '';

    // Re-create initial greeting and grid
    const greeting = document.createElement('div');
    greeting.className = 'chat-row chat-row--bot';
    greeting.innerHTML = `
      <div class="chat-row__avatar">
        <img src="assets/robot-mascot.svg" alt="Mascot">
      </div>
      <div class="chat-row__bubble chat-row__bubble--bot">
        <p><strong>Hello! Welcome to Excel Engineering College.</strong></p>
        <p>I'm your college assistant. How can I help you today?</p>
        <p style="margin-top: 6px; font-size: 0.88rem; color: #5E6682;">
          Ask me about courses, admissions, departments, facilities and contact information.
        </p>
        <span class="chat-row__time">${getTimeString()}</span>
      </div>
    `;
    messagesCanvas.appendChild(greeting);

    if (categoryGrid) {
      categoryGrid.style.display = 'grid';
      messagesCanvas.appendChild(categoryGrid);
    }
  }

  // ==================== ADMISSION FORM FLOW ====================

  async function startAdmissionFormFlow() {
    state.formMode = true;
    state.formCurrentStep = 0;
    state.formData = {};

    showTyping();
    try {
      state.formSteps = await API.getAdmissionFormConfig();
      hideTyping();

      if (!state.formSteps || state.formSteps.length === 0) {
        throw new Error('Empty form config');
      }

      addBotBubble("Great! Let's start your admission enquiry. You can answer the questions below or tap the options. If you want to submit at any time, just click **Submit Now**.", true);

      formProgressBar.style.display = 'block';
      updateChatInputPlaceholder();
      renderCurrentFormStep();
    } catch (e) {
      hideTyping();
      addBotBubble("Could not load the admission enquiry form at the moment. Please call our admission office at +91 98427 13789.", false);
      exitFormMode();
    }
  }

  function updateChatInputPlaceholder() {
    if (!state.formMode || !chatInput) return;
    const step = state.formSteps[state.formCurrentStep];
    if (step && step.control !== 'chips' && step.control !== 'chips-select') {
      chatInput.placeholder = step.placeholder || 'Type your answer...';
    } else {
      chatInput.placeholder = 'Tap an option above...';
    }
  }

  function cleanInputValue(step, rawVal) {
    if (!rawVal) return rawVal;
    let v = rawVal.trim();
    // Clean percentage signs
    if (step.control === 'number' || step.field.toLowerCase().includes('percentage')) {
      v = v.replace(/%/g, '').trim();
    }
    // Clean marks like "90/100"
    if (step.control === 'number' && v.includes('/')) {
      v = v.split('/')[0].trim();
    }
    return v;
  }

  function renderCurrentFormStep() {
    const totalSteps = state.formSteps.length;
    const stepIdx = state.formCurrentStep;

    if (stepIdx >= totalSteps) {
      renderReviewSummary();
      return;
    }

    const step = state.formSteps[stepIdx];
    const pct = Math.round(((stepIdx + 1) / (totalSteps + 1)) * 100);
    formStepLabel.textContent = `Step ${stepIdx + 1} of ${totalSteps}`;
    formProgressFill.style.width = `${pct}%`;

    // Clean up any previously orphaned widgets
    const oldWidgets = messagesCanvas.querySelectorAll('.form-step-widget');
    oldWidgets.forEach(w => w.remove());

    // Show cutoff badge if Maths, Physics, Chemistry exist
    let cutoffNotice = '';
    if (state.formData.maths && state.formData.physics && state.formData.chemistry) {
      const m = parseFloat(state.formData.maths) || 0;
      const p = parseFloat(state.formData.physics) || 0;
      const c = parseFloat(state.formData.chemistry) || 0;
      const calcCutoff = (m + (p / 2.0) + (c / 2.0)).toFixed(2);
      cutoffNotice = `<div class="form-cutoff-badge">&#9733; Calculated Cutoff: ${calcCutoff} / 200</div>`;
    }

    const widget = document.createElement('div');
    widget.className = 'form-step-widget';
    widget.setAttribute('data-step-index', stepIdx);

    let controlsHtml = '';
    if (step.control === 'chips' || step.control === 'chips-select') {
      const optionsHtml = step.options.map(opt => `
        <button type="button" class="chip-btn form-opt-chip" data-value="${escapeHtml(opt)}">${escapeHtml(opt)}</button>
      `).join('');
      controlsHtml = `<div class="chips-container">${optionsHtml}</div>`;
    } else {
      const inputType = step.control === 'number' ? 'number' : (step.control === 'tel' ? 'tel' : (step.control === 'email' ? 'email' : 'text'));
      const minAttr = step.min !== undefined ? `min="${step.min}"` : '';
      const maxAttr = step.max !== undefined ? `max="${step.max}"` : '';
      const stepAttr = step.stepValue !== undefined ? `step="${step.stepValue}"` : '';
      
      // If Step 4 (District), add quick city suggestions!
      let quickSuggestions = '';
      if (step.field === 'District') {
        const chips = POPULAR_DISTRICTS.map(d => `<button type="button" class="mini-chip" data-city="${d}">${d}</button>`).join('');
        quickSuggestions = `
          <div class="district-quick-chips">
            <span style="font-size: 0.74rem; color: #5E6682; width: 100%;">Popular districts:</span>
            ${chips}
          </div>
        `;
      }

      controlsHtml = `
        <input 
          type="${inputType}" 
          class="form-step-widget__input form-step-input" 
          placeholder="${escapeHtml(step.placeholder || '')}"
          ${minAttr} ${maxAttr} ${stepAttr} 
          autocomplete="off">
        ${quickSuggestions}
        <div class="form-step-error form-error-msg" style="display: none;"></div>
      `;
    }

    // Check if user has entered basic contact info (FullName & Mobile)
    const canSubmitEarly = Boolean(state.formData.fullName && state.formData.mobile);
    const submitEarlyBtn = canSubmitEarly ? 
      `<button type="button" class="form-btn-submit-partial" id="formSubmitEarlyBtn" title="Submit with details entered so far">Submit Now &check;</button>` : '';

    widget.innerHTML = `
      <div class="form-step-widget__question">${escapeHtml(step.question)}</div>
      ${cutoffNotice}
      ${controlsHtml}
      <div class="form-step-widget__actions">
        ${stepIdx > 0 ? '<button type="button" class="form-btn-back form-back-btn">&larr; Back</button>' : '<div></div>'}
        <div style="display: flex; gap: 8px; align-items: center;">
          ${submitEarlyBtn}
          ${!step.required ? '<button type="button" class="form-btn-skip form-skip-btn">Skip</button>' : ''}
          ${step.control !== 'chips' && step.control !== 'chips-select' ? '<button type="button" class="form-btn-next form-next-btn">Next &rarr;</button>' : ''}
        </div>
      </div>
    `;

    messagesCanvas.appendChild(widget);
    scrollToBottom();

    // Event attachments
    if (step.control === 'chips' || step.control === 'chips-select') {
      widget.querySelectorAll('.form-opt-chip').forEach(btn => {
        btn.addEventListener('click', () => {
          const val = btn.getAttribute('data-value');
          widget.querySelectorAll('.form-opt-chip').forEach(b => b.classList.remove('chip-selected'));
          btn.classList.add('chip-selected');
          setTimeout(() => {
            saveStepAndAdvance(step.field, val);
          }, 120);
        });
      });
    } else {
      const input = widget.querySelector('.form-step-input');
      const nextBtn = widget.querySelector('.form-next-btn');

      // District quick suggestion chips
      widget.querySelectorAll('.mini-chip').forEach(cBtn => {
        cBtn.addEventListener('click', () => {
          const city = cBtn.getAttribute('data-city');
          if (input) input.value = city;
          saveStepAndAdvance(step.field, city);
        });
      });

      if (input) {
        const mappedField = step.field.charAt(0).toLowerCase() + step.field.slice(1);
        if (state.formData[mappedField]) {
          input.value = state.formData[mappedField];
        }
        input.focus();
      }

      if (nextBtn && input) {
        nextBtn.addEventListener('click', () => {
          validateAndAdvanceFromWidget(step, input, widget);
        });
        input.addEventListener('keydown', (e) => {
          if (e.key === 'Enter') {
            e.preventDefault();
            e.stopPropagation();
            validateAndAdvanceFromWidget(step, input, widget);
          }
        });
      }
    }

    const backBtn = widget.querySelector('.form-back-btn');
    if (backBtn) {
      backBtn.addEventListener('click', () => {
        widget.remove();
        state.formCurrentStep = Math.max(0, state.formCurrentStep - 1);
        updateChatInputPlaceholder();
        renderCurrentFormStep();
      });
    }

    const skipBtn = widget.querySelector('.form-skip-btn');
    if (skipBtn) {
      skipBtn.addEventListener('click', () => {
        addUserBubble('Skipped');
        saveStepAndAdvance(step.field, null);
      });
    }

    const earlyBtn = widget.querySelector('#formSubmitEarlyBtn');
    if (earlyBtn) {
      earlyBtn.addEventListener('click', () => {
        widget.remove();
        renderReviewSummary();
      });
    }

    updateChatInputPlaceholder();
  }

  function validateAndAdvanceFromWidget(step, input, widget) {
    let val = cleanInputValue(step, input.value);
    const errorEl = widget.querySelector('.form-step-error');

    if (step.required && (!val || val.length === 0)) {
      if (errorEl) {
        errorEl.textContent = step.errorMessage || 'This field is required.';
        errorEl.style.display = 'block';
      }
      input.focus();
      return;
    }

    if (val && step.validation) {
      const regex = new RegExp(step.validation);
      if (!regex.test(val)) {
        if (errorEl) {
          errorEl.textContent = step.errorMessage || 'Invalid input format.';
          errorEl.style.display = 'block';
        }
        input.focus();
        return;
      }
    }

    if (errorEl) errorEl.style.display = 'none';
    saveStepAndAdvance(step.field, val || null);
  }

  function handleFormTextInput(text) {
    const stepIdx = state.formCurrentStep;
    if (stepIdx >= state.formSteps.length) return;

    const step = state.formSteps[stepIdx];
    if (!step) return;

    if (step.control === 'chips' || step.control === 'chips-select') {
      const lowerText = text.toLowerCase().trim();
      const match = step.options.find(opt => opt.toLowerCase() === lowerText);
      if (match) {
        addUserBubble(match);
        saveStepAndAdvance(step.field, match);
      } else {
        addBotBubble("Please tap one of the options above to continue.", false);
      }
      return;
    }

    let val = cleanInputValue(step, text);

    if (step.required && (!val || val.length === 0)) {
      addBotBubble(step.errorMessage || 'This field is required. Please enter a valid value.', false);
      return;
    }

    if (val && step.validation) {
      const regex = new RegExp(step.validation);
      if (!regex.test(val)) {
        addBotBubble(step.errorMessage || 'Invalid format. Please try again.', false);
        return;
      }
    }

    saveStepAndAdvance(step.field, val || null);
  }

  function saveStepAndAdvance(field, val) {
    const allWidgets = messagesCanvas.querySelectorAll('.form-step-widget');
    allWidgets.forEach(w => w.remove());

    const mappedField = field.charAt(0).toLowerCase() + field.slice(1);
    if (val !== null && val !== undefined && val !== '') {
      state.formData[mappedField] = val;
      addUserBubble(val);
    }

    state.formCurrentStep++;

    setTimeout(() => {
      renderCurrentFormStep();
    }, 100);
  }

  function renderReviewSummary() {
    formStepLabel.textContent = 'Review & Submit';
    formProgressFill.style.width = '100%';

    if (chatInput) chatInput.placeholder = 'Ask another question...';

    const data = state.formData;

    // Calculate cutoff score
    let calculatedCutoff = 'N/A';
    if (data.maths && data.physics && data.chemistry) {
      const m = parseFloat(data.maths) || 0;
      const p = parseFloat(data.physics) || 0;
      const c = parseFloat(data.chemistry) || 0;
      calculatedCutoff = (m + (p / 2.0) + (c / 2.0)).toFixed(2) + ' / 200';
    }

    const isHalfFilled = !data.programme1 || !data.twelfthPercentage;
    const noticeBanner = isHalfFilled ? 
      `<div style="background: #FEF3C7; border: 1px solid #F59E0B; color: #92400E; padding: 8px 12px; border-radius: 10px; font-size: 0.82rem; margin-bottom: 12px; font-weight: 500;">
        &bull; Submitting partial details. Our admission counselors will assist you with the rest during counselling.
      </div>` : '';

    const card = document.createElement('div');
    card.className = 'review-card';
    card.id = 'admissionReviewCard';

    card.innerHTML = `
      <h3>Review Your Admission Enquiry</h3>
      ${noticeBanner}
      <div class="review-grid">
        <div class="review-item"><label>Full Name</label><span class="review-value">${escapeHtml(data.fullName || '-')}</span></div>
        <div class="review-item"><label>Mobile</label><span class="review-value">${escapeHtml(data.mobile || '-')}</span></div>
        <div class="review-item"><label>Email</label><span class="review-value">${escapeHtml(data.email || 'Not provided')}</span></div>
        <div class="review-item"><label>District / City</label><span class="review-value">${escapeHtml(data.district || 'Not specified')}</span></div>
        <div class="review-item"><label>Board</label><span class="review-value">${escapeHtml(data.board || 'Not specified')}</span></div>
        <div class="review-item"><label>Stream</label><span class="review-value">${escapeHtml(data.stream || 'Not specified')}</span></div>
        <div class="review-item"><label>12th Percentage</label><span class="review-value">${data.twelfthPercentage ? escapeHtml(data.twelfthPercentage) + '%' : 'Pending'}</span></div>
        <div class="review-item"><label>Maths</label><span class="review-value">${escapeHtml(data.maths || '-')}</span></div>
        <div class="review-item"><label>Physics</label><span class="review-value">${escapeHtml(data.physics || '-')}</span></div>
        <div class="review-item"><label>Chemistry</label><span class="review-value">${escapeHtml(data.chemistry || '-')}</span></div>
        <div class="review-item"><label>Calculated Cutoff</label><span class="review-value" style="color: #047857; font-weight: 700;">${calculatedCutoff}</span></div>
        <div class="review-item"><label>Official Cutoff</label><span class="review-value">${escapeHtml(data.officialCutoff || 'Not provided')}</span></div>
        <div class="review-item" style="grid-column: span 2;"><label>1st Preferred Programme</label><span class="review-value" style="color: #1E4ED8; font-weight: 600;">${escapeHtml(data.programme1 || 'General Enquiry / Counselling Pending')}</span></div>
        <div class="review-item"><label>2nd Preference</label><span class="review-value">${escapeHtml(data.programme2 || 'Not provided')}</span></div>
        <div class="review-item"><label>Admission Type</label><span class="review-value">${escapeHtml(data.admissionType || 'Regular')}</span></div>
        <div class="review-item"><label>Hostel Required</label><span class="review-value">${escapeHtml(data.hostelRequired || 'Not specified')}</span></div>
        <div class="review-item"><label>Transport Required</label><span class="review-value">${escapeHtml(data.transportRequired || 'Not specified')}</span></div>
        <div class="review-item"><label>Contact Time</label><span class="review-value">${escapeHtml(data.contactTime || 'Anytime')}</span></div>
        <div class="review-item" style="grid-column: span 2;"><label>Message</label><span class="review-value">${escapeHtml(data.message || 'None')}</span></div>
      </div>

      <label class="review-consent">
        <input type="checkbox" id="admissionConsentCheck" checked>
        <span>I authorize Excel Engineering College to contact me regarding admissions &amp; academic programs.</span>
      </label>

      <div class="review-actions">
        <button type="button" class="review-edit-btn" id="editEnquiryBtn">&larr; Edit Answers</button>
        <button type="button" class="review-submit-btn" id="submitEnquiryBtn">Confirm &amp; Submit Enquiry</button>
      </div>
    `;

    messagesCanvas.appendChild(card);
    scrollToBottom();

    const submitBtn = document.getElementById('submitEnquiryBtn');
    if (submitBtn) {
      submitBtn.addEventListener('click', submitFinalEnquiry);
    }

    const editBtn = document.getElementById('editEnquiryBtn');
    if (editBtn) {
      editBtn.addEventListener('click', () => {
        card.remove();
        state.formCurrentStep = 0;
        renderCurrentFormStep();
      });
    }
  }

  async function submitFinalEnquiry() {
    const consentCheck = document.getElementById('admissionConsentCheck');
    if (consentCheck && !consentCheck.checked) {
      alert('Please check the authorization box to proceed.');
      return;
    }

    state.formData.consentGiven = true;
    const submitBtn = document.getElementById('submitEnquiryBtn');
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = 'Submitting Enquiry...';
    }

    showTyping();
    try {
      const res = await API.submitAdmissionEnquiry(state.formData);
      hideTyping();

      const reviewCard = document.getElementById('admissionReviewCard');
      if (reviewCard) reviewCard.remove();
      formProgressBar.style.display = 'none';
      state.formMode = false;

      if (chatInput) chatInput.placeholder = 'Ask another question...';

      if (res.duplicate) {
        addBotBubble(res.message || "We already have an enquiry from this mobile number. Our admissions team will reach out to you shortly!", true);
        renderOptionChips(['Explore Courses', 'Campus Facilities', 'Main Menu']);
        return;
      }

      const successCard = document.createElement('div');
      successCard.className = 'success-card';
      successCard.innerHTML = `
        <div class="success-card__icon">&#127881;</div>
        <h3>Enquiry Submitted Successfully!</h3>
        <p>Thank you, <strong>${escapeHtml(state.formData.fullName || 'Student')}</strong>. We have registered your admission enquiry.</p>
        <div class="success-card__ref">Reference ID: <strong>${escapeHtml(res.enquiryId || 'EXCEL-ADM-2026')}</strong></div>
        ${res.calculatedCutoff ? `<div class="success-card__cutoff">Calculated Cutoff: <strong>${res.calculatedCutoff} / 200</strong></div>` : ''}
        <p style="font-size: 0.85rem;">Our admissions counsellors will contact you at your preferred time.</p>
      `;
      messagesCanvas.appendChild(successCard);
      scrollToBottom();

      renderOptionChips([
        'Explore Courses',
        'Campus Facilities',
        'Main Menu'
      ]);

    } catch (err) {
      hideTyping();
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Confirm & Submit Enquiry';
      }
      addBotBubble("Submission error: " + (err.message || 'Please try again.'), false);
    }
  }

  function handleCancelForm() {
    if (state.formData.fullName && state.formData.mobile) {
      const shouldSubmit = confirm("You have already entered your contact details. Would you like to submit your enquiry now before exiting?");
      if (shouldSubmit) {
        const widget = document.querySelector('.form-step-widget');
        if (widget) widget.remove();
        renderReviewSummary();
        return;
      }
    }
    exitFormMode();
  }

  function exitFormMode() {
    state.formMode = false;
    state.formCurrentStep = 0;
    state.formData = {};
    state.formSteps = [];
    formProgressBar.style.display = 'none';

    messagesCanvas.querySelectorAll('.form-step-widget').forEach(w => w.remove());
    const review = document.getElementById('admissionReviewCard');
    if (review) review.remove();

    if (chatInput) chatInput.placeholder = 'Ask another question...';

    addBotBubble("Admission enquiry cancelled. Feel free to ask any other questions about Excel Engineering College!");
    renderOptionChips(CATEGORIES['Admissions'].options);
  }

  // ==================== UTILITY HELPERS ====================

  function scrollToBottom() {
    setTimeout(() => {
      messagesCanvas.scrollTop = messagesCanvas.scrollHeight;
    }, 50);
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function formatMarkdown(text) {
    if (!text) return '';
    let s = escapeHtml(text);

    s = s.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    s = s.replace(/\*(.*?)\*/g, '<em>$1</em>');
    s = s.replace(/^\s*[-•]\s+(.*)$/gm, '<li style="margin-left: 16px;">$1</li>');
    s = s.replace(/\n\n+/g, '</p><p style="margin-top: 8px;">');
    s = s.replace(/\n/g, '<br>');

    s = s.replace(/(\+91\s?[6-9]\d{4}\s?\d{5})/g, '<a href="tel:$1" style="color: #2F6BFF; font-weight: 600;">$1</a>');
    s = s.replace(/([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g, '<a href="mailto:$1" style="color: #2F6BFF; font-weight: 600;">$1</a>');
    s = s.replace(/(https?:\/\/[^\s<]+)/g, '<a href="$1" target="_blank" rel="noopener noreferrer" style="color: #2F6BFF; text-decoration: underline;">$1</a>');

    return `<p>${s}</p>`;
  }

})();
