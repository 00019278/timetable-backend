package com.sarmich.timetable.model;

import java.util.UUID;

public record FirebaseVerificationRequest(
    String firebaseAuthToken, UUID uuid, String firebaseToken) {

  public FirebaseVerificationRequest {}
}
