package com.example.navigatour_projekt


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.navigatour_projekt.ui.theme.NavigaTour_projektTheme
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import android.location.Location
import androidx.compose.material3.IconButton
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Omogućuje prikaz aktivnosti preko cijelog ekrana
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current

            // Provjerava je li korisnik prijavljen na temelju SharedPreferences
            val isLoggedIn = remember { mutableStateOf(false) } // Stanje prijave pohranjeno u mutableStateOf
            LaunchedEffect(Unit) {
                val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) // Dohvaća SharedPreferences
                isLoggedIn.value = sharedPref.getBoolean("is_logged_in", false) // Postavlja stanje prijave
            }


            NavigaTour_projektTheme {
                // navigacija između ekrana
                NavHost(
                    navController = navController, // Povezuje navController
                    startDestination = if (isLoggedIn.value) "home" else "main" //
                ) {
                    composable("main") { MainScreen(navController = navController) }
                    composable("login") { LoginScreen(navController = navController) }
                    composable("register") { RegistrationScreen(navController = navController) }
                    composable("home") { HomeScreen(navController = navController) }
                    composable("profile") { ProfileScreen(navController = navController) }
                    composable("add") { backStackEntry ->
                        val viewModel: AddGroupViewModel =
                            viewModel()
                        AddScreen(navController, viewModel)
                    }
                    composable("remove_groups") { RemoveScreen(navController = navController) }
                    composable("compass") { CompassScreen(navController = navController) }
                    composable("item/{groupName}") { backStackEntry ->
                        val groupName = backStackEntry.arguments?.getString("groupName")
                        if (groupName != null) {
                            // Koristi postojeći navController, ne stvara novi
                            ItemScreen(navController = navController, groupName = groupName)
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun MainScreen(navController: NavController) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.primary_color)),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp, start = 20.dp, end = 20.dp)
                ) {

                    OutlinedButton(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(id = R.color.white)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.white))
                    ) {
                        Text(text = "PRIJAVA")
                    }

                    Button(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.white),
                            contentColor = colorResource(id = R.color.primary_color)
                        )
                    ) {
                        Text(text = "REGISTRACIJA")
                    }
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen(navController: NavController) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }
        val context = LocalContext.current
        var isEmail by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.primary_color))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.navigate("main") }
                )
                Spacer(modifier = Modifier.width(70.dp))
                Image(
                    painter = painterResource(id = R.drawable.navbar_logo),
                    contentDescription = "Logo"
                )
            }
            Text(
                text = "Prijava",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                },
                label = { Text("Korisničko ime ili Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = colorResource(id = R.color.white)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.secondary_color),
                    unfocusedBorderColor = colorResource(id = R.color.white),
                    cursorColor = colorResource(id = R.color.secondary_color),
                    focusedLabelColor = colorResource(id = R.color.secondary_color),
                    unfocusedLabelColor = colorResource(id = R.color.light_gray)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Lozinka") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(color = colorResource(id = R.color.white)),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.secondary_color),
                    unfocusedBorderColor = colorResource(id = R.color.white),
                    cursorColor = colorResource(id = R.color.secondary_color),
                    focusedLabelColor = colorResource(id = R.color.secondary_color),
                    unfocusedLabelColor = colorResource(id = R.color.light_gray)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    when {
                        username.isEmpty() || password.isEmpty() -> {
                            errorText = "Unesite svoje podatke"
                        }
                        isEmail -> {
                            // Ako je uneseni tekst e-mail, prijavi se izravno
                            loginUser(
                                email = username,
                                password = password,
                                context = context,
                                onSuccess = {
                                    saveLoginState(context, true)
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    errorText = error
                                }
                            )
                        }
                        else -> {
                            // Funkcija za pronalaženje e-mail adrese korisnika na temelju korisničkog imena
                            findUserEmailByUsername(
                                username = username,
                                onEmailFound = { email ->
                                    loginUser(
                                        email = email,
                                        password = password,
                                        context = context,
                                        onSuccess = {
                                            saveLoginState(context, true)
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onError = { error ->
                                            errorText = error
                                        }
                                    )
                                },
                                onError = { error ->
                                    errorText = error
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.white),
                    contentColor = colorResource(id = R.color.primary_color)
                )
            ) {
                Text("PRIJAVI SE")
            }

            if (errorText.isNotEmpty()) {
                Text(
                    text = errorText,
                    color = colorResource(id = R.color.secondary_color),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    private fun loginUser(
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let {
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(it.uid!!)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {

                                    saveLoginState(context, true)
                                    onSuccess()
                                } else {
                                    onError("Podaci o korisniku nisu pronađeni")
                                }
                            }
                            .addOnFailureListener { e ->
                                onError(e.localizedMessage ?: "Došlo je do greške prilikom dohvaćanja podataka")
                            }
                    }
                } else {

                    val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                    val errorMessage = firebaseErrorMessages[errorCode]
                        ?: "Došlo je do greške prilikom prijave"
                    onError(errorMessage)
                }
            }
    }

    private fun findUserEmailByUsername(
        username: String,
        onEmailFound: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onError("Korisničko ime nije pronađeno")
                } else {
                    val email = result.documents.firstOrNull()?.getString("email")
                    if (email != null) {
                        onEmailFound(email)
                    } else {
                        onError("E-mail nije pronađen za korisničko ime")
                    }
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Došlo je do greške prilikom dohvaćanja e-maila")
            }
    }

    // Mapa s porukama o greškama prilikom prijave u Firebase Authentication
    private val firebaseErrorMessages = mapOf(
        "ERROR_INVALID_EMAIL" to "Nevažeća e-mail adresa",
        "ERROR_WRONG_PASSWORD" to "Pogrešna lozinka",
        "ERROR_USER_NOT_FOUND" to "Korisnik nije pronađen",
        "ERROR_USER_DISABLED" to "Korisnički račun je onemogućen",
        "ERROR_TOO_MANY_REQUESTS" to "Previše zahtjeva. Pokušajte ponovo kasnije",
        "ERROR_OPERATION_NOT_ALLOWED" to "Ova operacija nije dozvoljena",
        "ERROR_EMAIL_ALREADY_IN_USE" to "E-mail adresa već postoji",
        "ERROR_WEAK_PASSWORD" to "Lozinka je preslaba"
    )




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RegistrationScreen(navController: NavController) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }

        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.primary_color))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(70.dp))
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(70.dp))
                Image(
                    painter = painterResource(id = R.drawable.navbar_logo),
                    contentDescription = "Logo"
                )
            }

            Text(
                text = "Registracija",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.white),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Ime") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = colorResource(id = R.color.white)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.secondary_color),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.secondary_color),
                        focusedLabelColor = colorResource(id = R.color.secondary_color),
                        unfocusedLabelColor = colorResource(id = R.color.light_gray)
                    )
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Prezime") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = colorResource(id = R.color.white)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.secondary_color),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.secondary_color),
                        focusedLabelColor = colorResource(id = R.color.secondary_color),
                        unfocusedLabelColor = colorResource(id = R.color.light_gray)
                    )
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = colorResource(id = R.color.white)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.secondary_color),
                    unfocusedBorderColor = colorResource(id = R.color.white),
                    cursorColor = colorResource(id = R.color.secondary_color),
                    focusedLabelColor = colorResource(id = R.color.secondary_color),
                    unfocusedLabelColor = colorResource(id = R.color.light_gray)
                )
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Korisničko ime") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = colorResource(id = R.color.white)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.secondary_color),
                    unfocusedBorderColor = colorResource(id = R.color.white),
                    cursorColor = colorResource(id = R.color.secondary_color),
                    focusedLabelColor = colorResource(id = R.color.secondary_color),
                    unfocusedLabelColor = colorResource(id = R.color.light_gray)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Lozinka") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = colorResource(id = R.color.white)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.secondary_color),
                    unfocusedBorderColor = colorResource(id = R.color.white),
                    cursorColor = colorResource(id = R.color.secondary_color),
                    focusedLabelColor = colorResource(id = R.color.secondary_color),
                    unfocusedLabelColor = colorResource(id = R.color.light_gray)
                )
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Broj telefona") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = colorResource(id = R.color.white)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.secondary_color),
                    unfocusedBorderColor = colorResource(id = R.color.white),
                    cursorColor = colorResource(id = R.color.secondary_color),
                    focusedLabelColor = colorResource(id = R.color.secondary_color),
                    unfocusedLabelColor = colorResource(id = R.color.light_gray)
                )
            )

            Button(
                onClick = {
                    if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                        username.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()
                    ) {
                        errorText = "Sva polja moraju biti popunjena"
                    } else {
                        registerUser(
                            firstName,
                            lastName,
                            email,
                            username,
                            password,
                            phoneNumber,
                            context,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Uspješno ste se registrirali",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            },
                            onError = { errorMessage ->
                                errorText = errorMessage
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.white),
                    contentColor = colorResource(id = R.color.primary_color)
                )
            ) {
                Text("REGISTRIRAJ SE")
            }

            if (errorText.isNotEmpty()) {
                Text(
                    text = errorText,
                    color = colorResource(id = R.color.secondary_color),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }



    private fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        username: String,
        password: String,
        phoneNumber: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Kreiranje korisnika u Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Uspješna registracija u Firebase Authentication
                    val user = auth.currentUser

                    // Pripremite podatke za Firestore
                    val userData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "username" to username,
                        "phoneNumber" to phoneNumber
                    )

                    // Spremite podatke u Firestore
                    user?.let {
                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Registracija i spremanje podataka su uspješni
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onError("Došlo je do greške prilikom spremanja podataka u Firestore")
                            }
                    }
                } else {
                    // Greška prilikom registracije u Firebase Authentication
                    val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                    val errorMessage = firebaseErrorMessages[errorCode]
                        ?: "Došlo je do greške prilikom registracije"
                    onError(errorMessage)
                }
            }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen(navController: NavController) {
        val context = LocalContext.current

        // Stanje za pohranu lista grupa (naziv grupe i URL slike)
        val groups = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

        // Dobivanje trenutnog odredišta navigacije
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Stanje za pohranu imena trenutnog korisnika
        val currentUserName = remember { mutableStateOf("") }

        // Dohvaćanje imena trenutnog korisnika koristeći Firebase Authentication
        LaunchedEffect(Unit) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userDocRef = db.collection("users").document(currentUser.uid)
                userDocRef.get()
                    .addOnSuccessListener { document ->
                        // Dobivanje korisničkog imena iz Firestore dokumenta
                        val username = document.getString("username") ?: "Nepoznati korisnik"
                        currentUserName.value = username
                    }
                    .addOnFailureListener {
                        // Prikazivanje poruke o grešci ako dohvaćanje korisničkog imena ne uspije
                        Toast.makeText(context, "Greška pri dohvaćanju korisničkog imena", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Prikazivanje poruke ako korisnik nije prijavljen
                Toast.makeText(context, "Niste prijavljeni", Toast.LENGTH_SHORT).show()
            }
        }

        // Dohvaćanje grupa u kojima je trenutni korisnik član
        LaunchedEffect(currentUserName.value) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val groupsRef = db.collection("groups")
                groupsRef
                    .whereArrayContains("members", currentUserName.value)
                    .get()
                    .addOnSuccessListener { documents ->
                        val groupList = mutableListOf<Pair<String, String>>()
                        for (document in documents) {
                            // Dobivanje naziva grupe i URL slike iz Firestore dokumenta
                            val groupName = document.getString("name") ?: ""
                            val imageUrl = document.getString("image") ?: ""
                            groupList.add(Pair(groupName, imageUrl))
                        }
                        groups.value = groupList // Ažuriranje stanja s listom grupa
                    }
                    .addOnFailureListener {
                        // Prikazivanje poruke o grešci ako dohvaćanje grupa ne uspije
                        Toast.makeText(context, "Greška s učitavanjem grupa", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Scaffold pruža osnovnu strukturu ekrana s top barom, bottom barom i sadržajem
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Prikaz logotipa u top baru
                        Image(
                            painter = painterResource(id = R.drawable.navbar_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = colorResource(id = R.color.primary_color),
                        titleContentColor = colorResource(id = R.color.white)
                    ),
                    modifier = Modifier
                        .border(1.dp, colorResource(id = R.color.grey)) // Dodavanje obruba top baru
                )
            },
            bottomBar = {
                // Prikazivanje donje navigacijske trake
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            },
            content = { padding ->
                // Glavni sadržaj ekrana
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colorResource(id = R.color.primary_color))
                ) {
                    // Prikaz poruke ako korisnik nije član nijedne grupe
                    if (groups.value.isEmpty()) {
                        Text(
                            text = "Ne nalazite se ni u jednoj grupi",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    // Prikazivanje liste grupa pomoću LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(groups.value) { (groupName, imageUrl) ->
                            // Svaka stavka u listi grupa je prikazana pomoću GroupItem komponente
                            GroupItem(
                                groupName = groupName,
                                imageUrl = imageUrl,
                                navController = navController
                            )
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun GroupItem(groupName: String, imageUrl: String, navController: NavController) {
        // Učitavanje slike grupe koristeći Coil
        val painter = rememberImagePainter(
            data = imageUrl,
            builder = {
                crossfade(true) // Omogućavanje prijelaza
                placeholder(R.drawable.placeholder_image) // Placeholder slika dok se učitava
            }
        )

        val borderColor = colorResource(id = R.color.grey)

        // Box za crtanje obruba ispod svakog GroupItema
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 1.dp.toPx() // Debljina linije
                    val y = size.height - strokeWidth / 2 // Pozicija linije na dnu
                    drawLine(
                        color = borderColor,
                        start = Offset(
                            -32.dp.toPx(),
                            y
                        ), // Produženje linije izvan početka paddinga
                        end = Offset(
                            size.width + 32.dp.toPx(),
                            y
                        ), // Produženje linije izvan kraja paddinga
                        strokeWidth = strokeWidth
                    )
                }
                .clickable {
                    // Navigacija na detalje grupe kada se klikne na GroupItem
                    navController.navigate("item/$groupName")
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                // Prikaz slike grupe unutar kružnog okvira
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Group Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(32.dp)) // Razmak između slike i naziva grupe

                // Prikaz naziva grupe
                Text(
                    text = groupName,
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavController, currentRoute: String?) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.white))
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround, // Raspored ikona ravnomjerno
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikona za Home
            BottomNavIcon(
                iconId = R.drawable.home_outlined,
                activeIconId = R.drawable.home_focused,
                contentDescription = "home",
                navController = navController,
                route = "home",
                currentRoute = currentRoute
            )
            // Ikona za Remove Groups
            BottomNavIcon(
                iconId = R.drawable.remove_group_outlined,
                activeIconId = R.drawable.remove_group_focused,
                contentDescription = "remove_groups",
                navController = navController,
                route = "remove_groups",
                currentRoute = currentRoute
            )
            // Ikona za Add (s većom veličinom)
            BottomNavIcon(
                iconId = R.drawable.add,
                activeIconId = R.drawable.add,
                contentDescription = "add",
                navController = navController,
                route = "add",
                currentRoute = currentRoute,
                size = 50.dp // Veća veličina ikone
            )
            // Ikona za Compass
            BottomNavIcon(
                iconId = R.drawable.compass_outlined,
                activeIconId = R.drawable.compass_focused,
                contentDescription = "compass",
                navController = navController,
                route = "compass",
                currentRoute = currentRoute
            )
            // Ikona za Profile
            BottomNavIcon(
                iconId = R.drawable.person_outlined,
                activeIconId = R.drawable.person_focused,
                contentDescription = "profile",
                navController = navController,
                route = "profile",
                currentRoute = currentRoute
            )
        }
    }

    @Composable
    fun BottomNavIcon(
        iconId: Int,
        activeIconId: Int,
        contentDescription: String,
        navController: NavController,
        route: String,
        currentRoute: String?,
        size: Dp = 24.dp // Zadana veličina ikona
    ) {
        // Provjera je li trenutna ruta ista kao ruta ove ikone
        val isActive = route == currentRoute
        // Odabir prikladne slike ikone ovisno o aktivnom stanju
        val iconPainter = painterResource(id = if (isActive) activeIconId else iconId)
        // Odabir boje ikone ovisno o aktivnom stanju
        val iconColor =
            if (isActive) colorResource(id = R.color.secondary_color) else colorResource(id = R.color.primary_color)

        // Prikazivanje ikone
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size) // Postavljanje veličine ikone
                .clickable { navController.navigate(route) }, // Navigacija na odgovarajuću rutu kada se klikne
            tint = iconColor // Postavljanje boje ikone
        )
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileScreen(navController: NavController) {
        val context = LocalContext.current
        val userData = remember { mutableStateOf<User?>(null) }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        LaunchedEffect(Unit) {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Dohvati podatke o korisniku iz Firestore-a
                db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            userData.value = User(
                                firstName = document.getString("firstName") ?: "",
                                lastName = document.getString("lastName") ?: "",
                                email = document.getString("email") ?: "",
                                username = document.getString("username") ?: "",
                                phoneNumber = document.getString("phoneNumber") ?: ""
                            )
                        } else {
                            Log.d("ProfileScreen", "No user data found in Firestore")
                            Toast.makeText(context, "No user data found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProfileScreen", "Error fetching user data", exception)
                        Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d("ProfileScreen", "No user is signed in")
                Toast.makeText(context, "No user is signed in", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.navbar_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = colorResource(id = R.color.primary_color),
                        titleContentColor = colorResource(id = R.color.white)
                    ),
                    modifier = Modifier
                        .border(1.dp, colorResource(id = R.color.grey))
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colorResource(id = R.color.primary_color))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    userData.value?.let { user ->
                        Text(
                            text = "Ime: ${user.firstName}",
                            color = colorResource(id = R.color.white),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Prezime: ${user.lastName}",
                            color = colorResource(id = R.color.white),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Email: ${user.email}",
                            color = colorResource(id = R.color.white),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Korisničko ime: ${user.username}",
                            color = colorResource(id = R.color.white),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Telefonski broj: ${user.phoneNumber}",
                            color = colorResource(id = R.color.white),
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                logoutUser(context)
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.white),
                                contentColor = colorResource(id = R.color.primary_color)
                            )
                        ) {
                            Text("ODJAVA")
                        }
                    } ?: run {
                        Text(
                            text = "Učitavanje podataka...",
                            color = colorResource(id = R.color.white),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        )
    }

    // Funkcija za spremanje stanja prijave
    private fun saveLoginState(context: Context, isLoggedIn: Boolean) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_logged_in", isLoggedIn)
            apply()
        }
    }

    private fun logoutUser(context: Context) {
        val auth = FirebaseAuth.getInstance()
        auth.signOut() // Odjava korisnika iz Firebase Authentication

        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_logged_in", false)
            remove("username")
            apply()
        }
    }

    data class User(
        val firstName: String,
        val lastName: String,
        val email: String,
        val username: String,
        val phoneNumber: String
    )


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddScreen(navController: NavController, viewModel: AddGroupViewModel) {
        val context = LocalContext.current
        var groupName by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var allUsers by remember { mutableStateOf<List<String>>(emptyList()) }
        var selectedUsers by remember { mutableStateOf<Set<String>>(emptySet()) }
        var currentUserName by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
        }

        LaunchedEffect(Unit) {
            viewModel.checkIfUserIsLoggedIn { isLoggedIn ->
                if (isLoggedIn) {
                    viewModel.getCurrentUserName { username ->
                        currentUserName = username
                        viewModel.getAllUsersExceptCurrent(currentUserName) { users ->

                            allUsers = users
                        }
                    }
                } else {
                    Toast.makeText(context, "Molimo se prijavite", Toast.LENGTH_SHORT).show()
                    navController.navigate("login")
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Kreiraj novu grupu") },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = colorResource(id = R.color.primary_color),
                        titleContentColor = colorResource(id = R.color.white)
                    )
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Ime grupe") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(id = R.color.secondary_color),
                            unfocusedBorderColor = colorResource(id = R.color.primary_color),
                            cursorColor = colorResource(id = R.color.secondary_color),
                            focusedLabelColor = colorResource(id = R.color.secondary_color),
                            unfocusedLabelColor = colorResource(id = R.color.light_gray)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Odaberite sliku")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    imageUri?.let {
                        Image(
                            painter = rememberImagePainter(it),
                            contentDescription = "Odabrana slika",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Odaberite članove:")

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn {
                            items(allUsers) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedUsers = if (selectedUsers.contains(user)) {
                                                selectedUsers - user
                                            } else {
                                                selectedUsers + user
                                            }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedUsers.contains(user),
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = user)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (groupName.isNotEmpty() && imageUri != null) {
                                if (selectedUsers.isNotEmpty()) {
                                    viewModel.uploadImageToFirebase(
                                        context = context,
                                        imageUri = imageUri!!,
                                        groupName = groupName,
                                        selectedUsers = selectedUsers.toList(),
                                        currentUserName = currentUserName,
                                        navController = navController
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Molimo odaberite barem jednog člana",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Molimo ispunite sve podatke",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primary_color),
                            contentColor = colorResource(id = R.color.white)
                        )
                    ) {
                        Text("Stvori grupu")
                    }
                }
            }
        )
    }



    class AddGroupViewModel : ViewModel() {

        private val db = Firebase.firestore
        private val storageRef = Firebase.storage.reference

        fun uploadImageToFirebase(
            context: Context,
            imageUri: Uri,
            groupName: String,
            selectedUsers: List<String>,
            currentUserName: String,
            navController: NavController
        ) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(context, "Molimo prijavite se prije nego što pokušate uploadati slike", Toast.LENGTH_SHORT).show()
                return
            }

            val imageRef = storageRef.child("group_images/${System.currentTimeMillis()}.jpg")

            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    createGroup(
                        context = context,
                        navController = navController,
                        groupName = groupName,
                        groupImage = imageUrl,
                        memberUsernames = selectedUsers,
                        createdBy = currentUserName // Pass the current user's name
                    )
                } else {
                    Toast.makeText(context, "Greška pri prijenosu slike", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun createGroup(
            context: Context,
            navController: NavController,
            groupName: String,
            groupImage: String,
            memberUsernames: List<String>,
            createdBy: String
        ) {
            val allMembers = memberUsernames.toMutableSet()
            allMembers.add(createdBy) // Add the creator to the list of members

            val groupDocRef = db.collection("groups").document()
            val groupData = hashMapOf(
                "name" to groupName,
                "image" to groupImage,
                "members" to allMembers.toList(),
                "createdBy" to createdBy,
                "createdAt" to System.currentTimeMillis()
            )

            groupDocRef.set(groupData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Grupa uspješno kreirana", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("add") { inclusive = true }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Greška pri kreiranju grupe", Toast.LENGTH_SHORT).show()
                    Log.e("AddGroupViewModel", "Error creating group", exception)
                }
        }

        fun getAllUsersExceptCurrent(currentUsername: String, onResult: (List<String>) -> Unit) {
            db.collection("users").get()
                .addOnSuccessListener { result ->
                    val users = result.mapNotNull { document ->
                        val username = document.getString("username")
                        if (username != currentUsername) {
                            username
                        } else {
                            null
                        }
                    }
                    onResult(users)
                }
                .addOnFailureListener { exception ->
                    Log.e("AddGroupViewModel", "Error fetching users", exception)

                }
        }



        fun getCurrentUserName(onResult: (String) -> Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userDocRef = Firebase.firestore.collection("users").document(currentUser.uid)
                userDocRef.get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username") ?: "UnknownUser"
                        onResult(username)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AddGroupViewModel", "Error fetching user data", exception)
                        onResult("UnknownUser")
                    }
            } else {
                onResult("UnknownUser")
            }
        }

        fun checkIfUserIsLoggedIn(onResult: (Boolean) -> Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            onResult(currentUser != null)
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RemoveScreen(navController: NavController) {
        val context = LocalContext.current
        val groups = remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }
        val selectedGroups = remember { mutableStateOf<Set<String>>(emptySet()) }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Stanje za pohranu korisničkog imena
        val currentUserName = remember { mutableStateOf("") }

        // Dohvaćanje korisničkog imena koristeći Firebase Authentication
        LaunchedEffect(Unit) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userDocRef = db.collection("users").document(currentUser.uid)
                userDocRef.get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username") ?: "Nepoznati korisnik"
                        currentUserName.value = username

                        // Dohvaćanje grupa koje je stvorio trenutni korisnik
                        db.collection("groups")
                            .whereEqualTo("createdBy", username)
                            .get()
                            .addOnSuccessListener { documents ->
                                groups.value = documents.documents  // Postavljanje dohvaćenih grupa u stanje
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Greška s učitavanjem grupa", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Greška pri dohvaćanju korisničkog imena", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Niste prijavljeni", Toast.LENGTH_SHORT).show()

            }
        }


        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.navbar_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = colorResource(id = R.color.primary_color),
                        titleContentColor = colorResource(id = R.color.white)
                    ),
                    modifier = Modifier
                        .border(1.dp, colorResource(id = R.color.grey))
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colorResource(id = R.color.primary_color))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())  // Omogućuje skrolanje kroz listu grupa
                            .padding(bottom = 16.dp)  // Dodavanje razmaka na dnu kako bi se izbjeglo preklapanje s gumbom
                    ) {
                        if (groups.value.isEmpty()) {  // Provjera da li je lista grupa prazna
                            Text(
                                text = "Niste kreirali nijednu grupu",
                                color = colorResource(id = R.color.white),
                                fontSize = 18.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            groups.value.forEach { group ->  // Iteracija kroz svaku grupu
                                val groupId = group.id
                                val groupName = group.getString("name") ?: ""

                                GroupItem(
                                    groupName = groupName,
                                    isSelected = selectedGroups.value.contains(groupId),
                                    onSelectChange = { isSelected ->
                                        selectedGroups.value = if (isSelected) {
                                            selectedGroups.value + groupId  // Dodavanje ili uklanjanje grupe iz odabranih grupa
                                        } else {
                                            selectedGroups.value - groupId
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (selectedGroups.value.isNotEmpty()) {  // Provjera da li su neke grupe odabrane
                        Button(
                            onClick = {
                                deleteSelectedGroups(
                                    context = context,
                                    navController = navController,
                                    groupIds = selectedGroups.value.toList(),
                                    onDeleteSuccess = {
                                        selectedGroups.value = emptySet()  // Poništavanje odabranih grupa nakon brisanja
                                        groups.value = groups.value.filter { it.id !in selectedGroups.value }
                                    }
                                )
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.secondary_color),
                                contentColor = colorResource(id = R.color.white)
                            )
                        ) {
                            Text("Obriši odabrane grupe")
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun GroupItem(
        groupName: String,
        isSelected: Boolean = false,
        onSelectChange: (Boolean) -> Unit = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.grey),
                    shape = RectangleShape
                )
                .clickable { onSelectChange(!isSelected) }  // Omogućava klik na cijeli red za odabir ili poništavanje odabira
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = groupName,
                color = colorResource(id = R.color.white),
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Checkbox(
                checked = isSelected,  // Provjera je li stavka odabrana
                onCheckedChange = { onSelectChange(it) },  // Ažuriranje stanja odabira
                colors = CheckboxDefaults.colors(
                    checkedColor = colorResource(id = R.color.white),
                    uncheckedColor = colorResource(id = R.color.white)
                )
            )
        }
    }

    private fun deleteSelectedGroups(
        context: Context,
        navController: NavController,
        groupIds: List<String>,
        onDeleteSuccess: () -> Unit
    ) {
        val db = Firebase.firestore
        val batch = db.batch()

        groupIds.forEach { groupId ->
            val groupRef = db.collection("groups").document(groupId)
            batch.delete(groupRef)  // Priprema za brisanje svake odabrane grupe
        }

        batch.commit()  // Pokretanje batch operacije brisanja
            .addOnSuccessListener {
                Toast.makeText(context, "Odabrane grupe su uspješno obrisane", Toast.LENGTH_SHORT)
                    .show()
                onDeleteSuccess()  // Poziv funkcije za uspješno brisanje
                navController.navigate("home") {
                    popUpTo("remove") { inclusive = true }  // Vraćanje na početni ekran nakon brisanja
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Greška pri brisanju grupa", Toast.LENGTH_SHORT).show()
                Log.e("RemoveScreen", "Error deleting groups", exception)  // Prikaz greške u logu
            }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CompassScreen(navController: NavController) {
        val context = LocalContext.current
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotation = remember { mutableStateOf(0f) }  // Stanje za pohranu trenutne rotacije
        val gravity = remember { FloatArray(3) }  // Polje za pohranu podataka sa senzora akcelerometra
        val geomagnetic = remember { FloatArray(3) }  // Polje za pohranu podataka sa senzora magnetometra
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Postavljanje slušatelja senzora
        DisposableEffect(Unit) {
            val sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, event.values.size)  // Ažuriranje podataka sa akcelerometra
                        }

                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)  // Ažuriranje podataka sa magnetometra
                        }
                    }

                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)

                        var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                        // Osigurava da azimut bude između 0 i 359 stupnjeva
                        if (azimuth < 0) {
                            azimuth += 360
                        }

                        rotation.value = azimuth
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    // Nema implementacije
                }
            }

            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)  // Dohvaća senzor akcelerometra
            val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)  // Dohvaća senzor magnetometra

            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI  // Registrira senzor akcelerometra
            )
            sensorManager.registerListener(
                sensorEventListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI  // Registrira senzor magnetometra
            )

            onDispose {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }

        // Animacija vrijednosti rotacije za osiguranje glatkih prijelaza
        val animatedRotation by animateFloatAsState(
            targetValue = rotation.value,
            animationSpec = tween(
                durationMillis = 200,  // Trajanje animacije za glatke ili brže prijelaze
                easing = FastOutSlowInEasing
            )
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.navbar_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = colorResource(id = R.color.primary_color),
                        titleContentColor = colorResource(id = R.color.white)
                    ),
                    modifier = Modifier
                        .border(1.dp, colorResource(id = R.color.grey))
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colorResource(id = R.color.primary_color)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Prikazuje trenutni kut rotacije iznad slike kompasa
                        Text(
                            text = "${rotation.value.toInt()}°",
                            color = colorResource(id = R.color.white),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.compass_photo),
                            contentDescription = "Compass",
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer(rotationZ = -animatedRotation)  // Primjena glatke rotacije na sliku kompasa
                        )
                    }
                }
            }
        )
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ItemScreen(navController: NavController, groupName: String?) {
        val context = LocalContext.current
        val currentUserName = remember { mutableStateOf("") }
        val groupMembers = remember { mutableStateOf<List<String>>(emptyList()) }
        val groupExists = remember { mutableStateOf(false) }
        val groupImageUrl = remember { mutableStateOf<String?>(null) }
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val cameraPositionState = rememberCameraPositionState {
            // Početna pozicija kamere će se postaviti kasnije kada dobijemo lokaciju
        }

        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }  // Instanca za dohvaćanje lokacije
        val locationState = remember { mutableStateOf<Location?>(null) }  // Stanje za pohranu trenutne lokacije
        val isLocationShared = remember { mutableStateOf(false) }  // Stanje za praćenje statusa dijeljenja lokacije
        val sharedLocations = remember { mutableStateMapOf<String, UserLocation>() }  // Stanje za pohranu dijeljenih lokacija

        // Funkcija za ažuriranje pozicije kamere na temelju lokacije
        fun updateCameraPosition(location: Location) {
            val newCameraPosition = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude),
                12f
            )
            cameraPositionState.position = newCameraPosition
        }

        // Funkcija za zahtjev dozvole za pristup lokaciji
        @OptIn(ExperimentalPermissionsApi::class)
        @Composable
        fun RequestLocationPermission(
            onPermissionGranted: @Composable () -> Unit,
            onPermissionDenied: () -> Unit
        ) {
            val permissionState = rememberPermissionState(permission = ACCESS_FINE_LOCATION)

            // Pokretanje zahtjeva za dozvolu
            LaunchedEffect(Unit) {
                if (!permissionState.status.isGranted) {
                    permissionState.launchPermissionRequest()
                }
            }

            // Praćenje statusa dozvole
            when (permissionState.status) {
                is PermissionStatus.Granted -> onPermissionGranted()
                is PermissionStatus.Denied -> {
                    if ((permissionState.status as PermissionStatus.Denied).shouldShowRationale) {

                    }
                    onPermissionDenied()
                }
                else -> {}
            }
        }

        // Dohvaćanje korisničkog imena trenutnog korisnika iz Firebase Authentication
        LaunchedEffect(Unit) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userDocRef = db.collection("users").document(currentUser.uid)
                userDocRef.get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username") ?: "Nepoznati korisnik"
                        currentUserName.value = username
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Greška pri dohvaćanju korisničkog imena",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(context, "Niste prijavljeni", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }

        // Provjerava postoji li grupa i dohvaća članove i URL slike grupe
        LaunchedEffect(groupName) {
            groupName?.let { name ->
                val groupDocRef = db.collection("groups").whereEqualTo("name", name)
                groupDocRef.get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            groupExists.value = true
                            val groupDoc = documents.firstOrNull()
                            val members = groupDoc?.get("members") as? List<String> ?: emptyList()
                            groupMembers.value = members

                            val imageUrl = groupDoc?.getString("image")
                            groupImageUrl.value = imageUrl
                        } else {
                            groupExists.value = false
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Greška pri dohvaćanju podataka grupe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        // Zahtjev za dozvolu za pristup lokaciji i dohvaćanje trenutne lokacije
        RequestLocationPermission(
            onPermissionGranted = {
                LaunchedEffect(Unit) {
                    try {
                        val location = fusedLocationClient.lastLocation.await()
                        location?.let {
                            locationState.value = it
                            updateCameraPosition(it)
                        }
                    } catch (e: SecurityException) {
                        Toast.makeText(
                            context,
                            "Greška pri dohvaćanju lokacije",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onPermissionDenied = {
                // Obrada slučaja kada korisnik odbije dozvolu za pristup lokaciji
                Toast.makeText(context, "Dozvola za lokaciju je odbijena", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        // Funkcija za dijeljenje ili prestanak dijeljenja lokacije
        fun toggleLocationSharing() {
            val location = locationState.value
            val currentUser = auth.currentUser
            val username = currentUserName.value
            if (location != null && currentUser != null) {
                val groupDocRef = db.collection("groups").document(groupName ?: "")
                val userLocationRef = groupDocRef.collection("shared_locations").document(currentUser.uid)

                if (isLocationShared.value) {
                    // Uklanjanje lokacije iz "shared_locations"
                    userLocationRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Dijeljenje lokacije zaustavljeno",
                                Toast.LENGTH_SHORT
                            ).show()
                            isLocationShared.value = false
                            sharedLocations.remove(currentUser.uid) // Uklanjanje iz mape
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Greška pri uklanjanju lokacije",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Dodavanje lokacije u "shared_locations"
                    val locationData = hashMapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "username" to username
                    )

                    userLocationRef.set(locationData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Lokacija uspješno podijeljena",
                                Toast.LENGTH_SHORT
                            ).show()
                            isLocationShared.value = true
                            sharedLocations[currentUser.uid] = UserLocation(location.latitude, location.longitude, username)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Greška pri dijeljenju lokacije",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(
                    context,
                    "Nemoguće dijeliti lokaciju. Provjerite dozvole.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Provjerava status dijeljenja lokacije kada se stranica učita
        LaunchedEffect(groupName) {
            groupName?.let { name ->
                val groupDocRef = db.collection("groups").document(name)
                val currentUser = auth.currentUser

                currentUser?.let { user ->
                    val userLocationRef = groupDocRef.collection("shared_locations").document(user.uid)
                    userLocationRef.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                isLocationShared.value = true
                            } else {
                                isLocationShared.value = false
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Greška pri dohvaćanju statusa dijeljenja lokacije",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

        // Dohvaća sve dijeljene lokacije u grupi
        LaunchedEffect(groupName) {
            groupName?.let { name ->
                val groupDocRef = db.collection("groups").document(name)
                groupDocRef.collection("shared_locations").get()
                    .addOnSuccessListener { documents ->
                        val locationsMap = mutableMapOf<String, UserLocation>()
                        for (document in documents) {
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0
                            val username = document.getString("username") ?: "Nepoznati korisnik"
                            locationsMap[document.id] = UserLocation(latitude, longitude, username)
                        }
                        sharedLocations.putAll(locationsMap)
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Greška pri dohvaćanju dijeljenih lokacija",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        val borderColor = colorResource(id = R.color.grey)
        val primaryColor = colorResource(id = R.color.primary_color)
        val secondaryColor = colorResource(id = R.color.secondary_color)

        // Definiraj boju gumba na temelju statusa dijeljenja lokacije
        val buttonColor = if (isLocationShared.value) secondaryColor else primaryColor

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.navbar_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = primaryColor,
                        titleContentColor = Color.White
                    ),
                    modifier = Modifier
                        .border(1.dp, borderColor)
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryColor)
                        .padding(padding)
                ) {
                    // Informacije o grupi
                    groupName?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorResource(id = R.color.primary_color))
                                .padding(horizontal = 32.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier
                                        .size(30.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.back_arrow),
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Prikaz slike grupe ako je dostupna
                                groupImageUrl.value?.let { imageUrl ->
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    ) {
                                        val painter = rememberImagePainter(
                                            data = imageUrl,
                                            builder = {
                                                crossfade(true)
                                                placeholder(R.drawable.placeholder_image) // Zamijenite s vašom slikom za prikaz
                                            }
                                        )

                                        Image(
                                            painter = painter,
                                            contentDescription = "Group Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Prikaz imena grupe
                                Text(
                                    text = groupName,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }

                    // Google mapa
                    Box(
                        modifier = Modifier
                            .weight(1f) // Pusti mapu da zauzme preostali prostor
                            .fillMaxSize()
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = true),
                            uiSettings = MapUiSettings(compassEnabled = true)
                        ) {
                            // Prikaz markera za sve dijeljene lokacije
                            sharedLocations.values.forEach { userLocation ->
                                val location = LatLng(userLocation.latitude, userLocation.longitude)
                                val markerColor = if (userLocation.username == currentUserName.value) {
                                    BitmapDescriptorFactory.HUE_BLUE
                                } else {
                                    BitmapDescriptorFactory.HUE_GREEN
                                }
                                Marker(
                                    state = MarkerState(position = location),
                                    title = userLocation.username,
                                    snippet = if (userLocation.username == currentUserName.value) "Tvoja lokacija" else "Podijeljena lokacija",
                                    icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                                )
                            }
                        }

                        // Gumb za dijeljenje lokacije na dnu karte
                        Button(
                            onClick = { toggleLocationSharing() },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor), // Postavi boju gumba na temelju statusa
                            modifier = Modifier
                                .align(Alignment.BottomCenter) // Pozicioniranje gumba na dno karte
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (isLocationShared.value) "Zaustavi" else "Dijeli lokaciju",
                                color = Color.White 
                            )
                        }
                    }
                }
            }
        )
    }

    data class UserLocation(
        val latitude: Double,
        val longitude: Double,
        val username: String
    )



}

