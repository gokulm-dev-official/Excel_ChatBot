/**
 * Excel Engineering College - Splash Screen Controller
 */

(function() {
  'use strict';

  const splash = document.querySelector('.splash');
  if (!splash) return;

  // Check if splash has already been seen in this session
  if (sessionStorage.getItem('splashSeen') === 'true') {
    splash.classList.add('hidden');
    splash.style.display = 'none';
    if (window.HomeAnimations) window.HomeAnimations.init();
    return;
  }

  // Create ambient dots
  const dotsContainer = document.querySelector('.splash__dots');
  if (dotsContainer) {
    for (let i = 0; i < 20; i++) {
      const dot = document.createElement('div');
      dot.className = 'splash__dot';
      dot.style.left = (Math.random() * 100) + '%';
      dot.style.top = (Math.random() * 100) + '%';
      dot.style.animationDelay = (Math.random() * 4) + 's';
      dot.style.animationDuration = (4 + Math.random() * 4) + 's';
      dotsContainer.appendChild(dot);
    }
  }

  const progressBar = document.querySelector('.splash__progress-fill');
  const skipBtn = document.querySelector('.splash__skip');

  let splashEnded = false;

  function endSplash() {
    if (splashEnded) return;
    splashEnded = true;
    sessionStorage.setItem('splashSeen', 'true');
    splash.classList.add('hidden');
    setTimeout(() => {
      splash.style.display = 'none';
      if (window.HomeAnimations) window.HomeAnimations.init();
    }, 800);
  }

  if (skipBtn) {
    skipBtn.addEventListener('click', endSplash);
    // Show skip after 1 second
    setTimeout(() => {
      skipBtn.style.opacity = '1';
    }, 1000);
  }

  // Progress Bar Animation
  let progress = 0;
  const interval = setInterval(() => {
    progress += 2;
    if (progressBar) {
      progressBar.style.width = progress + '%';
    }
    if (progress >= 100) {
      clearInterval(interval);
      setTimeout(endSplash, 400);
    }
  }, 50); // ~2.5 to 3.0s total

  // Respect reduced motion
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    clearInterval(interval);
    setTimeout(endSplash, 600);
  }
})();

