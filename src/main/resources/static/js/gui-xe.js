/**
 * PARKING PRO - Script vận hành giao diện tiếng Việt
 */
document.addEventListener('DOMContentLoaded', function() {
    // Tự động viết hoa biển số xe
    document.querySelectorAll('.input-plate-auto, input[name="bienSoXe"], input[name="bienSo"]').forEach(input => {
        input.addEventListener('input', function() {
            this.value = this.value.toUpperCase();
        });
    });
});
