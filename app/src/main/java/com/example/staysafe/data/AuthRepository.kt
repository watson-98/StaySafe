package com.example.staysafe.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.staysafe.ui.login.UserRole
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

// profile stored in Firestore: users/{uid}
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.Guest,
    val hotelName: String = "",   // hotel owners only
    val city: String = ""         // hotel owners only
)

const val LOGIN_ERROR = "Password or phone number incorrect"
const val MIN_PASSWORD_LENGTH = 6

object AuthRepository {

    private const val TAG = "AuthRepository"
    private const val EMAIL_DOMAIN = "staysafe.app"
    private const val GENERIC_ERROR = "Something went wrong. Please try again."
    private const val NETWORK_ERROR = "No internet connection. Please try again."
    private const val PHONE_TAKEN_ERROR = "An account with this phone number already exists"

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    var currentUser by mutableStateOf<UserProfile?>(null)
        private set

    private fun emailFor(phone: String, role: UserRole): String {
        val suffix = if (role == UserRole.HotelOwner) "owner" else "guest"
        return "${phone.trim()}.$suffix@$EMAIL_DOMAIN"
    }

    fun signUp(
        fullName: String,
        phone: String,
        password: String,
        role: UserRole,
        hotelName: String,
        city: String,
        onResult: (UserProfile?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(emailFor(phone, role), password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    onResult(null, GENERIC_ERROR)
                    return@addOnSuccessListener
                }
                val profile = UserProfile(
                    uid = firebaseUser.uid,
                    fullName = fullName,
                    phone = phone,
                    role = role,
                    hotelName = hotelName,
                    city = city
                )
                db.collection("users").document(profile.uid).set(profile.toMap())
                    .addOnSuccessListener {
                        currentUser = profile
                        onResult(profile, null)
                    }
                    .addOnFailureListener { e ->
                        // don't leave an account behind without a profile
                        Log.e(TAG, "Could not save profile", e)
                        firebaseUser.delete()
                        auth.signOut()
                        onResult(null, GENERIC_ERROR)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Sign up failed", e)
                onResult(null, messageFor(e))
            }
    }

    fun login(
        phone: String,
        password: String,
        role: UserRole,
        onResult: (UserProfile?, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(emailFor(phone, role), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onResult(null, GENERIC_ERROR)
                    return@addOnSuccessListener
                }
                loadProfile(uid) { profile ->
                    if (profile == null) {
                        auth.signOut()
                        onResult(null, GENERIC_ERROR)
                    } else {
                        currentUser = profile
                        onResult(profile, null)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Login failed", e)
                onResult(null, messageFor(e))
            }
    }

    // Firebase keeps the session between app launches
    fun restoreSession(onDone: (UserProfile?) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onDone(null)
            return
        }
        loadProfile(firebaseUser.uid) { profile ->
            currentUser = profile
            onDone(profile)
        }
    }

    fun logout() {
        auth.signOut()
        currentUser = null
    }

    private fun loadProfile(uid: String, onLoaded: (UserProfile?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onLoaded(null)
                    return@addOnSuccessListener
                }
                val role = if (doc.getString("role") == UserRole.HotelOwner.name) {
                    UserRole.HotelOwner
                } else {
                    UserRole.Guest
                }
                onLoaded(
                    UserProfile(
                        uid = uid,
                        fullName = doc.getString("fullName") ?: "",
                        phone = doc.getString("phone") ?: "",
                        role = role,
                        hotelName = doc.getString("hotelName") ?: "",
                        city = doc.getString("city") ?: ""
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Could not load profile", e)
                onLoaded(null)
            }
    }

    private fun UserProfile.toMap() = hashMapOf(
        "fullName" to fullName,
        "phone" to phone,
        "role" to role.name,
        "hotelName" to hotelName,
        "city" to city
    )

    private fun messageFor(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException -> PHONE_TAKEN_ERROR
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> LOGIN_ERROR
        is FirebaseNetworkException -> NETWORK_ERROR
        else -> GENERIC_ERROR
    }
}
