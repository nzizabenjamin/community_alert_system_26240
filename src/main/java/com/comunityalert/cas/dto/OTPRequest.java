package com.comunityalert.cas.dto;

public class OTPRequest {
    private String tempToken;
    private String otp;
    private String otpCode; // Support both field names for compatibility

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    
    public String getOtpCode() { return otpCode != null ? otpCode : otp; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
