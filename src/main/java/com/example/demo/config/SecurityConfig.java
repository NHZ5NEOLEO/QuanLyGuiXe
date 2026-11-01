package com.example.demo.config;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final NguoiDungRepository nguoiDungRepository;

    public SecurityConfig(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    /**
     * Cấu hình phân quyền chặt chẽ:
     * - Trang công khai (portal, tra cứu, bảng giá, đăng ký vé tháng portal, xem QR): Khách hàng truy cập được
     * - Các API có thể tạo lượt gửi, xác nhận checkout, cập nhật vé, báo cáo hoặc dữ liệu quản trị:
     *   bắt buộc phải đăng nhập và đúng vai trò (ADMIN hoặc NHAN_VIEN)
     * - Trang quản trị chuyên sâu (nhân viên, bảng giá, cấu hình, báo cáo doanh thu, phương tiện): chỉ ADMIN
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/portal/api/**", "/login", "/dang-nhap", "/logout", "/dang-xuat", "/quen-mat-khau", "/quen-mat-khau/**", "/forgot-password", "/forgot-password/**", "/tai-khoan/**", "/portal/ho-so/**")
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Static resources & assets
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // 2. Authentication & Error
                .requestMatchers("/login", "/logout", "/dang-nhap", "/dang-xuat", "/dang-ky", "/register", "/quen-mat-khau", "/quen-mat-khau/**", "/forgot-password", "/forgot-password/**").permitAll()
                .requestMatchers("/api/auth/**", "/actuator/**", "/error").permitAll()
                // 3. Quản lý Hồ sơ cá nhân & Tài khoản (Bắt buộc phải đăng nhập)
                .requestMatchers("/tai-khoan", "/tai-khoan/**", "/ho-so", "/ho-so/**", "/portal/ho-so", "/portal/ho-so/**", "/portal/profile").authenticated()
                // 4. Trang và API công khai cho khách hàng / cư dân
                .requestMatchers("/", "/home", "/portal", "/portal/**", "/cong-thong-tin", "/cong-thong-tin/**").permitAll()
                .requestMatchers("/tra-cuu", "/tra-cuu/**", "/tra-cuu-xe", "/tra-cuu-xe/**", "/portal/api/lookup").permitAll()
                .requestMatchers("/bang-gia-dich-vu", "/bang-gia-dich-vu/**", "/dich-vu/**").permitAll()
                .requestMatchers("/dang-ky-ve-thang", "/dang-ky-ve-thang/**", "/portal/api/register-monthly").permitAll()
                .requestMatchers("/danh-gia", "/danh-gia/**", "/portal/api/feedback", "/portal/api/feedbacks").permitAll()
                .requestMatchers("/qr-entry", "/qr-exit", "/qr-cong-vao", "/qr-cong-ra", "/customer-checkin", "/customer-checkout", "/khach-gui-xe", "/khach-nhan-xe", "/khach-xe-vao", "/khach-xe-ra").permitAll()
                // 4. API tra cứu công khai (chỉ xem, không làm thay đổi trạng thái bãi xe hay tạo lượt gửi)
                .requestMatchers("/api/gate/check-plate", "/api/xe-vao/check-plate", "/api/check-plate").permitAll()
                .requestMatchers("/api/gate/qr-checkout-preview", "/api/parking/preview-checkout").permitAll()
                .requestMatchers("/api/gate/check-qr-paid", "/api/parking/qr/**").permitAll()

                // 5. Trang và API chỉ dành riêng cho Quản trị viên (ADMIN)
                .requestMatchers("/staff/**", "/nhan-vien/**").hasRole("ADMIN")
                .requestMatchers("/price-settings/**", "/bang-gia/**").hasRole("ADMIN")
                .requestMatchers("/settings/**", "/cai-dat/**").hasRole("ADMIN")
                .requestMatchers("/reports/**", "/thong-ke/**", "/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/phuong-tien/**", "/vehicles/**", "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers("/tong-quan", "/tong-quan/**", "/dashboard", "/dashboard/**", "/index").hasRole("ADMIN")

                // 6. Trang & API Nghiệp vụ trực cổng & Vận hành bãi đỗ (ADMIN + NHAN_VIEN)
                // Nghiêm ngặt: Tất cả API tạo lượt gửi xe (check-in) và xác nhận checkout phải yêu cầu nhân viên/admin đăng nhập
                .requestMatchers("/gui-xe", "/gui-xe/**", "/nhan-xe", "/nhan-xe/**", "/xe-vao", "/xe-vao/**", "/xe-ra", "/xe-ra/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/gate/**", "/check-in/**", "/check-out/**", "/checkin", "/checkout/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/api/gate/check-in", "/api/gate/qr-checkin", "/api/gate/check-out/confirm").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/api/parking/check-in", "/api/parking/check-out").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/active-vehicles/**", "/xe-trong-bai/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/history/**", "/monthly-tickets/**", "/ve-thang/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/parking-map", "/parking-map/**", "/so-do-bai-xe", "/so-do-bai-xe/**", "/quan-ly-do-xe", "/quan-ly-do-xe/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/security-alerts/**", "/canh-bao-an-ninh/**", "/shift/**", "/ca-truc/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/api/tickets/**", "/api/parking/**", "/api/parking-map/**", "/api/gate/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                // 7. Mọi request khác cần xác thực đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String username = authentication.getName();
                    var userOpt = nguoiDungRepository.findByTenDangNhap(username);
                    String role = "ADMIN";
                    if (userOpt.isPresent()) {
                        NguoiDung nd = userOpt.get();
                        HttpSession session = request.getSession(true);
                        session.setAttribute("currentUser", nd);
                        session.setAttribute("userId", nd.getId());
                        session.setAttribute("userName", nd.getHoTen());
                        session.setAttribute("userRole", nd.getVaiTro());
                        session.setAttribute("userLogin", nd.getTenDangNhap());
                        role = nd.getVaiTro();
                    } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NHAN_VIEN"))) {
                        role = "NHAN_VIEN";
                        request.getSession().setAttribute("userRole", "NHAN_VIEN");
                    }

                    // Ưu tiên chuyển hướng về SavedRequest nếu người dùng đang truy cập một trang được bảo vệ
                    RequestCache requestCache = new HttpSessionRequestCache();
                    SavedRequest savedRequest = requestCache.getRequest(request, response);
                    if (savedRequest != null) {
                        String targetUrl = savedRequest.getRedirectUrl();
                        if (targetUrl != null 
                                && !targetUrl.contains("/login") 
                                && !targetUrl.contains("/dang-nhap")
                                && !targetUrl.endsWith(".css")
                                && !targetUrl.endsWith(".js")
                                && !targetUrl.endsWith(".ico")
                                && !targetUrl.endsWith(".png")
                                && !targetUrl.endsWith(".jpg")) {
                            response.sendRedirect(targetUrl);
                            return;
                        }
                    }

                    if ("NHAN_VIEN".equalsIgnoreCase(role)) {
                        response.sendRedirect("/gui-xe");
                    } else if ("KHACH_HANG".equalsIgnoreCase(role) || "CU_DAN".equalsIgnoreCase(role)) {
                        response.sendRedirect("/dang-ky-ve-thang");
                    } else {
                        response.sendRedirect("/tong-quan");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(rm -> rm
                .key("ParkingAppRememberMeSecretKey2026")
                .rememberMeParameter("rememberMe")
                .tokenValiditySeconds(86400 * 7)
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    org.springframework.security.core.Authentication auth = 
                            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_KHACH_HANG") || a.getAuthority().equals("ROLE_CU_DAN"))) {
                        response.sendRedirect("/dang-ky-ve-thang");
                    } else {
                        response.sendRedirect("/gui-xe?denied=true");
                    }
                })
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new OrRequestMatcher(
                    PathPatternRequestMatcher.pathPattern("/logout"),
                    PathPatternRequestMatcher.pathPattern("/dang-xuat")
                ))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            );

        return http.build();
    }

    /**
     * UserDetailsService tích hợp bảng NguoiDung
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            NguoiDung nguoiDung = nguoiDungRepository.findByTenDangNhap(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

            if (!Boolean.TRUE.equals(nguoiDung.getTrangThai())) {
                throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + username);
            }

            String role = "ROLE_" + nguoiDung.getVaiTro();

            return new User(
                    nguoiDung.getTenDangNhap(),
                    nguoiDung.getMatKhau(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        };
    }

    /**
     * Tự động nâng cấp mật khẩu từ plain text sang BCrypt hash khi người dùng đăng nhập thành công
     */
    @Bean
    public UserDetailsPasswordService userDetailsPasswordService() {
        return (user, newPassword) -> {
            nguoiDungRepository.findByTenDangNhap(user.getUsername()).ifPresent(nd -> {
                nd.setMatKhau(newPassword);
                nguoiDungRepository.save(nd);
            });
            return user;
        };
    }

    /**
     * Sử dụng BCryptPasswordEncoder với cơ chế tương thích ngược (Backward Compatibility)
     * cho phép tài khoản cũ dạng plain text vẫn đăng nhập bình thường và được nâng cấp tự động sang BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                if (rawPassword == null) return null;
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (rawPassword == null || encodedPassword == null) {
                    return false;
                }
                // Nếu đã là BCrypt hash ($2a$, $2b$, $2y$)
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
                // Tương thích ngược: kiểm tra so khớp mật khẩu plain text cũ
                return rawPassword.toString().equals(encodedPassword);
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                if (encodedPassword == null) return true;
                return !(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$"));
            }
        };
    }
}
