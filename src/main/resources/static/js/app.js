/**
 * PARKING PRO - Global Application Scripts
 * Operational Software Interactions & Responsiveness
 */

document.addEventListener('DOMContentLoaded', function() {
    // 1. Live Vietnamese Clock & Date
    updateLiveDateTime();
    setInterval(updateLiveDateTime, 1000);

    // 2. Global search shortcut (focus search on '/')
    document.addEventListener('keydown', function(e) {
        if (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
            e.preventDefault();
            const searchInput = document.getElementById('globalSearchInput');
            if (searchInput) searchInput.focus();
        }
    });

    // 3. Auto-uppercase for plate inputs
    document.querySelectorAll('.input-plate-auto, input[name="bienSoXe"], input[name="bienSo"]').forEach(input => {
        input.addEventListener('input', function() {
            this.value = this.value.toUpperCase();
        });
    });

    // 4. Close mobile sidebar when clicking backdrop
    const backdrop = document.getElementById('sidebarBackdrop');
    if (backdrop) {
        backdrop.addEventListener('click', function() {
            toggleMobileSidebar(false);
        });
    }

    // 5. Close mobile sidebar on link click
    document.querySelectorAll('.app-sidebar .sidebar-link').forEach(link => {
        link.addEventListener('click', function() {
            if (window.innerWidth <= 992) {
                toggleMobileSidebar(false);
            }
        });
    });

    // 6. Close mobile sidebar on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            toggleMobileSidebar(false);
        }
    });

    // 7. Auto-dismiss flash alerts after 6 seconds
    setTimeout(function() {
        document.querySelectorAll('.alert-dismissible').forEach(function(alert) {
            try {
                if (window.bootstrap && bootstrap.Alert) {
                    const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
                    if (bsAlert) bsAlert.close();
                }
            } catch(ignored) {}
        });
    }, 6000);
});

// Mobile Sidebar Drawer Toggle
function toggleMobileSidebar(forceState) {
    const sidebar = document.querySelector('.app-sidebar');
    const backdrop = document.getElementById('sidebarBackdrop');
    if (!sidebar) return;

    if (forceState !== undefined) {
        if (forceState) {
            sidebar.classList.add('show-mobile');
            if (backdrop) backdrop.classList.add('show');
        } else {
            sidebar.classList.remove('show-mobile');
            if (backdrop) backdrop.classList.remove('show');
        }
    } else {
        sidebar.classList.toggle('show-mobile');
        if (backdrop) backdrop.classList.toggle('show');
    }
}

// Live Clock & Date in Vietnamese
function updateLiveDateTime() {
    const el = document.getElementById('headerLiveDate');
    if (!el) return;
    
    const now = new Date();
    const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
    const dayName = days[now.getDay()];
    
    const day = String(now.getDate()).padStart(2, '0');
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const year = now.getFullYear();
    const hours = String(now.getHours()).padStart(2, '0');
    const mins = String(now.getMinutes()).padStart(2, '0');
    const secs = String(now.getSeconds()).padStart(2, '0');

    el.innerHTML = `<i class="fa-regular fa-calendar-days text-primary me-1"></i> ${dayName}, ${day}/${month}/${year} &nbsp; <i class="fa-regular fa-clock text-primary ms-2 me-1"></i> ${hours}:${mins}:${secs}`;
}

// Global quick search handler
function handleGlobalSearch(event) {
    event.preventDefault();
    const input = document.getElementById('globalSearchInput');
    if (!input || !input.value.trim()) return false;
    const query = encodeURIComponent(input.value.trim().toUpperCase());
    window.location.href = '/gate?tab=checkout&query=' + query;
    return false;
}

// Show / Hide password toggle
function togglePasswordVisibility(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        if (icon) icon.className = 'fa-solid fa-eye-slash';
    } else {
        input.type = 'password';
        if (icon) icon.className = 'fa-solid fa-eye';
    }
}
