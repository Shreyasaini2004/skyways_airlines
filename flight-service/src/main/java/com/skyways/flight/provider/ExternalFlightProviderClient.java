package com.skyways.flight.provider;

import com.skyways.flight.dto.ProviderFlightResponse;
import java.time.LocalDate;
import java.util.List;

public interface ExternalFlightProviderClient {

    List<ProviderFlightResponse> search(String origin, String destination, LocalDate departureDate, int passengers);
}
