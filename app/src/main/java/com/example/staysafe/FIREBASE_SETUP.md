# StaySafe – Firebase setup checklist

1. **Authentication**: Firebase console > Authentication > Sign-in method > enable **Email/Password**.
   (The app turns each phone number + role into an email like `0880667711.guest@staysafe.app`.)
2. **Dependencies** (same BoM you already use): add `com.google.firebase:firebase-auth` next to `firebase-firestore`.
3. **Firestore rules** (Firestore > Rules). Fine for coursework; tighten before real use:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    match /hotels/{id}   { allow read, write: if request.auth != null; }
    match /rooms/{id}    { allow read, write: if request.auth != null; }
    match /bookings/{id} { allow read, write: if request.auth != null; }
  }
}
```
4. Old test bookings (saved before this change) have no `guestId`, so they no longer show in any guest's Bookings tab. Delete them in the console if you want a clean start.
