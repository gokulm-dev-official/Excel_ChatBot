# Excel Engineering College (Autonomous) - AI Chatbot Website

A production-quality, full-stack college website and conversational assistant for **Excel Engineering College (Autonomous)**, built with Spring Boot 3.x, Java 21, Vanilla JavaScript, and an institutional Apple-style design system.

---

## 🏛️ Project Highlights

- **Mandatory Branding Assets**:
  - `excel-logo.jpg` / `excel-logo.png` & `excel-logo-light.png`: Authentic college crest and wordmark placed exclusively on clean white surfaces, badges, navbar, chatbot header, and footer without distortion.
  - `college-campus.jpg` / `college-campus.png`: Authentic photograph of the domed main building with brand-aligned orange basketball post accents, cinematic hero background with a navy gradient overlay, about-section showcase, and chatbot header backdrop.
  - `robot-mascot.svg`: Friendly AI Assistant mascot matching the visual design reference.
- **Design Tokens & Extended Palette**:
  - Brand Navy (`#050E3A` / `#0A1C66` / `#292F91`), Accent Blue (`#2F6BFF`), Soft Tint (`#E9EEFF`, `#F0F4FC`), and Vibrant Orange Accent (`#E77932` / `#FF7A00`).
  - Google Fonts: **Plus Jakarta Sans** (Headings), **Inter** (Body), and **Playfair Display Italic** (Taglines).
  - Smooth micro-interactions, card hover elevations, glowing arcs, and accessible focus states.
- **Three Core Experiences**:
  1. **Cinematic Splash Screen**: Brand reveal with glowing crest, orange arc curve, building silhouette fade, circular rotating loader, and instant skip control. Plays once per session (`sessionStorage`).
  2. **Institutional Homepage**: Sticky rounded navbar pill with robot icon AI Assistant button, cinematic hero with curved navy banner and campus photo, welcome strip, 5 category explore cards with circular arrow buttons, AI promo banner with robot mascot, verified contact details, and institutional footer.
  3. **Excel AI Assistant (`chatbot.html`)**: Two-panel branded interface, floating rounded card layout, guided category cards, dynamic quick-reply chips, verified response badges, rich clickable contact cards (`tel:`, `mailto:`, web links), manual input auto-classification, and conversational controls (Back, Main Menu, New Conversation).
- **Deterministic Response Engine**:
  - In-house NLP pipeline: TextNormalizer (typo tolerance, light stemming), SynonymDictionary (`nlp/synonyms.json`), IntentDetector, QuestionClassifierService, and KnowledgeSearchService (35% subcat, 30% Jaccard+trigram, 20% entity, 15% category).
  - Verbatim answers strictly sourced from Google Sheets records with clean connective templates.
  - Exact verified fallback for unanswered queries.
  - Zero external paid AI APIs, zero external dependencies.
- **Conversational Admission Enquiry Assistant**:
  - 18-step guided application flow inside the chatbot interface.
  - Server-side cutoff calculation (`Maths + Physics/2 + Chemistry/2` out of 200).
  - Formula injection protection, duplicate prevention (SHA-256 mobile + programme + date), and in-memory rate limiting (5 submissions/hour/IP).
  - Forwarding to Google Apps Script Web App for storage in Google Sheets.

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.0, Spring Web, Spring Validation, Maven
- **Frontend**: HTML5, CSS3 Custom Properties (`tokens.css`, `base.css`, `splash.css`, `home.css`, `chatbot.css`), Vanilla JavaScript (`api.js`, `splash.js`, `home.js`, `chatbot.js`)
- **Engine**: In-House Deterministic Response & Search Engine
- **Data Source**: Google Spreadsheet (ID: `1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA`, Sheet: `CollegeData`)

---

## 📁 Project Structure

```text
Excel_ChatBot/
├── .env.example
├── .env
├── .gitignore
├── pom.xml
├── README.md
├── mvn.ps1
├── docs/
│   ├── apps-script/
│   │   └── Code.gs
│   └── ui-reference/
│       └── Excel_College_AI_Assistant_Experience.png
├── src/
│   ├── main/
│   │   ├── java/com/excel/chatbot/
│   │   │   ├── ExcelChatbotApplication.java
│   │   │   ├── config/
│   │   │   │   ├── GoogleSheetsConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdmissionController.java
│   │   │   │   ├── ChatController.java
│   │   │   │   └── CollegeDataController.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   │   ├── AdmissionEnquiry.java
│   │   │   │   ├── CategoryResponse.java
│   │   │   │   ├── ChatRequest.java
│   │   │   │   ├── ChatResponse.java
│   │   │   │   ├── CollegeInfo.java
│   │   │   │   ├── ContactInfo.java
│   │   │   │   └── FollowUpOption.java
│   │   │   ├── nlp/
│   │   │   │   ├── ContactExtractor.java
│   │   │   │   ├── IntentDetector.java
│   │   │   │   ├── SynonymDictionary.java
│   │   │   │   └── TextNormalizer.java
│   │   │   └── service/
│   │   │       ├── AdmissionEnquiryService.java
│   │   │       ├── ChatService.java
│   │   │       ├── FollowUpOptionsBuilder.java
│   │   │       ├── GoogleSheetsService.java
│   │   │       ├── KnowledgeSearchService.java
│   │   │       ├── QuestionClassifierService.java
│   │   │       └── ResponseComposer.java
│   │   └── resources/
│   │       ├── admission-form.json
│   │       ├── application.properties
│   │       ├── nlp/
│   │       │   └── synonyms.json
│   │       └── static/
│   │           ├── index.html
│   │           ├── chatbot.html
│   │           ├── css/
│   │           │   ├── tokens.css
│   │           │   ├── base.css
│   │           │   ├── splash.css
│   │           │   ├── home.css
│   │           │   └── chatbot.css
│   │           ├── js/
│   │           │   ├── api.js
│   │           │   ├── splash.js
│   │           │   ├── home.js
│   │           │   └── chatbot.js
│   │           └── assets/
│   │               ├── excel-logo.jpg (and .png)
│   │               ├── excel-logo-light.png
│   │               ├── college-campus.jpg (and .png)
│   │               └── robot-mascot.svg
│   └── test/
│       └── java/com/excel/chatbot/
│           ├── ExcelChatbotApplicationTests.java
│           └── VerifyTestRunner.java
```

---

## ⚙️ Environment Configuration

Create or modify `.env` in the project root:

```properties
GOOGLE_SHEETS_ID=1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA
GOOGLE_SHEETS_TAB=Sheet1
GOOGLE_SHEETS_READ_URL=
GOOGLE_ENQUIRY_SHEET_ID=1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA
GOOGLE_APPS_SCRIPT_URL=
GOOGLE_APPS_SCRIPT_TOKEN=excel-admission-token-2026
ADMIN_API_KEY=excel-admin-secret-key-2026
SERVER_PORT=8080
SHEETS_CACHE_SECONDS=60
```

---

## 📊 Google Sheets & Admission Enquiry Storage Setup

### 1. Store Admission Enquiries in Google Sheets (Apps Script)
To save full and half-filled admission enquiries directly into your spreadsheet:
1. Open your Google Sheet: [Excel_Chatbot Spreadsheet](https://docs.google.com/spreadsheets/d/1lYbmg-7LYewpJhxTVqyYZSToFS7XjUyLDdwIQkJn5zA/edit)
2. In the top menu, click **Extensions** > **Apps Script**.
3. Delete any default code in the editor and paste the entire contents of [`docs/apps-script/Code.gs`](file:///d:/Excel_ChatBot/docs/apps-script/Code.gs).
4. Click the blue **Deploy** button at top right > **New deployment**.
5. Click the gear icon next to "Select type" and choose **Web app**.
6. Set:
   - **Description**: `Excel Chatbot Admission Webhook`
   - **Execute as**: `Me`
   - **Who has access**: `Anyone` *(Crucial so the server can submit enquiries)*
7. Click **Deploy** and grant access permissions when prompted.
8. Copy the **Web app URL** (looks like `https://script.google.com/macros/s/.../exec`).
9. Paste that URL into `.env`:
   ```properties
   GOOGLE_APPS_SCRIPT_URL=https://script.google.com/macros/s/.../exec
   ```
10. The script will automatically create an **`AdmissionEnquiries`** tab with styled Navy headers if it does not already exist, and all submitted enquiries (including calculated cutoffs) will be appended as new rows!

### 2. Populate Knowledge Base in `Sheet1`
A ready-to-import CSV with all 25 verified records across Courses, Admissions, Departments, Facilities, and Contact Information is available at [`docs/google-sheets/CollegeData_Sheet1.csv`](file:///d:/Excel_ChatBot/docs/google-sheets/CollegeData_Sheet1.csv):
1. In the same Google Sheet, select the **Sheet1** tab.
2. Click **File** > **Import** > **Upload**.
3. Select `docs/google-sheets/CollegeData_Sheet1.csv`.
4. Choose **Replace current sheet** and click **Import data**.
5. Ensure the sheet is shared as **Anyone with the link can view**.

---

## 🚀 How to Run

### Package & Run with Java 21+

```powershell
powershell -ExecutionPolicy Bypass -File .\mvn.ps1 package "-DskipTests" -o
java -jar target\excel-chatbot-1.0.0.jar
```

Open your browser at `http://localhost:8080`.
