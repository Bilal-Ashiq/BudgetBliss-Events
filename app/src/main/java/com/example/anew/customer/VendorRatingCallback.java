package com.example.anew.customer;

import java.util.List;

public interface VendorRatingCallback {
    void onRatingFetched(List<Long> currentRatings);
}
