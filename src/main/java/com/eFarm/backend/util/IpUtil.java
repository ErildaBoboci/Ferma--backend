package com.eFarm.backend.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    /**
     * Merr IP address nga request-i
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = null;

        // Kontrollo headers të ndryshëm që mund të përmbajnë IP real
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            ipAddress = request.getHeader(header);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                // Nëse ka shumë IP (ndarë me presje), merr të parin
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.split(",")[0].trim();
                }
                break;
            }
        }

        // Fallback në IP-në direkte
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Konverto localhost IPv6 në IPv4
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        return ipAddress;
    }

    /**
     * Valido nëse IP është valid
     */
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // IPv4 validation
        if (ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            String[] parts = ip.split("\\.");
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        }

        // IPv6 basic validation (simplified)
        if (ip.contains(":")) {
            return ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$") ||
                    ip.equals("::1") || ip.contains("::");
        }

        return false;
    }

    /**
     * Maskimi i IP për privacy
     */
    public static String maskIpAddress(String ip) {
        if (!isValidIpAddress(ip)) {
            return "Unknown";
        }

        if (ip.contains(".")) { // IPv4
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".xxx.xxx";
            }
        } else if (ip.contains(":")) { // IPv6
            String[] parts = ip.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":xxxx:xxxx:xxxx:xxxx:xxxx:xxxx";
            }
        }

        return "xxx.xxx.xxx.xxx";
    }

    /**
     * Kontrollo nëse IP është nga rrjeti lokal
     */
    public static boolean isLocalNetwork(String ip) {
        if (!isValidIpAddress(ip)) {
            return false;
        }

        return ip.startsWith("127.") ||      // Localhost
                ip.startsWith("192.168.") ||  // Private Class C
                ip.startsWith("10.") ||       // Private Class A
                ip.startsWith("172.") ||      // Private Class B
                ip.equals("::1");             // IPv6 localhost
    }

    /**
     * Gjenero një përshkrim të shkurtër për IP
     */
    public static String getIpDescription(String ip) {
        if (!isValidIpAddress(ip)) {
            return "IP i pavlefshëm";
        }

        if (isLocalNetwork(ip)) {
            return "Rrjet lokal";
        }

        // Mund të shtosh më shumë logjikë për të identifikuar providers
        return "IP publik";
    }
}