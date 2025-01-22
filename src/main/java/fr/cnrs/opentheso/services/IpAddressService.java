package fr.cnrs.opentheso.services;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class IpAddressService {

    private final HttpServletRequest request;

    public IpAddressService(HttpServletRequest request) {
        this.request = request;
    }

    public String getClientIpAddress() {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
