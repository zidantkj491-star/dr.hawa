package com.example.drhawa

import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Sage = Color(0xFF789B88)
private val Deep = Color(0xFF29483D)
private val Cream = Color(0xFFF6F7F1)
private val Pale = Color(0xFFE6EEE8)
private val Ink = Color(0xFF26352E)

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("id", "ID")
        }
        setContent { DrHawaApp(
            onSpeak = { text -> tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hawa") }
        ) }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun DrHawaApp(onSpeak: (String) -> Unit) {
    var page by remember { mutableStateOf("Home") }
    var selectedMood by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("Halo, aku Dr. Hawa. Kamu boleh bercerita di sini. Apa yang sedang kamu rasakan hari ini?")) }
    var breathing by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) message = spoken
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Sage, onPrimary = Color.White, background = Cream,
            surface = Color.White, onSurface = Ink, secondary = Deep
        )
    ) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf(
                        "Home" to Icons.Default.Home,
                        "Hawa" to Icons.Default.Favorite,
                        "Relax" to Icons.Default.Spa,
                        "Learn" to Icons.Default.MenuBook,
                        "Profile" to Icons.Default.Person
                    ).forEach { (name, icon) ->
                        NavigationBarItem(
                            selected = page == name,
                            onClick = { page = name },
                            icon = { Icon(icon, contentDescription = name) },
                            label = { Text(name, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            when (page) {
                "Home" -> HomeScreen(padding, selectedMood) { mood ->
                    selectedMood = mood
                    page = "Hawa"
                    messages = listOf("Terima kasih sudah berbagi. Aku melihat kamu sedang merasa $mood. Mau cerita apa yang terjadi?")
                }
                "Hawa" -> ChatScreen(
                    padding, messages, message,
                    onMessageChange = { message = it },
                    onSend = {
                        val input = message.trim()
                        if (input.isNotEmpty()) {
                            messages = messages + "Kamu: $input" + "Dr. Hawa: ${localReply(input)}"
                            message = ""
                        }
                    },
                    onVoice = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan bicara")
                        }
                        runCatching { speechLauncher.launch(intent) }
                    },
                    onSpeak = onSpeak
                )
                "Relax" -> RelaxScreen(padding, breathing) { breathing = !breathing }
                "Learn" -> LearnScreen(padding)
                else -> ProfileScreen(padding)
            }
        }
    }
}

@Composable
fun HomeScreen(padding: PaddingValues, mood: String, onMood: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text("DR. HAWA", color = Sage, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Text("Take a breath.\nYou're here.", fontSize = 32.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold, color = Deep)
            Text("Sebuah ruang kecil untuk memahami dirimu.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Pale), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Bagaimana perasaanmu hari ini?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Deep)
                    Text("Pilih yang paling mendekati perasaanmu.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp, bottom = 14.dp))
                    val moods = listOf("Bahagia" to "😊", "Sedih" to "🌧️", "Cemas" to "🌫️", "Marah" to "🔥", "Kesepian" to "🌙", "Lelah" to "🍃", "Bingung" to "🌀")
                    moods.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            pair.forEach { (name, emoji) ->
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onMood(name) },
                                    color = if (mood == name) Sage else Color.White,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 20.sp)
                                        Spacer(Modifier.width(7.dp))
                                        Text(name, color = if (mood == name) Color.White else Deep, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Deep)) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Butuh teman bicara?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Dr. Hawa siap mendengarkan.", color = Color(0xFFD8E6DC), fontSize = 13.sp)
                    }
                    Text("♡", color = Color.White, fontSize = 38.sp)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    padding: PaddingValues, messages: List<String>, message: String,
    onMessageChange: (String) -> Unit, onSend: () -> Unit,
    onVoice: () -> Unit, onSpeak: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Dr. Hawa", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Deep)
        Text("Teman bicara yang mendengarkan tanpa menghakimi", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { item ->
                val isUser = item.startsWith("Kamu:")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isUser) Pale else Color.White),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.removePrefix("Kamu: ").removePrefix("Dr. Hawa: "), modifier = Modifier.weight(1f), color = Ink)
                            if (!isUser) IconButton(onClick = { onSpeak(item.removePrefix("Dr. Hawa: ")) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Bacakan", tint = Sage)
                            }
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoice) { Icon(Icons.Default.Mic, contentDescription = "Suara", tint = Sage) }
            OutlinedTextField(
                value = message, onValueChange = onMessageChange,
                modifier = Modifier.weight(1f), placeholder = { Text("Ceritakan perasaanmu...") },
                shape = RoundedCornerShape(20.dp), maxLines = 3
            )
            IconButton(onClick = onSend) { Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Sage) }
        }
        Text("Dr. Hawa bukan layanan darurat atau pengganti tenaga profesional.", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
    }
}

fun localReply(input: String): String {
    val s = input.lowercase()
    return when {
        listOf("sedih", "kecewa", "menangis").any { it in s } ->
            "Kedengarannya ini terasa berat buatmu. Tidak apa-apa untuk memberi ruang pada perasaan itu. Apa hal yang paling mengganggumu saat ini?"
        listOf("cemas", "takut", "khawatir", "panik").any { it in s } ->
            "Terima kasih sudah menceritakannya. Kita bisa pelan-pelan. Coba tarik napas dengan nyaman, lalu ceritakan bagian yang paling membuatmu khawatir."
        listOf("marah", "kesal").any { it in s } ->
            "Aku menangkap ada sesuatu yang membuatmu kesal. Sebelum mengambil keputusan, mungkin kita bisa mengenali apa yang sebenarnya kamu butuhkan."
        listOf("lelah", "capek").any { it in s } ->
            "Kamu mungkin sedang membutuhkan jeda. Tidak semua hal harus diselesaikan sekaligus. Apa yang paling menguras energimu?"
        else -> "Terima kasih sudah berbagi denganku. Aku ingin memahami ceritamu lebih jauh. Menurutmu, apa yang paling penting untuk aku ketahui?"
    }
}

@Composable
fun RelaxScreen(padding: PaddingValues, breathing: Boolean, onToggle: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Relaxation Center", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Deep)
        Text("Ambil jeda. Kamu tidak harus terburu-buru.", color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(34.dp))
        Box(Modifier.size(210.dp).background(Pale, CircleShape), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (breathing) "Bernapas..." else "🌿", fontSize = 35.sp)
                Text(if (breathing) "Tarik dan hembuskan\nsecara perlahan" else "Ruang tenang", color = Deep, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(25.dp))
        Button(onClick = onToggle, colors = ButtonDefaults.buttonColors(containerColor = Sage)) {
            Text(if (breathing) "Selesai latihan" else "Mulai latihan napas")
        }
        Spacer(Modifier.height(25.dp))
        listOf("📝  Jurnal perasaan" to "Tuliskan apa yang sedang kamu rasakan.", "🌱  Mindfulness" to "Arahkan perhatian pada keadaan saat ini.", "🙏  Gratitude" to "Catat hal kecil yang kamu syukuri.").forEach { (title, desc) ->
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, color = Deep, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun LearnScreen(padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(22.dp)) {
        Text("Mental Health Notes", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Deep)
        Text("Kenali dirimu melalui pengetahuan.", color = Color.Gray, modifier = Modifier.padding(top = 5.dp, bottom = 18.dp))
        val lessons = listOf(
            "Apa itu stres?" to "Stres adalah respons tubuh dan pikiran terhadap tekanan. Istirahat, dukungan sosial, dan pengelolaan aktivitas dapat membantu.",
            "Mengenali kecemasan" to "Kecemasan adalah rasa khawatir atau takut. Perhatikan pemicunya dan bicarakan dengan orang tepercaya bila mengganggu keseharian.",
            "Pentingnya tidur" to "Tidur yang cukup mendukung suasana hati, konsentrasi, dan kesehatan tubuh.",
            "Mencari bantuan" to "Meminta bantuan bukan tanda kelemahan. Kamu dapat berbicara dengan orang dewasa tepercaya, konselor sekolah, psikolog, atau tenaga kesehatan."
        )
        lessons.forEach { (title, desc) ->
            var expanded by remember { mutableStateOf(false) }
            Card(Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { expanded = !expanded }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(17.dp)) {
                    Text(title, color = Deep, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (expanded) Text(desc, color = Color.DarkGray, modifier = Modifier.padding(top = 9.dp))
                    else Text("Ketuk untuk membaca", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(22.dp)) {
        Text("Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Deep)
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Pale), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Dr. Hawa", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Deep)
                Text("Mental Health Companion", color = Color.Gray)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Privasi", fontWeight = FontWeight.Bold, color = Deep)
        Text("Versi prototipe ini menyimpan percakapan hanya selama aplikasi terbuka. Jangan masukkan informasi pribadi yang sangat sensitif.", color = Color.Gray, modifier = Modifier.padding(top = 7.dp))
        Spacer(Modifier.height(18.dp))
        Text("Catatan penting", fontWeight = FontWeight.Bold, color = Deep)
        Text("Aplikasi ini bersifat edukatif dan suportif, bukan alat diagnosis. Jika kamu merasa tidak aman atau membutuhkan bantuan segera, hubungi orang dewasa tepercaya dan layanan darurat setempat.", color = Color.Gray, modifier = Modifier.padding(top = 7.dp))
    }
}
