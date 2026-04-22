package com.closingassistant.data.repository

import com.closingassistant.data.model.ClientProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

sealed class ClientResult {
    data class Success(val clientId: String) : ClientResult()
    data class Error(val message: String) : ClientResult()
}

class ClientRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val clientsCollection
        get() = db.collection("agents")
            .document(auth.currentUser?.uid ?: "unknown")
            .collection("clients")

    suspend fun saveClient(profile: ClientProfile): ClientResult {
        return try {
            val agentId = auth.currentUser?.uid
                ?: return ClientResult.Error("User not authenticated")

            val profileWithAgent = profile.copy(agentId = agentId)

            val docRef = if (profile.id.isBlank()) {
                // New client
                clientsCollection.add(profileWithAgent).await()
            } else {
                // Update existing
                clientsCollection.document(profile.id).also { ref ->
                    ref.set(profileWithAgent).await()
                }
            }

            ClientResult.Success(docRef.id)
        } catch (e: Exception) {
            ClientResult.Error(e.message ?: "Failed to save client profile.")
        }
    }

    suspend fun getClients(): List<ClientProfile> {
        return try {
            clientsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(ClientProfile::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getClient(clientId: String): ClientProfile? {
        return try {
            clientsCollection
                .document(clientId)
                .get()
                .await()
                .toObject(ClientProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
