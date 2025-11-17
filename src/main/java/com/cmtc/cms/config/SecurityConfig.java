package com.cmtc.cms.config;

import com.cmtc.cms.security.JwtAuthenticationFilter;
import com.cmtc.cms.serviceImpl.UserDetailsServiceImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableMethodSecurity(prePostEnabled = true) // This enables @PreAuthorize annotations
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;

	public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter, PasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.passwordEncoder = passwordEncoder;
	}

	private final String[] frontendRequests = new String[] {
			// 🌐 Base React routes
			"/", "/index.html", "/favicon.ico", "/manifest.json",
			"/static/**", "/assets/**", "/logo*.png", "/robots.txt","vite.svg",

			// 🧭 Frontend React Router paths (SPA routes)
			"/news/**",
			"/circulars/**",
			"/tenders/**",
			"/pages/**",
			"/gallery/**",
			"/books/**",
			"/login/**",
			"/signup/**",
			"/forgot-password/**",
			"/reset-password/**",
			"/profile/**",

			// 🧰 CMS/Admin React routes (frontend-managed views)
			"/cms/**",
			"/admin/**",

	};


	@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            	    // Public static content (e.g., frontend build assets)
            	    .requestMatchers("/", "/index.html", "/favicon.ico", "/static/**", "/assets/**", "/uploads/**").permitAll()
					.requestMatchers(frontendRequests).permitAll()
                    .requestMatchers("/error").permitAll() // Explicitly permit /error path
                    //------Public (rojgar aur nirman)
                    .requestMatchers("/api/rojgar-nirman/public/**").permitAll()
                    //------Admin Area (based on roles) (rojgar aur nirman)
            	    // Public API access (endpoints that do NOT require authentication)
            	    .requestMatchers(
            	    	 "api/admin/users/user/**", 
            	        "/api/auth/**",// Login, forgot password, reset password
            	        "/auth/public/**",// normal registration
            	        "/api/public/**", // General public APIs
            	        "/api/news/public/**",
            	        "/api/circulars/public/**",
            	        "/api/tenders/public/**",
                        "/api/gallery/public/**",
                        "/api/gallery/categories", // <--- ADDED THIS LINE
                        "/api/contact/submit",
                        "/files/**", // For serving uploaded files
                        "/api/v1/menu/**",
                        "/api/rojgar-nirman/public/**",
                        "api/marquee-item/public/fetch/all",
                        "/api/carousel/public/**",
                        "/actuator/**"

            	    ).permitAll()
            	    
                    // ----------- Public Area for Marquee Items -----------
            	    .requestMatchers("/api/marquee-item/public/fetch/all").permitAll()
            	    .requestMatchers("/api/marquee-item/public/fetch/**").permitAll()
            	    // ----------- Public Area for RTI Document -----------
            	    .requestMatchers("/api/rti-document/public/fetch-approved-visible/all").permitAll()
            	    .requestMatchers("/api/rti-document/public/fetch/{id}").permitAll()
            	    .requestMatchers("/api/rti-document/public/fetch/all").permitAll()
            	    
            	    // ----------- Public Area for Category master -----------
            	    .requestMatchers("/api/categories/**").permitAll()            	                	    
                    // Swagger/dev access (usually permitted for development/documentation)
            	    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()

                    // Explicitly define roles for specific admin/cms endpoints at the filter chain level
                    // Changed hasAnyRole to hasAnyAuthority and added "ROLE_" prefix
                    .requestMatchers("/api/analytics/admin/dashboard-stats").hasAnyAuthority("ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN", "ROLE_EDITOR")
                    .requestMatchers("/api/news/admin/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/circulars/admin/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/tender/admin/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/users/admin/**").hasAnyAuthority("ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/approval/admin/**").hasAnyAuthority("ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN") 
                   

                    // ----------- Admin Area for Marquee Item (based on roles) -----------
                    .requestMatchers("/api/marquee-item/admin/fetch/all").hasAnyAuthority("ROLE_EDITOR","ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/marquee-item/admin/fetch/**").hasAnyAuthority("ROLE_EDITOR","ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/marquee-item/admin/create").hasAnyAuthority("ROLE_EDITOR","ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/marquee-item/admin/edit/**").hasAnyAuthority("ROLE_EDITOR","ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/marquee-item/admin/approve/**").hasAnyAuthority("ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                    .requestMatchers("/api/marquee-item/admin/delete/**").hasAnyAuthority("ROLE_PUBLISHER", "ROLE_PORTAL_ADMIN")
                 
                    // All other requests (that haven't been matched above) require authentication
            	    .anyRequest().authenticated()
            	);

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
