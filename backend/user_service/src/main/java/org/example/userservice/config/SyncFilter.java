// package org.example.userservice.config;

// import java.io.IOException;
// import java.nio.file.DirectoryStream.Filter;

// import org.example.userservice.service.UserService;
// import org.springframework.core.annotation.Order;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// @Order(2)
// public class SyncFilter extends OncePerRequestFilter {

//     private final UserService userService;

//     public SyncFilter(UserService userService) {
//         this.userService = userService;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain) throws ServletException, IOException {

//         // Log the incoming request
//         System.out.println("SyncFilter is being executed for request: " + request.getRequestURI());

//         // Retrieve the Authentication object from the SecurityContext
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//         // Check if authentication exists
//         if (authentication != null) {
//             System.out.println("Authentication is available");

//             // Check if the user is authenticated and the principal is an instance of Jwt
//             if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt) {
//                 System.out.println("User is authenticated and principal is a JWT");
                
//                 // Get the JWT token and call syncUser to sync user data
//                 Jwt jwt = (Jwt) authentication.getPrincipal();
//                 userService.syncUser(jwt);

//             } else {
//                 System.out.println("Authentication is not authenticated or principal is not a JWT");
//             }
//         } else {
//             System.out.println("Authentication is null, possibly an issue with security context population.");
//         }

//         // Continue with the filter chain
//         filterChain.doFilter(request, response);
//     }
// }
