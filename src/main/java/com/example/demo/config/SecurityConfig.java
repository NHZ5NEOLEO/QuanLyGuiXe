package com.example.demo.config;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final NguoiDungRepository nguoiDungRepository;

    public SecurityConfig(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    /**
     * Cấu hình phân quyền:
     * - Trang công khai (portal, gửi xe, nhận xe, QR): ai cũng truy cập được
     * - Trang quản trị (staff, price-settings, settings): chỉ ADMIN
     * - Trang nghiệp vụ (dashboard, gate, history, reports...): ADMIN + NHAN_VIEN
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/portal/api/**", "/login", "/logout", "/dang-xuat")
            )
            .authorizeHttpRequests(auth -> auth
                // Trang và API công khai cho khách hàng
                .requestMatchers("/", "/home", "/portal", "/portal/**", "/cong-thong-tin", "/cong-thong-tin/**").permitAll()
                .requestMatchers("/tra-cuu", "/tra-cuu/**", "/tra-cuu-xe", "/tra-cuu-xe/**").permitAll()
                .requestMatchers("/bang-gia-dich-vu", "/bang-gia-dich-vu/**", "/dich-vu/**").permitAll()
                .requestMatchers("/dang-ky-ve-thang", "/dang-ky-ve-thang/**").permitAll()
                .requestMatchers("/danh-gia", "/danh-gia/**").permitAll()
                .requestMatchers("/qr-entry", "/qr-exit", "/qr-cong-vao", "/qr-cong-ra", "/customer-checkin", "/customer-checkout", "/khach-gui-xe", "/khach-nhan-xe", "/khach-xe-vao", "/khach-xe-ra").permitAll()
                .requestMatchers("/api/gate/qr-checkin", "/api/gate/qr-checkout-preview", "/api/gate/check-plate", "/api/xe-vao/**", "/api/check-plate", "/api/ve-xe/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/parking/qr/**").permitAll()
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Login / Logout
                .requestMatchers("/login", "/logout", "/dang-nhap", "/dang-xuat").permitAll()
                // Actuator & Error
                .requestMatchers("/actuator/**", "/error").permitAll()
                // Trang chỉ dành riêng cho ADMIN (Quản trị, Cấu hình, Danh mục xe, Báo cáo tài chính & Dashboard)
                .requestMatchers("/staff/**", "/nhan-vien/**").hasRole("ADMIN")
                .requestMatchers("/price-settings/**", "/bang-gia/**").hasRole("ADMIN")
                .requestMatchers("/settings/**", "/cai-dat/**").hasRole("ADMIN")
                .requestMatchers("/reports/**", "/thong-ke/**", "/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/phuong-tien/**", "/vehicles/**", "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers("/tong-quan", "/tong-quan/**", "/dashboard", "/dashboard/**", "/index").hasRole("ADMIN")

                // Trang Nghiệp vụ trực cổng & Vận hành bãi đỗ (ADMIN + NHAN_VIEN)
                .requestMatchers("/gui-xe", "/gui-xe/**", "/nhan-xe", "/nhan-xe/**", "/xe-vao", "/xe-vao/**", "/xe-ra", "/xe-ra/**", "/parking/**", "/tickets/**", "/ve-xe/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/gate/**", "/check-in/**", "/check-out/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/checkin", "/checkout/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/active-vehicles/**", "/xe-trong-bai/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/history/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/monthly-tickets/**", "/ve-thang/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/parking-map/**", "/so-do-bai-xe/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/security-alerts/**", "/canh-bao-an-ninh/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/shift/**", "/ca-truc/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                .requestMatchers("/api/tickets/**", "/api/parking/**", "/api/gate/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                // Mọi request khác cần xác thực
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
                        request.getSession().setAttribute("userName", nd.getHoTen());
                        request.getSession().setAttribute("userRole", nd.getVaiTro());
                        role = nd.getVaiTro();
                    } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NHAN_VIEN"))) {
                        role = "NHAN_VIEN";
                        request.getSession().setAttribute("userRole", "NHAN_VIEN");
                    }
                    if ("NHAN_VIEN".equalsIgnoreCase(role)) {
                        response.sendRedirect("/gui-xe");
                    } else {
                        response.sendRedirect("/tong-quan");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/gui-xe?denied=true");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    /**
     * UserDetailsService tích hợp với bảng NguoiDung trong SQL Server.
     * Lưu ý: Project hiện tại lưu mật khẩu dạng plain text (phù hợp đồ án),
     * nên dùng NoOpPasswordEncoder để tương thích.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            NguoiDung nguoiDung = nguoiDungRepository.findByTenDangNhap(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

            if (!Boolean.TRUE.equals(nguoiDung.getTrangThai())) {
                throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + username);
            }

            // Map vai trò trong DB thành Spring Security role
            String role = "ROLE_" + nguoiDung.getVaiTro(); // ROLE_ADMIN hoặc ROLE_NHAN_VIEN

            return new User(
                    nguoiDung.getTenDangNhap(),
                    nguoiDung.getMatKhau(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        };
    }

    /**
     * Sử dụng NoOpPasswordEncoder vì mật khẩu trong DB lưu plain text.
     * Đây là giải pháp phù hợp cho đồ án chuyên ngành, tránh phải migrate dữ liệu hiện có.
     */
    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }
}
