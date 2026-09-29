package com.example.domain.model

data class MicroLesson(
    val id: String,
    val category: String,
    val title: String,
    val durationMinutes: Int,
    val overview: String,
    val contentMarkdown: String,
    val scholarlySources: List<String>,
    val practicalTakeaways: List<String>
)

object LearningDatasetRepository {

    val LESSONS = listOf(
        MicroLesson(
            id = "lesson_wudu_basics",
            category = "Salah & Purity",
            title = "The Essentials of Wudu (Ablution)",
            durationMinutes = 3,
            overview = "Understand the 4 Quranic obligations of Wudu and common authentic prophetic sunnahs.",
            contentMarkdown = """
                Wudu is the spiritual and physical purification required before standing in prayer.
                
                The Quran establishes four obligatory pillars (Fara'id) in Surah Al-Ma'idah (5:6):
                1. Washing the face from forehead to chin and ear to ear.
                2. Washing both arms including the elbows.
                3. Wiping at least a quarter of the head with wet hands (Hanafi; Sunnat is wiping the whole head).
                4. Washing both feet up to and including the ankles.
                
                Prophetic Sunnahs (Recommended Practices):
                - Starting with Bismillah and intention (Niyyah) in the heart.
                - Washing the hands thrice up to wrists.
                - Rinsing the mouth (Madmadah) and cleaning the nose (Istinshaq) thrice.
                - Running wet fingers through the beard and between fingers and toes (Takhlil).
                - Maintaining sequential order (Tartib) without prolonged interruption (Muwalat).
            """.trimIndent(),
            scholarlySources = listOf(
                "The Noble Quran, Surah Al-Ma'idah (5:6)",
                "Sahih al-Bukhari 159 (Hadith of Uthman describing Prophet's wudu)",
                "Al-Hidaya, Kitab al-Taharah (Imam Marghinani, Hanafi)"
            ),
            practicalTakeaways = listOf(
                "Do not waste water even if by a flowing stream.",
                "Ensure dry spots on heels and elbows are reached.",
                "Recite the Kalimah Shahadah and supplication after finishing."
            )
        ),
        MicroLesson(
            id = "lesson_khushu_salah",
            category = "Salah & Purity",
            title = "Attaining Khushu' (Tranquility) in Prayer",
            durationMinutes = 4,
            overview = "How to calm a wandering mind and feel divine connection during your daily prayers.",
            contentMarkdown = """
                Khushu' is inner humility, stillness of the limbs, and attentive contemplation of what one is reciting.
                
                Practical Anchors for Khushu':
                - Prepare before the Takbir: Perform wudu mindfully, arrive before iqamah, and eliminate immediate physical distractions.
                - Contemplate the Words: Recite slowly (Tartil). When saying 'Al-Hamdu lillahi Rabbil-Alameen', remember Allah responds directly: 'My servant has praised Me' (Sahih Muslim 395).
                - Fix the Gaze: Direct your gaze respectfully at the place of prostration (Sujud), rather than looking around.
                - Remember the Final Prayer: The Prophet (peace be upon him) advised: 'Pray as if it were your farewell prayer' (Sunan Ibn Majah 4171).
            """.trimIndent(),
            scholarlySources = listOf(
                "Surah Al-Mu'minun (23:1-2): 'Successful indeed are the believers who are humble in their prayers.'",
                "Ihya Ulum al-Din, Kitab Asrar al-Salah (Imam Ghazali)",
                "Sahih Muslim 395 (Hadith Qudsi on Surat al-Fatihah)"
            ),
            practicalTakeaways = listOf(
                "Pause for 2 seconds after each ayah before reciting the next.",
                "Vary the short surahs recited in Salah to prevent mechanical repetition.",
                "Take a deep breath and consciously let go of worldly tasks before saying 'Allahu Akbar'."
            )
        ),
        MicroLesson(
            id = "lesson_akhlaq_neighbor",
            category = "Character & Akhlaq",
            title = "Rights of Neighbors in Islam",
            durationMinutes = 3,
            overview = "The high ethical status of neighbors regardless of their faith or background.",
            contentMarkdown = """
                The Prophet Muhammad (peace be upon him) said: 'Jibril kept enjoining good treatment of the neighbor upon me until I thought he would assign him a share of inheritance.' (Sahih al-Bukhari 6014).
                
                Islamic teachings identify three classes of neighbors:
                1. A neighbor who is a relative and Muslim: has three rights (kinship, Islam, neighborhood).
                2. A neighbor who is Muslim: has two rights (Islam and neighborhood).
                3. A neighbor of different faith: has the sacred right of good neighborhood and kindness.
                
                Harm to neighbors is strictly condemned: 'By Allah he does not believe... whose neighbor is not safe from his harm.' (Sahih al-Bukhari 6016).
            """.trimIndent(),
            scholarlySources = listOf(
                "Sahih al-Bukhari 6014, 6016",
                "Sahih Muslim 2625 (Abu Dharr on adding water to broth to share with neighbors)",
                "Adab al-Mufrad (Imam al-Bukhari)"
            ),
            practicalTakeaways = listOf(
                "Share homemade food or fruit occasionally.",
                "Keep noise levels respectful during late night and early mornings.",
                "Help senior neighbors with groceries or physical tasks."
            )
        )
    )
}
