/**
 * Excel Engineering College - Homepage Controller
 */

window.HomeAnimations = (() => {
  'use strict';

  let initialized = false;

  function init() {
    if (initialized) return;
    initialized = true;

    initNavbar();
    initScrollSpy();
    initMobileMenu();
    initScrollEffects();
    loadLiveContactData();
  }

  // Navbar transparent to solid transition
  function initNavbar() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    function onScroll() {
      if (window.scrollY > 60) {
        navbar.classList.add('navbar--solid');
        navbar.classList.remove('navbar--transparent');
      } else {
        navbar.classList.add('navbar--transparent');
        navbar.classList.remove('navbar--solid');
      }
    }

    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
  }

  // ScrollSpy for navbar links
  function initScrollSpy() {
    const links = document.querySelectorAll('.navbar__link[data-section]');
    const sections = [];

    links.forEach(link => {
      const sectionId = link.getAttribute('data-section');
      const section = document.getElementById(sectionId);
      if (section) sections.push({ link, section });
    });

    if (!sections.length) return;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          links.forEach(l => l.classList.remove('active'));
          const match = sections.find(s => s.section === entry.target);
          if (match) match.link.classList.add('active');
        }
      });
    }, {
      rootMargin: '-20% 0px -60% 0px',
      threshold: 0
    });

    sections.forEach(s => observer.observe(s.section));
  }

  // Mobile Menu Toggle
  function initMobileMenu() {
    const toggle = document.querySelector('.navbar__toggle');
    const mobile = document.querySelector('.navbar__mobile');
    if (!toggle || !mobile) return;

    toggle.addEventListener('click', () => {
      const isOpen = toggle.classList.toggle('active');
      mobile.classList.toggle('open');
      document.body.classList.toggle('no-scroll', isOpen);
    });

    mobile.querySelectorAll('.navbar__link, .navbar__cta').forEach(link => {
      link.addEventListener('click', () => {
        toggle.classList.remove('active');
        mobile.classList.remove('open');
        document.body.classList.remove('no-scroll');
      });
    });
  }

  // Scroll effects & triggers
  function initScrollEffects() {
    const heroBg = document.querySelector('.hero__bg img');
    if (heroBg && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      window.addEventListener('scroll', () => {
        const scrolled = window.scrollY;
        if (scrolled < window.innerHeight) {
          heroBg.style.transform = `translateY(${scrolled * 0.15}px) scale(1.05)`;
        }
      }, { passive: true });
    }

    // Scroll to About on hero scroll mouse click
    const scrollMouse = document.querySelector('.hero__scroll');
    if (scrollMouse) {
      scrollMouse.addEventListener('click', () => {
        const aboutSec = document.getElementById('about');
        if (aboutSec) {
          aboutSec.scrollIntoView({ behavior: 'smooth' });
        }
      });
    }

    // Back to top button
    const backTop = document.querySelector('.footer__back-top');
    if (backTop) {
      backTop.addEventListener('click', () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
    }
  }

  // Fetch and populate verified contact info
  async function loadLiveContactData() {
    const contactGrid = document.querySelector('.contact__grid');
    if (!contactGrid) return;

    try {
      const data = await API.getDataByCategory('Contact Information');
      if (!data || !data.length) return;

      contactGrid.innerHTML = '';

      const iconSvg = {
        pin: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>',
        phone: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>',
        mail: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>',
        globe: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20"/><path d="M2 12h20"/></svg>',
        clock: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
      };

      data.forEach(item => {
        const sub = (item.subcategory || '').toLowerCase();
        let icon = iconSvg.pin;
        let contentHtml = escapeHtml(item.answer || '');

        if (sub.includes('phone') || sub.includes('mobile') || sub.includes('contact')) {
          icon = iconSvg.phone;
          const phone = escapeHtml(item.answer);
          contentHtml = `<a href="tel:${phone.replace(/[^\d+]/g, '')}">${phone}</a>`;
        } else if (sub.includes('email') || sub.includes('mail')) {
          icon = iconSvg.mail;
          const mail = escapeHtml(item.answer);
          contentHtml = `<a href="mailto:${mail}">${mail}</a>`;
        } else if (sub.includes('website') || sub.includes('web') || sub.includes('portal')) {
          icon = iconSvg.globe;
          const url = item.answer.startsWith('http') ? item.answer : 'https://' + item.answer;
          contentHtml = `<a href="${url}" target="_blank" rel="noopener noreferrer">${escapeHtml(item.answer)}</a>`;
        } else if (sub.includes('time') || sub.includes('hours') || sub.includes('timing')) {
          icon = iconSvg.clock;
        }

        const card = document.createElement('div');
        card.className = 'card contact__card';
        card.innerHTML = `
          <div class="icon-tile">${icon}</div>
          <h4>${escapeHtml(item.subcategory || 'Contact Details')}</h4>
          <p>${contentHtml}</p>
        `;
        contactGrid.appendChild(card);
      });
    } catch (e) {
      console.log('Using default verified contact layout');
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    const d = document.createElement('div');
    d.textContent = str;
    return d.innerHTML;
  }

  return { init };
})();

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    if (sessionStorage.getItem('splashSeen') === 'true') {
      window.HomeAnimations.init();
    }
  });
} else {
  if (sessionStorage.getItem('splashSeen') === 'true') {
    window.HomeAnimations.init();
  }
}

