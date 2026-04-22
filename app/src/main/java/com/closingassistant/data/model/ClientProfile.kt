package com.closingassistant.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ClientProfile(
    @DocumentId
    val id: String = "",
    val agentId: String = "",
    val age: Int = 0,
    val monthlyIncome: Double = 0.0,
    val numberOfDependents: Int = 0,
    val financialGoals: String = "",
    val concerns: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // No-arg constructor required by Firestore
    constructor() : this("", "", 0, 0.0, 0, "", "", null)
}
