package com.tiora.mob.dto.response;

public class JwtWithBarberResponse extends JwtResponse {
    private BarberAuthResponse barberDetails;

    public JwtWithBarberResponse(String token, boolean isProfileComplete, boolean customerExists, BarberAuthResponse barberDetails) {
        super(token, isProfileComplete, customerExists);
        this.barberDetails = barberDetails;
    }

    public JwtWithBarberResponse(String token, BarberAuthResponse barberDetails) {
        super(token, true, true); // For barbers, profile is always complete and exists
        this.barberDetails = barberDetails;
    }

    public BarberAuthResponse getBarberDetails() {
        return barberDetails;
    }

    public void setBarberDetails(BarberAuthResponse barberDetails) {
        this.barberDetails = barberDetails;
    }
}
