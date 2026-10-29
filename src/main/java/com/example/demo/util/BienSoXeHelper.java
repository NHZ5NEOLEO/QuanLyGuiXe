package com.example.demo.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích chuẩn hóa và xử lý biển số xe Việt Nam.
 * Tham chiếu Thông tư 79/2024/TT-BCA, Thông tư 24/2023/TT-BCA và quy định về biển số đang lưu hành.
 *
 * Tách biệt rõ 3 mức xử lý:
 * 1. Giá trị người dùng nhập (Raw input).
 * 2. Giá trị chuẩn hóa để đối chiếu, kiểm tra trùng và tìm kiếm (Clean key - không dấu phân cách, chữ hoa).
 * 3. Giá trị hiển thị chuẩn mực theo loại phương tiện và quy cách biển số (Canonical display format).
 */
public final class BienSoXeHelper {

    private BienSoXeHelper() {
        // Utility class
    }

    // Biểu thức cho Ô tô 5 số (Sê-ri 1 chữ cái): ví dụ 30A12345, 29C45678, 51K99999
    private static final Pattern PATTERN_CAR_5_SINGLE = Pattern.compile("^(\\d{2})([A-Z])(\\d{3})(\\d{2})$");

    // Biểu thức cho Ô tô 5 số (Sê-ri 2 chữ cái - Liên doanh, thương mại, con mới): ví dụ 51LD12345, 30DA12345
    private static final Pattern PATTERN_CAR_5_DOUBLE = Pattern.compile("^(\\d{2})([A-Z]{2})(\\d{3})(\\d{2})$");

    // Biểu thức cho Xe máy 5 số thông thường (Sê-ri 1 chữ cái + 1 số): ví dụ 29B188888, 59X399999, 43B199881
    private static final Pattern PATTERN_MOTO_5_STANDARD = Pattern.compile("^(\\d{2})([A-Z]\\d)(\\d{3})(\\d{2})$");

    // Biểu thức cho Xe máy 5 số (Sê-ri 2 chữ cái cho xe máy điện, xe < 50cc): ví dụ 29MD12345, 29AA88888
    private static final Pattern PATTERN_MOTO_5_ELECTRIC = Pattern.compile("^(\\d{2})([A-Z]{2})(\\d{3})(\\d{2})$");

    // Biểu thức cho Ô tô 4 số cũ (vẫn lưu hành hợp pháp): ví dụ 29A1234, 52Z9999, 33M5678
    private static final Pattern PATTERN_CAR_4 = Pattern.compile("^(\\d{2})([A-Z]{1,2})(\\d{4})$");

    // Biểu thức cho Xe máy 4 số cũ (vẫn lưu hành hợp pháp): ví dụ 29B11234, 33M21234, 29K15678
    private static final Pattern PATTERN_MOTO_4 = Pattern.compile("^(\\d{2})([A-Z]\\d)(\\d{4})$");

    // Biểu thức kiểm tra an toàn ký tự (chỉ cho phép chữ cái, chữ số, khoảng trắng, gạch ngang, dấu chấm)
    private static final Pattern PATTERN_ALLOWED_CHARS = Pattern.compile("^[0-9a-zA-Z\\s\\-\\.,_]+$");

    /**
     * Kết quả kiểm tra tính hợp lệ của biển số
     */
    public record ValidationResult(boolean valid, String message, String cleanKey, String displayValue) {}

    /**
     * BƯỚC 2: Chuẩn hóa chuỗi tìm kiếm và kiểm tra trùng lặp (Clean Key).
     * Loại bỏ toàn bộ khoảng trắng, dấu gạch ngang, dấu chấm, dấu phẩy, đưa về CHỮ HOA.
     * Bảo toàn nguyên vẹn tất cả chữ số 0 ở đầu mã địa phương và nhóm số đăng ký.
     */
    public static String toCleanKey(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\s\\-\\.,_]+", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Kiểm tra tính hợp lệ cơ bản của dữ liệu nhập.
    /**
     * Kiểm tra tính hợp lệ cơ bản của dữ liệu nhập.
     * Chặn các trường hợp gõ bừa chỉ gồm số, thiếu sê-ri, hoặc sai quy chuẩn biển số Việt Nam.
     */
    public static ValidationResult validate(String rawInput, String loaiXeTen) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return new ValidationResult(false, "Biển số xe không được để trống!", "", "");
        }

        String trimmed = rawInput.trim();
        if (!PATTERN_ALLOWED_CHARS.matcher(trimmed).matches()) {
            return new ValidationResult(false, "Biển số xe chứa ký tự không hợp lệ! Chỉ cho phép chữ cái (A-Z), chữ số (0-9), dấu gạch ngang (-) và dấu chấm (.).", "", "");
        }

        String cleanKey = toCleanKey(trimmed);

        // ĐẶC BIỆT: HỖ TRỢ PHƯƠNG TIỆN KHÔNG BIỂN SỐ (XE ĐẠP, XE ĐIỆN KHÔNG BIỂN, THẺ SỐ...)
        boolean isBicycle = (loaiXeTen != null && (loaiXeTen.toLowerCase().contains("đạp") || loaiXeTen.toLowerCase().contains("bicycle")))
                || cleanKey.startsWith("XD") || cleanKey.startsWith("XEDAP");

        if (isBicycle) {
            if (cleanKey.length() < 2) {
                return new ValidationResult(false, 
                    "Mã định danh xe đạp quá ngắn! Vui lòng nhập số thẻ hoặc mã xe (Ví dụ: XD-01, XD-88).", 
                    cleanKey, "");
            }
            String displayVal = cleanKey;
            if (cleanKey.matches("^\\d+$")) {
                displayVal = "XD-" + (cleanKey.length() == 1 ? "0" + cleanKey : cleanKey);
            } else if (cleanKey.startsWith("XD") && !trimmed.contains("-") && cleanKey.length() > 2) {
                displayVal = "XD-" + cleanKey.substring(2);
            } else {
                displayVal = trimmed.toUpperCase(Locale.ROOT);
            }
            return new ValidationResult(true, "Hợp lệ (Phương tiện không biển số)", cleanKey, displayVal);
        }

        // 1. Chặn trường hợp chỉ gõ toàn số (lỗi người dùng gõ đại các số)
        if (cleanKey.matches("^\\d+$")) {
            return new ValidationResult(false, 
                "Biển số xe không hợp lệ: Không thể chỉ nhập toàn chữ số (" + cleanKey + ")! Biển số xe Việt Nam bắt buộc phải có mã tỉnh và sê-ri chữ cái (Ví dụ: 30A-123.45 hoặc 29B1-888.88).", 
                cleanKey, "");
        }

        // 2. Chặn trường hợp chỉ gõ toàn chữ cái
        if (cleanKey.matches("^[A-Z]+$")) {
            return new ValidationResult(false, 
                "Biển số xe không hợp lệ: Không thể chỉ nhập chữ cái! Biển số xe phải bao gồm số đăng ký (Ví dụ: 30A-123.45 hoặc 29B1-888.88).", 
                cleanKey, "");
        }

        // 3. Kiểm tra độ dài hợp lệ (tối thiểu 6 ký tự sạch, tối đa 11 ký tự sạch)
        if (cleanKey.length() < 6) {
            return new ValidationResult(false, 
                "Biển số xe quá ngắn (" + cleanKey.length() + " ký tự)! Biển số xe hợp lệ phải có từ 6 đến 11 ký tự (Ví dụ: 29A-1234 hoặc 30A-123.45).", 
                cleanKey, "");
        }
        if (cleanKey.length() > 11) {
            return new ValidationResult(false, 
                "Biển số xe quá dài (" + cleanKey.length() + " ký tự)! Vui lòng kiểm tra lại nội dung biển số.", 
                cleanKey, "");
        }

        // 4. Kiểm tra 2 ký tự đầu
        char c0 = cleanKey.charAt(0);
        char c1 = cleanKey.charAt(1);
        boolean startsWithProvince = Character.isDigit(c0) && Character.isDigit(c1);
        boolean startsWithLetters = Character.isLetter(c0) && Character.isLetter(c1);

        if (!startsWithProvince && !startsWithLetters) {
            return new ValidationResult(false, 
                "Biển số không đúng định dạng! 2 ký tự đầu phải là 2 chữ số mã tỉnh (VD: 29, 30, 51...) hoặc 2 chữ cái xe chuyên dụng/quân sự.", 
                cleanKey, "");
        }

        // 5. Nếu bắt đầu bằng 2 số mã tỉnh, ký tự thứ 3 BẮT BUỘC phải là chữ cái (sê-ri đăng ký)
        if (startsWithProvince) {
            char c2 = cleanKey.charAt(2);
            if (!Character.isLetter(c2)) {
                return new ValidationResult(false, 
                    "Biển số xe không hợp lệ: Sau mã tỉnh (2 chữ số đầu) bắt buộc phải là sê-ri chữ cái (Ví dụ: 30A-123.45 hoặc 29B1-888.88).", 
                    cleanKey, "");
            }
        }

        // 6. Kiểm tra nhóm số đăng ký cuối cùng (phải có ít nhất 4 chữ số ở đuôi)
        int trailingDigits = 0;
        for (int i = cleanKey.length() - 1; i >= 0; i--) {
            if (Character.isDigit(cleanKey.charAt(i))) {
                trailingDigits++;
            } else {
                break;
            }
        }
        if (trailingDigits < 4) {
            return new ValidationResult(false, 
                "Biển số không hợp lệ: Nhóm số đăng ký ở cuối phải có từ 4 đến 5 chữ số (Ví dụ: 30A-123.45 hoặc 29B1-8888).", 
                cleanKey, "");
        }

        String display = toDisplayFormat(trimmed, loaiXeTen);
        return new ValidationResult(true, "Hợp lệ", cleanKey, display);
    }

    public static ValidationResult validate(String rawInput) {
        return validate(rawInput, null);
    }

    /**
     * BƯỚC 3: Trình bày biển số theo định dạng hiển thị phù hợp với loại phương tiện (Canonical display format).
     * Hỗ trợ cách trình bày có dấu chấm phân cách cho nhóm 5 số (dạng 123.45) theo Thông tư 79/2024/TT-BCA,
     * đồng thời tương thích định dạng có dấu gạch ngang truyền thống.
     */
    public static String toDisplayFormat(String rawInput, String loaiXeTen) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return "";
        }

        String trimmed = rawInput.trim();
        String cleanKey = toCleanKey(trimmed);

        boolean isBicycle = (loaiXeTen != null && (loaiXeTen.toLowerCase().contains("đạp") || loaiXeTen.toLowerCase().contains("bicycle")))
                || cleanKey.startsWith("XD") || cleanKey.startsWith("XEDAP");
        if (isBicycle) {
            if (cleanKey.matches("^\\d+$")) {
                return "XD-" + (cleanKey.length() == 1 ? "0" + cleanKey : cleanKey);
            }
            if (cleanKey.startsWith("XD") && !trimmed.contains("-") && cleanKey.length() > 2) {
                return "XD-" + cleanKey.substring(2);
            }
            return trimmed.toUpperCase(Locale.ROOT);
        }

        if (cleanKey.length() < 5) {
            return trimmed.toUpperCase(Locale.ROOT);
        }

        boolean isCar = loaiXeTen != null && (loaiXeTen.toLowerCase().contains("tô") || loaiXeTen.toLowerCase().contains("car") || loaiXeTen.toLowerCase().contains("tải"));
        boolean isMoto = loaiXeTen != null && (loaiXeTen.toLowerCase().contains("máy") || loaiXeTen.toLowerCase().contains("moto") || loaiXeTen.toLowerCase().contains("đạp"));

        // TRƯỜNG HỢP 1: XE MÁY (Ưu tiên các mẫu xe máy nếu loại xe là xe máy)
        if (isMoto) {
            // Xe máy 5 số chuẩn: 29B1-888.88
            Matcher mMoto5 = PATTERN_MOTO_5_STANDARD.matcher(cleanKey);
            if (mMoto5.matches()) {
                return mMoto5.group(1) + mMoto5.group(2) + "-" + mMoto5.group(3) + "." + mMoto5.group(4);
            }

            // Xe máy 4 số cũ: 29B1-1234, 33M2-1234
            Matcher mMoto4 = PATTERN_MOTO_4.matcher(cleanKey);
            if (mMoto4.matches()) {
                return mMoto4.group(1) + mMoto4.group(2) + "-" + mMoto4.group(3);
            }

            // Xe máy điện / xe <50cc 5 số: 29MD-123.45
            Matcher mMotoElectric = PATTERN_MOTO_5_ELECTRIC.matcher(cleanKey);
            if (mMotoElectric.matches()) {
                return mMotoElectric.group(1) + mMotoElectric.group(2) + "-" + mMotoElectric.group(3) + "." + mMotoElectric.group(4);
            }
        }

        // TRƯỜNG HỢP 2: Ô TÔ (Ưu tiên các mẫu ô tô nếu loại xe là ô tô)
        if (isCar) {
            // Ô tô 5 số sê-ri 1 chữ cái: 30A-123.45
            Matcher mCar5 = PATTERN_CAR_5_SINGLE.matcher(cleanKey);
            if (mCar5.matches()) {
                return mCar5.group(1) + mCar5.group(2) + "-" + mCar5.group(3) + "." + mCar5.group(4);
            }

            // Ô tô 5 số sê-ri 2 chữ cái: 51LD-123.45
            Matcher mCar5Double = PATTERN_CAR_5_DOUBLE.matcher(cleanKey);
            if (mCar5Double.matches()) {
                return mCar5Double.group(1) + mCar5Double.group(2) + "-" + mCar5Double.group(3) + "." + mCar5Double.group(4);
            }

            // Ô tô 4 số cũ: 29A-1234, 52Z-9999
            Matcher mCar4 = PATTERN_CAR_4.matcher(cleanKey);
            if (mCar4.matches()) {
                return mCar4.group(1) + mCar4.group(2) + "-" + mCar4.group(3);
            }
        }

        // TRƯỜNG HỢP 3: CHƯA RÕ LOẠI XE (Suy luận từ cấu trúc ký tự)
        // 1. Xe máy 5 số thông thường: Sê-ri 1 chữ + 1 số: \d{2}[A-Z]\d\d{5} -> 29B1-888.88
        Matcher mMoto5 = PATTERN_MOTO_5_STANDARD.matcher(cleanKey);
        if (mMoto5.matches()) {
            return mMoto5.group(1) + mMoto5.group(2) + "-" + mMoto5.group(3) + "." + mMoto5.group(4);
        }

        // 2. Ô tô 5 số thông thường: Sê-ri 1 chữ: \d{2}[A-Z]\d{5} -> 30A-123.45
        Matcher mCar5 = PATTERN_CAR_5_SINGLE.matcher(cleanKey);
        if (mCar5.matches()) {
            return mCar5.group(1) + mCar5.group(2) + "-" + mCar5.group(3) + "." + mCar5.group(4);
        }

        // 3. Sê-ri 2 chữ cái 5 số: 51LD-123.45 hoặc 29MD-123.45
        Matcher mDouble5 = PATTERN_CAR_5_DOUBLE.matcher(cleanKey);
        if (mDouble5.matches()) {
            return mDouble5.group(1) + mDouble5.group(2) + "-" + mDouble5.group(3) + "." + mDouble5.group(4);
        }

        // 4. Xe máy 4 số cũ: 29B1-1234, 33M2-1234
        Matcher mMoto4 = PATTERN_MOTO_4.matcher(cleanKey);
        if (mMoto4.matches()) {
            return mMoto4.group(1) + mMoto4.group(2) + "-" + mMoto4.group(3);
        }

        // 5. Ô tô 4 số cũ: 29A-1234, 52Z-9999
        Matcher mCar4 = PATTERN_CAR_4.matcher(cleanKey);
        if (mCar4.matches()) {
            return mCar4.group(1) + mCar4.group(2) + "-" + mCar4.group(3);
        }

        // 6. Trường hợp người dùng nhập có cấu trúc riêng hoặc biển đặc thù:
        // Chuẩn hóa viết hoa và khoảng cách, giữ nguyên nội dung đăng ký mà không làm thay đổi ký tự
        String formatted = trimmed.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        return formatted;
    }

    public static String toDisplayFormat(String rawInput) {
        return toDisplayFormat(rawInput, null);
    }

    /**
     * So sánh 2 biển số có đại diện cho cùng một phương tiện hay không.
     * Không phụ thuộc vào dấu gạch nối, dấu chấm, khoảng trắng hay chữ hoa/thường.
     */
    public static boolean isSamePlate(String plateA, String plateB) {
        if (plateA == null || plateB == null) return false;
        String cleanA = toCleanKey(plateA);
        String cleanB = toCleanKey(plateB);
        if (cleanA.isEmpty() || cleanB.isEmpty()) return false;
        return cleanA.equals(cleanB);
    }

    /**
     * Kiểm tra biển số có khớp với từ khóa tìm kiếm hay không.
     * Hỗ trợ tìm kiếm cả chuỗi gốc và chuỗi đã chuẩn hóa (clean key).
     */
    public static boolean matchesSearch(String plate, String keyword) {
        if (plate == null || keyword == null) return false;
        String trimmedKw = keyword.trim();
        if (trimmedKw.isEmpty()) return true;

        String rawPlate = plate.toLowerCase(Locale.ROOT);
        String rawKw = trimmedKw.toLowerCase(Locale.ROOT);
        if (rawPlate.contains(rawKw)) return true;

        String cleanPlate = toCleanKey(plate);
        String cleanKw = toCleanKey(trimmedKw);
        return !cleanKw.isEmpty() && cleanPlate.contains(cleanKw);
    }

    /**
     * Tự động suy luận loại xe từ cấu trúc biển số xe Việt Nam.
     * @return "XE_MAY" nếu là xe máy, "O_TO" nếu là ô tô, hoặc null nếu không xác định được.
     */
    public static String inferVehicleType(String rawOrClean) {
        if (rawOrClean == null) return null;
        String clean = toCleanKey(rawOrClean);
        if (clean.length() < 5) return null;

        if (PATTERN_MOTO_5_STANDARD.matcher(clean).matches() || 
            PATTERN_MOTO_4.matcher(clean).matches() || 
            PATTERN_MOTO_5_ELECTRIC.matcher(clean).matches()) {
            return "XE_MAY";
        }

        if (PATTERN_CAR_5_SINGLE.matcher(clean).matches() || 
            PATTERN_CAR_4.matcher(clean).matches() || 
            PATTERN_CAR_5_DOUBLE.matcher(clean).matches()) {
            return "O_TO";
        }

        // Sau 2 số mã tỉnh: nếu là 1 chữ + 1 số (VD: 29B1...) -> Xe máy
        if (clean.matches("^\\d{2}[A-Z]\\d.*")) {
            return "XE_MAY";
        }

        // Sau 2 số mã tỉnh: nếu là 1 chữ + 4 hoặc 5 số (VD: 30A12345...) -> Ô tô
        if (clean.matches("^\\d{2}[A-Z]\\d{4,5}$")) {
            return "O_TO";
        }

        return null;
    }

    private static final Map<String, String> PROVINCE_MAP = new HashMap<>();
    static {
        PROVINCE_MAP.put("11", "Cao Bằng");
        PROVINCE_MAP.put("12", "Lạng Sơn");
        PROVINCE_MAP.put("14", "Quảng Ninh");
        PROVINCE_MAP.put("15", "Hải Phòng");
        PROVINCE_MAP.put("16", "Hải Phòng");
        PROVINCE_MAP.put("17", "Thái Bình");
        PROVINCE_MAP.put("18", "Nam Định");
        PROVINCE_MAP.put("19", "Phú Thọ");
        PROVINCE_MAP.put("20", "Thái Nguyên");
        PROVINCE_MAP.put("21", "Yên Bái");
        PROVINCE_MAP.put("22", "Tuyên Quang");
        PROVINCE_MAP.put("23", "Hà Giang");
        PROVINCE_MAP.put("24", "Lào Cai");
        PROVINCE_MAP.put("25", "Lai Châu");
        PROVINCE_MAP.put("26", "Sơn La");
        PROVINCE_MAP.put("27", "Điện Biên");
        PROVINCE_MAP.put("28", "Hòa Bình");
        PROVINCE_MAP.put("29", "Hà Nội");
        PROVINCE_MAP.put("30", "Hà Nội");
        PROVINCE_MAP.put("31", "Hà Nội");
        PROVINCE_MAP.put("32", "Hà Nội");
        PROVINCE_MAP.put("33", "Hà Nội");
        PROVINCE_MAP.put("40", "Hà Nội");
        PROVINCE_MAP.put("34", "Hải Dương");
        PROVINCE_MAP.put("35", "Ninh Bình");
        PROVINCE_MAP.put("36", "Thanh Hóa");
        PROVINCE_MAP.put("37", "Nghệ An");
        PROVINCE_MAP.put("38", "Hà Tĩnh");
        PROVINCE_MAP.put("41", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("43", "Đà Nẵng");
        PROVINCE_MAP.put("47", "Đắk Lắk");
        PROVINCE_MAP.put("48", "Đắk Nông");
        PROVINCE_MAP.put("49", "Lâm Đồng");
        PROVINCE_MAP.put("50", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("51", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("52", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("53", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("54", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("55", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("56", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("57", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("58", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("59", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("60", "Đồng Nai");
        PROVINCE_MAP.put("61", "Bình Dương");
        PROVINCE_MAP.put("62", "Long An");
        PROVINCE_MAP.put("63", "Tiền Giang");
        PROVINCE_MAP.put("64", "Vĩnh Long");
        PROVINCE_MAP.put("65", "Cần Thơ");
        PROVINCE_MAP.put("66", "Đồng Tháp");
        PROVINCE_MAP.put("67", "An Giang");
        PROVINCE_MAP.put("68", "Kiên Giang");
        PROVINCE_MAP.put("69", "Cà Mau");
        PROVINCE_MAP.put("70", "Tây Ninh");
        PROVINCE_MAP.put("71", "Bến Tre");
        PROVINCE_MAP.put("72", "Bà Rịa - Vũng Tàu");
        PROVINCE_MAP.put("73", "Quảng Bình");
        PROVINCE_MAP.put("74", "Quảng Trị");
        PROVINCE_MAP.put("75", "Thừa Thiên Huế");
        PROVINCE_MAP.put("76", "Quảng Ngãi");
        PROVINCE_MAP.put("77", "Bình Định");
        PROVINCE_MAP.put("78", "Phú Yên");
        PROVINCE_MAP.put("79", "Khánh Hòa");
        PROVINCE_MAP.put("81", "Gia Lai");
        PROVINCE_MAP.put("82", "Kon Tum");
        PROVINCE_MAP.put("83", "Sóc Trăng");
        PROVINCE_MAP.put("84", "Trà Vinh");
        PROVINCE_MAP.put("85", "Ninh Thuận");
        PROVINCE_MAP.put("86", "Bình Thuận");
        PROVINCE_MAP.put("88", "Vĩnh Phúc");
        PROVINCE_MAP.put("89", "Hưng Yên");
        PROVINCE_MAP.put("90", "Hà Nam");
        PROVINCE_MAP.put("92", "Quảng Nam");
        PROVINCE_MAP.put("93", "Bình Phước");
        PROVINCE_MAP.put("94", "Bạc Liêu");
        PROVINCE_MAP.put("95", "Hậu Giang");
        PROVINCE_MAP.put("97", "Bắc Kạn");
        PROVINCE_MAP.put("98", "Bắc Giang");
        PROVINCE_MAP.put("99", "Bắc Ninh");
    }

    /**
     * Tự động tra cứu Tỉnh / Thành phố phát hành biển số
     */
    public static String getProvinceName(String rawOrClean) {
        if (rawOrClean == null) return null;
        String clean = toCleanKey(rawOrClean);
        if (clean.length() < 2) return null;
        String code = clean.substring(0, 2);
        return PROVINCE_MAP.get(code);
    }

    /**
     * Nhận diện biển số đặc biệt (VIP, Ngũ quý, Tứ quý, Sảnh tiến, Lộc phát, Thần tài)
     */
    public static String getSpecialPlateDescription(String rawOrClean) {
        if (rawOrClean == null) return null;
        String clean = toCleanKey(rawOrClean);
        if (clean.length() < 6) return null;

        Matcher matcher = Pattern.compile("(\\d{4,5})$").matcher(clean);
        if (!matcher.find()) return null;
        String digits = matcher.group(1);

        // 1. Ngũ quý (5 số giống nhau, VD: 88888, 99999)
        if (digits.length() == 5 && digits.charAt(0) == digits.charAt(1) && 
            digits.charAt(1) == digits.charAt(2) && digits.charAt(2) == digits.charAt(3) && 
            digits.charAt(3) == digits.charAt(4)) {
            return "Biển VIP Ngũ Quý " + digits.charAt(0);
        }

        // 2. Tứ quý (4 số cuối giống nhau, VD: 8888, 9999)
        if (digits.length() >= 4) {
            String last4 = digits.substring(digits.length() - 4);
            if (last4.charAt(0) == last4.charAt(1) && last4.charAt(1) == last4.charAt(2) && 
                last4.charAt(2) == last4.charAt(3)) {
                return "Biển VIP Tứ Quý " + last4.charAt(0);
            }
        }

        // 3. Sảnh tiến (VD: 12345, 56789, 2345, 6789)
        boolean isAscending = true;
        for (int i = 0; i < digits.length() - 1; i++) {
            if (digits.charAt(i + 1) - digits.charAt(i) != 1) {
                isAscending = false;
                break;
            }
        }
        if (isAscending && digits.length() >= 4) {
            return "Biển Sảnh Tiến (" + digits + ")";
        }

        // 4. Lộc phát (kết thúc 68 hoặc 86)
        if (digits.endsWith("6868") || digits.endsWith("8686")) {
            return "Biển Lộc Phát Kép";
        } else if (digits.endsWith("68") || digits.endsWith("86")) {
            return "Biển Lộc Phát";
        }

        // 5. Thần tài (39 hoặc 79)
        if (digits.endsWith("7979") || digits.endsWith("3939")) {
            return "Biển Thần Tài Kép";
        } else if (digits.endsWith("79") || digits.endsWith("39")) {
            return "Biển Thần Tài";
        }

        // 6. Tam hoa (3 số cuối giống nhau)
        if (digits.length() >= 3) {
            String last3 = digits.substring(digits.length() - 3);
            if (last3.charAt(0) == last3.charAt(1) && last3.charAt(1) == last3.charAt(2)) {
                return "Biển Tam Hoa " + last3.charAt(0);
            }
        }

        return null;
    }
}
