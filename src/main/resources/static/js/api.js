/**
 * Excel Engineering College - API Module
 */

const API = (() => {
  const BASE_URL = '';

  async function request(url, options = {}) {
    try {
      const response = await fetch(`${BASE_URL}${url}`, {
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.warn('API Request error:', error.message);
      throw error;
    }
  }

  return {
    async getCategories() {
      return request('/api/college/categories');
    },

    async getAllData() {
      return request('/api/college/data');
    },

    async getDataByCategory(category) {
      return request(`/api/college/data/${encodeURIComponent(category)}`);
    },

    async getDepartments() {
      return request('/api/college/departments');
    },

    async getCourses() {
      return request('/api/college/courses');
    },

    async sendMessage(message, category = null, context = null) {
      return request('/api/chat/message', {
        method: 'POST',
        body: JSON.stringify({
          message,
          category,
          context,
        }),
      });
    },

    async getAdmissionFormConfig() {
      return request('/api/admissions/form-config');
    },

    async submitAdmissionEnquiry(enquiry) {
      return request('/api/admissions/enquiry', {
        method: 'POST',
        body: JSON.stringify(enquiry),
      });
    },

    async refreshData(adminKey = 'excel-admin-secret-key-2026') {
      return request('/api/college/refresh', {
        method: 'POST',
        headers: {
          'X-Admin-Key': adminKey,
        },
      });
    },
  };
})();
