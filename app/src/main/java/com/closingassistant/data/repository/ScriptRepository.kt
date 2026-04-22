package com.closingassistant.data.repository

import com.closingassistant.data.model.*

class ScriptRepository {

    /**
     * Generates a complete Taglish (Tagalog + English) sales script flow
     * based on the client's profile data.
     */
    fun generateSalesSteps(profile: com.closingassistant.data.model.ClientProfile): List<SalesStep> {
        val firstName = "ka" // Generic address; in a real app, collect client name
        val incomeFormatted = "₱${formatIncome(profile.monthlyIncome)}"
        val hasDependents = profile.numberOfDependents > 0
        val dependentsPhrase = if (hasDependents)
            "ng iyong ${profile.numberOfDependents} dependent${if (profile.numberOfDependents > 1) "s" else ""}"
        else "para sa iyong kinabukasan"

        return listOf(

            SalesStep(
                stepNumber = 1,
                title = "Opening / Rapport Building",
                script = """
                    "Kumusta $firstName! Salamat sa iyong oras ngayon. 
                    
                    Alam ko pong marami kang inaasikaso sa buhay, kaya tutulungan kita ng mabilis at malinaw ngayon.
                    
                    Ang layunin natin ay tingnan kung paano natin mapapangalagaan ang iyong pamilya at ang iyong kinabukasan — nang hindi kailangang mag-ipon ng malaki ngayon."
                """.trimIndent(),
                tips = "Establish trust first. Smile, make eye contact. Don't rush. Mirror their energy — if they're relaxed, be relaxed."
            ),

            SalesStep(
                stepNumber = 2,
                title = "Fact-Finding / Needs Analysis",
                script = """
                    "Para mas matulungan kita, may ilang tanong lang ako — wala itong tama o maling sagot, ha?
                    
                    Ang kita mo ngayon ay around $incomeFormatted kada buwan, tama ba? At ikaw ay ${profile.age} years old pa lang — ibig sabihin, napakagandang oras pa para mag-plan.
                    
                    ${if (hasDependents) "Mahalaga rin sa akin na malaman — mayroon kang ${profile.numberOfDependents} na umaasa sa iyo. Ano ang pinakamalaking concern mo para sa kanila?" else "Ano ang pinaka-importanteng bagay na gusto mong makamit sa susunod na 5–10 taon?"}
                    
                    At kung may mangyari sa iyo bukas — may plano ka na ba para sa kanila?""".trimIndent(),
                tips = "Listen actively. Let the client speak. Take note of emotional words they use — these become your triggers later."
            ),

            SalesStep(
                stepNumber = 3,
                title = "Problem Identification",
                script = """
                    "Maraming salamat sa pagiging bukas $firstName. 
                    
                    Narinig ko na ang iyong concern ay: '${profile.concerns.ifBlank { "paano mapapangalagaan ang pamilya" }}.'
                    
                    Ito ang katotohanan — ${if (hasDependents) "ang ${profile.numberOfDependents} na taong umaasa sa iyo ay walang backup plan kung wala ka." else "ikaw ang iyong sariling investment — walang plano, walang proteksyon."}
                    
                    Hindi natin gustong isipin ito, pero mas mahalaga na maging handa kaysa magsisi sa bandang huli, di ba?"
                """.trimIndent(),
                tips = "Use the 'What if' emotional pull. Don't be too aggressive — be genuine and empathetic. Their concern is your opening."
            ),

            SalesStep(
                stepNumber = 4,
                title = "Solution Presentation",
                script = """
                    "Kaya naman, ituturo ko sa iyo ang isang solusyon na dalawa ang trabaho para sa iyo.
                    
                    Una — pinoprotektahan nito ang iyong pamilya $dependentsPhrase kung may mangyari sa iyo.
                    
                    Pangalawa — lumalago ang iyong pera habang tulog ka. Hindi lang basta insurance — ito ay investment din.
                    
                    At ang maganda pa — based sa iyong income na $incomeFormatted, ang monthly premium ay very manageable. Parang kape lang sa umaga — pero ang balik nito? Napakalaki.
                    
                    Handa ka na bang tingnan natin ang detalye?"
                """.trimIndent(),
                tips = "Use relatable comparisons (coffee, lunch budget). Always link benefits back to their specific goals and concerns. Keep it simple."
            ),

            SalesStep(
                stepNumber = 5,
                title = "Handling Objections",
                script = """
                    [If they say "Mahal naman..."]
                    "Naiintindihan kita. Totoo ngang may gastos — pero isipin natin ito: kung ako ay magtatanong, 'Magkano ang halaga ng buhay mo para sa iyong pamilya?' — ano ang sagot mo?
                    
                    Ang ${profile.numberOfDependents} dependent${if (profile.numberOfDependents > 1) "s" else ""} mo ay walang presyo, $firstName.
                    
                    Ang mas mahal na bagay ay kung wala kang plano at may mangyari."
                    
                    [If they say "Mag-iisip pa ako..."]
                    "Sige lang, respeto ko ang desisyon mo. Pero baka naman i-share ko lang — ang pinaka-murang oras para mag-avail ng insurance ay ngayon, habang malusog ka at bata pa. Ang presyo ay tumaas habang tumatanda tayo."
                """.trimIndent(),
                tips = "Validate their concern first — never argue. Use the 'Feel, Felt, Found' method: 'I understand how you feel, others have felt the same, but what they found was...'"
            ),

            SalesStep(
                stepNumber = 6,
                title = "Closing / Call to Action",
                script = """
                    "So $firstName, base sa lahat ng ating napag-usapan — ang ${profile.financialGoals.ifBlank { "financial security" }} mo at ang proteksyon $dependentsPhrase — malinaw naman na mayroon nang solusyon para sa iyo.
                    
                    Ito na ang hakbang mo para sa kinabukasan ng iyong pamilya. Ang tanong lang ay — kailan mo gustong magsimula?
                    
                    Pwede tayong simulan ngayon mismo. Ilang minuto lang ang proseso, at magiging kampante ka na bukas."
                """.trimIndent(),
                tips = "Use assumptive close: 'When do you want to start?' not 'Do you want to start?' Reduce friction — offer to help with the paperwork immediately."
            )
        )
    }

    /**
     * Generates product recommendation based on age and income.
     */
    fun generateRecommendation(profile: com.closingassistant.data.model.ClientProfile): Recommendation {
        val plan = selectBestPlan(profile)
        val premium = calculateEstimatedPremium(profile)
        val coverage = calculateCoverage(profile)

        val talkingPoints = buildTalkingPoints(profile)
        val triggers = buildEmotionalTriggers(profile)
        val closingScript = buildClosingScript(profile, plan)

        return Recommendation(
            planName = plan.first,
            planDescription = plan.second,
            estimatedPremium = "₱${formatIncome(premium)} / mo.",
            coverage = "₱${formatIncome(coverage)}",
            talkingPoints = talkingPoints,
            emotionalTriggers = triggers,
            closingScript = closingScript
        )
    }

    private fun selectBestPlan(profile: com.closingassistant.data.model.ClientProfile): Pair<String, String> {
        return when {
            profile.age <= 30 && profile.monthlyIncome >= 30_000 ->
                Pair(
                    "VUL — Variable Unit-Linked Plan",
                    "Perpekto para sa kabataang professional tulad mo. Pinagsama ang life insurance at investment para lumaking mabuti ang iyong pera habang protektado ka at ang iyong pamilya."
                )
            profile.age in 31..45 && profile.numberOfDependents > 0 ->
                Pair(
                    "Whole Life + Critical Illness Rider",
                    "Designed para sa mga nagtataguyod ng pamilya — comprehensive na proteksyon against death, disability, at critical illness. Assured ang future ng iyong mga mahal sa buhay."
                )
            profile.age in 31..45 && profile.numberOfDependents == 0 ->
                Pair(
                    "VUL — Growth-Focused Plan",
                    "Para sa professionals na focused sa wealth accumulation. Malaking portion ay napupunta sa equity funds para sa higher long-term returns kasama ang life coverage."
                )
            profile.age > 45 ->
                Pair(
                    "Endowment / Retirement Plan",
                    "Angkop para sa iyong stage of life — guaranteed na ibabalik ang iyong investment sa retirement age, kasama ang life protection habang aktibo ka pa."
                )
            else ->
                Pair(
                    "Term Life + Savings Plan",
                    "Budget-friendly na solusyon na nagbibigay ng malaking proteksyon sa mababang premium, kasama ang savings component para sa iyong mga goals."
                )
        }
    }

    private fun calculateEstimatedPremium(profile: com.closingassistant.data.model.ClientProfile): Double {
        // Rule of thumb: 10–15% of monthly income for ideal insurance premium
        val basePercent = when {
            profile.age <= 30 -> 0.08
            profile.age in 31..40 -> 0.10
            profile.age in 41..50 -> 0.12
            else -> 0.14
        }
        val dependentBonus = profile.numberOfDependents * 500.0
        return (profile.monthlyIncome * basePercent) + dependentBonus
    }

    private fun calculateCoverage(profile: com.closingassistant.data.model.ClientProfile): Double {
        // Standard: 10–12x annual income
        val multiplier = when {
            profile.numberOfDependents >= 3 -> 12.0
            profile.numberOfDependents > 0 -> 10.0
            else -> 8.0
        }
        return profile.monthlyIncome * 12 * multiplier
    }

    private fun buildTalkingPoints(profile: com.closingassistant.data.model.ClientProfile): List<String> {
        val points = mutableListOf<String>()

        points.add("Sa edad na ${profile.age}, ang premium mo ay mas mababa kumpara sa mas matanda — perfect timing ito.")

        if (profile.numberOfDependents > 0) {
            points.add("Ang ${profile.numberOfDependents} dependent${if (profile.numberOfDependents > 1) "s" else ""} mo ay nangangailangan ng garantiya — hindi lang pag-asa.")
        }

        if (profile.monthlyIncome > 0) {
            val premium = calculateEstimatedPremium(profile)
            val percentage = (premium / profile.monthlyIncome * 100).toInt()
            points.add("Ang estimated premium ay $percentage% lang ng iyong monthly income — financially manageable.")
        }

        if (profile.financialGoals.isNotBlank()) {
            points.add("Para sa iyong goal na '${profile.financialGoals.take(60)}${if (profile.financialGoals.length > 60) "…" else ""}' — ang planong ito ay tumutulong direkta.")
        }

        points.add("Hindi ito basta insurance — ito ay isang systematic investment na lumalago every year.")
        points.add("Tax-free ang death benefit — lahat ng proceeds ay mapupunta sa iyong pamilya, walang deductions.")

        return points
    }

    private fun buildEmotionalTriggers(profile: com.closingassistant.data.model.ClientProfile): List<EmotionalTrigger> {
        val triggers = mutableListOf<EmotionalTrigger>()

        if (profile.numberOfDependents > 0) {
            triggers.add(
                EmotionalTrigger(
                    emoji = "👨‍👩‍👧",
                    label = "Pamilya",
                    description = "Ang iyong ${profile.numberOfDependents} na mahal sa buhay ay umaasa sa iyo — bigyan mo sila ng security na karapat-dapat sa kanila.",
                    backgroundType = TriggerType.FAMILY
                )
            )
        }

        triggers.add(
            EmotionalTrigger(
                emoji = "🛡️",
                label = "Seguridad",
                description = "Ang buhay ay puno ng hindi inaasahan. Ang isang plano ngayon ay nagbibigay ng kapayapaan ng isip sa bawat araw.",
                backgroundType = TriggerType.SECURITY
            )
        )

        if (profile.age <= 40) {
            triggers.add(
                EmotionalTrigger(
                    emoji = "🌱",
                    label = "Kinabukasan",
                    description = "Sa edad ${profile.age}, ang bawat pisong namumuhunan mo ngayon ay magiging malaking halaga sa hinaharap. Huwag palampasin ang oras.",
                    backgroundType = TriggerType.FUTURE
                )
            )
        }

        if (profile.concerns.isNotBlank()) {
            triggers.add(
                EmotionalTrigger(
                    emoji = "💡",
                    label = "Solusyon sa Concern",
                    description = "Nabanggit mo: '${profile.concerns.take(80)}${if (profile.concerns.length > 80) "…" else ""}' — direkta itong nareresolba ng planong ito.",
                    backgroundType = TriggerType.LEGACY
                )
            )
        }

        return triggers
    }

    private fun buildClosingScript(
        profile: com.closingassistant.data.model.ClientProfile,
        plan: Pair<String, String>
    ): String {
        val premium = calculateEstimatedPremium(profile)
        return """
"So ${'$'}{firstName}, ngayon na malinaw na ang lahat — ang ${plan.first} ay ang pinaka-angkop na solusyon para sa iyo.

Para sa halos ₱${formatIncome(premium)} kada buwan, makukuha mo ang:
✅ Life protection para sa iyong pamilya
✅ Investment na lumalago para sa iyong future
✅ Peace of mind na hindi mabibili ng kahit anong halaga

Hindi ito tungkol sa kung kaya mo o hindi — tungkol ito sa kung gusto mong protektahan ang mga taong mahal mo sa buhay.

Sisimulan natin ngayon — ibibigay ko sa iyo ang form, at ito na ang pinaka-importanteng desisyon na gagawin mo ngayon para sa iyong pamilya."
        """.trimIndent()
    }

    private fun formatIncome(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${(amount / 1_000_000.0).let { if (it % 1 == 0.0) it.toInt().toString() else String.format("%.1f", it) }}M"
            amount >= 1_000 -> "${(amount / 1_000.0).let { if (it % 1 == 0.0) it.toInt().toString() else String.format("%.1f", it) }}K"
            else -> amount.toInt().toString()
        }
    }
}
