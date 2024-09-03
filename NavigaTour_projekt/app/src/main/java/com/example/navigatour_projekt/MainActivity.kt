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
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current

            // Provjerite stanje prijave iz SharedPreferences
            val isLoggedIn = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                isLoggedIn.value = sharedPref.getBoolean("is_logged_in", false)
            }

            NavigaTour_projektTheme {
                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn.value) "home" else "main"
                ) {
                    composable("main") { MainScreen(navController = navController) }
                    composable("login") { LoginScreen(navController = navController) }
                    composable("register") { RegistrationScreen(navController = navController) }
                    composable("home") { HomeScreen(navController = navController) }
                    composable("profile") { ProfileScreen(navController = navController) }
                    composable("add") { backStackEntry ->
                        val viewModel: AddGroupViewModel =
                            viewModel() // Koristite viewModel() za pružanje ViewModel
                        AddScreen(navController, viewModel)
                    }
                    composable("remove_groups") { RemoveScreen(navController = navController) }
                    composable("compass") { CompassScreen(navController = navController) }
                    composable("item/{groupName}") { backStackEntry ->
                        val groupName = backStackEntry.arguments?.getString("groupName")
                        if (groupName != null) {
                            // Koristite postojeći navController, nemojte kreirati novi
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
                            // Ako je uneseni tekst korisničko ime, najprije pronađi e-mail
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
                    // Prijava je uspješna, dohvatite podatke o korisniku iz Firestore
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let {
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(it.uid!!)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Ako su podaci uspješno dohvaćeni, nastavite s navigacijom
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
                    // Došlo je do greške prilikom prijave
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
                                navController.popBackStack() // Navigates back after registration
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
        val groups = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // State to store the current user's name
        val currentUserName = remember { mutableStateOf("") }

        // Fetch the current user's name using Firebase Authentication
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
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Greška pri dohvaćanju korisničkog imena", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Niste prijavljeni", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch groups where the current user is a member
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
                            val groupName = document.getString("name") ?: ""
                            val imageUrl = document.getString("image") ?: ""
                            groupList.add(Pair(groupName, imageUrl))
                        }
                        groups.value = groupList
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Greška s učitavanjem grupa", Toast.LENGTH_SHORT).show()
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
                ) {
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

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(groups.value) { (groupName, imageUrl) ->
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
        val painter = rememberImagePainter(
            data = imageUrl,
            builder = {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
            }
        )

        val borderColor = colorResource(id = R.color.grey)

        // Use a Box to draw the border across the entire screen width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 1.dp.toPx() // Maintain the standard border thickness
                    val y = size.height - strokeWidth / 2 // Draw line at the very bottom
                    drawLine(
                        color = borderColor,
                        start = Offset(-32.dp.toPx(), y), // Extend the line beyond the start of padding
                        end = Offset(size.width + 32.dp.toPx(), y), // Extend the line beyond the end of padding
                        strokeWidth = strokeWidth
                    )
                }
                .clickable {
                    navController.navigate("item/$groupName")
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
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

                Spacer(modifier = Modifier.width(32.dp))

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
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavIcon(
                iconId = R.drawable.home_outlined,
                activeIconId = R.drawable.home_focused,
                contentDescription = "home",
                navController = navController,
                route = "home",
                currentRoute = currentRoute
            )
            BottomNavIcon(
                iconId = R.drawable.remove_group_outlined,
                activeIconId = R.drawable.remove_group_focused,
                contentDescription = "remove_groups",
                navController = navController,
                route = "remove_groups",
                currentRoute = currentRoute
            )
            BottomNavIcon(
                iconId = R.drawable.add,
                activeIconId = R.drawable.add,
                contentDescription = "add",
                navController = navController,
                route = "add",
                currentRoute = currentRoute,
                size = 50.dp
            )
            BottomNavIcon(
                iconId = R.drawable.compass_outlined,
                activeIconId = R.drawable.compass_focused,
                contentDescription = "compass",
                navController = navController,
                route = "compass",
                currentRoute = currentRoute
            )
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
        size: Dp = 24.dp // Default size for icons
    ) {
        val isActive = route == currentRoute
        val iconPainter = painterResource(id = if (isActive) activeIconId else iconId)
        val iconColor =
            if (isActive) colorResource(id = R.color.secondary_color) else colorResource(id = R.color.primary_color)

        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .clickable { navController.navigate(route) },
            tint = iconColor
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

    fun saveLoginState(context: Context, isLoggedIn: Boolean) {
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
                            // Update the list of users
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

        fun createGroup(
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
                    // Handle failure, perhaps inform the user
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

        // State to store the current user's name
        val currentUserName = remember { mutableStateOf("") }

        // Fetch the current user's name using Firebase Authentication
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

                        // Fetch groups created by the current user
                        db.collection("groups")
                            .whereEqualTo("createdBy", username)
                            .get()
                            .addOnSuccessListener { documents ->
                                groups.value = documents.documents
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
                // Handle user not logged in, perhaps navigate to login screen
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
                    verticalArrangement = Arrangement.SpaceBetween // Spacing between items
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()) // Make the group list scrollable
                            .padding(bottom = 16.dp) // Padding to avoid overlap with the button
                    ) {
                        if (groups.value.isEmpty()) {
                            Text(
                                text = "Niste kreirali nijednu grupu",
                                color = colorResource(id = R.color.white),
                                fontSize = 18.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            groups.value.forEach { group ->
                                val groupId = group.id
                                val groupName = group.getString("name") ?: ""

                                GroupItem(
                                    groupName = groupName,
                                    isSelected = selectedGroups.value.contains(groupId),
                                    onSelectChange = { isSelected ->
                                        selectedGroups.value = if (isSelected) {
                                            selectedGroups.value + groupId
                                        } else {
                                            selectedGroups.value - groupId
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (selectedGroups.value.isNotEmpty()) {
                        Button(
                            onClick = {
                                deleteSelectedGroups(
                                    context = context,
                                    navController = navController,
                                    groupIds = selectedGroups.value.toList(),
                                    onDeleteSuccess = {
                                        selectedGroups.value = emptySet()
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
                .padding(vertical = 8.dp) // Adjusted padding for spacing
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.grey),
                    shape = RectangleShape
                )
                .clickable { onSelectChange(!isSelected) }
                .padding(horizontal = 16.dp, vertical = 12.dp) // Adjusted padding for content
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Spacer to push content to the left

            Text(
                text = groupName,
                color = colorResource(id = R.color.white),
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(16.dp)) // Space between text and checkbox

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = colorResource(id = R.color.white),
                    uncheckedColor = colorResource(id = R.color.white)
                )
            )
        }
    }

    fun deleteSelectedGroups(
        context: Context,
        navController: NavController,
        groupIds: List<String>,
        onDeleteSuccess: () -> Unit
    ) {
        val db = Firebase.firestore
        val batch = db.batch()

        groupIds.forEach { groupId ->
            val groupRef = db.collection("groups").document(groupId)
            batch.delete(groupRef)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "Odabrane grupe su uspješno obrisane", Toast.LENGTH_SHORT)
                    .show()
                onDeleteSuccess()
                navController.navigate("home") {
                    popUpTo("remove") { inclusive = true }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Greška pri brisanju grupa", Toast.LENGTH_SHORT).show()
                Log.e("RemoveScreen", "Error deleting groups", exception)
            }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CompassScreen(navController: NavController) {
        val context = LocalContext.current
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotation = remember { mutableStateOf(0f) }
        val gravity = remember { FloatArray(3) }
        val geomagnetic = remember { FloatArray(3) }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Setup sensor listener
        DisposableEffect(Unit) {
            val sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                        }

                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                        }
                    }

                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        // orientation[0] is the azimuth (rotation around the Z axis)
                        var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                        // Ensure azimuth is between 0 and 359 degrees
                        if (azimuth < 0) {
                            azimuth += 360
                        }

                        rotation.value = azimuth
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    // No implementation needed
                }
            }

            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                sensorEventListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }

        // Animate the rotation value to ensure smooth transitions
        val animatedRotation by animateFloatAsState(
            targetValue = rotation.value,
            animationSpec = tween(
                durationMillis = 200, // Adjust the duration for smoother or quicker animations
                easing = FastOutSlowInEasing // Easing function for natural movement
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
                        // Display the current rotation angle above the compass image
                        Text(
                            text = "${rotation.value.toInt()}°",
                            color = colorResource(id = R.color.white),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 16.dp) // Adjust padding as needed
                        )

                        Image(
                            painter = painterResource(id = R.drawable.compass_photo),
                            contentDescription = "Compass",
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer(rotationZ = -animatedRotation) // Apply smooth rotation to the compass image
                        )
                    }
                }
            }
        )
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ItemScreen(navController: NavController, groupName: String) {
        val context = LocalContext.current
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

        // Stanja za podatke o grupi, URL slike grupe, trenutnog korisnika i lokacija korisnika
        val groupDetails = remember { mutableStateOf<Map<String, Any>?>(null) }
        val groupImageUrl = remember { mutableStateOf<String?>(null) }
        val currentUser = remember { mutableStateOf(auth.currentUser) }
        val currentUserName = remember { mutableStateOf("") }
        val userLocations = remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }

        // Stanja za dijeljenje lokacije
        val isSharingLocation = remember { mutableStateOf(false) }
        val isLocationSharedInGroup = remember { mutableStateOf(false) }
        val locationState = remember { mutableStateOf<Location?>(null) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 10f) // Zadana pozicija
        }

        // Sanitizacija imena grupe i trenutnog korisnika
        val sanitizedGroupName = groupName.trim()
        val sanitizedCurrentUserName = currentUserName.value.trim()

        // Provjeri postoji li grupa i da li je trenutni korisnik dijeli lokaciju
        LaunchedEffect(sanitizedGroupName) {
            val groupDocRef = db.collection("groups").document(sanitizedGroupName)

            // Dohvati podatke o grupi
            groupDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    groupDetails.value = documentSnapshot.data
                    groupImageUrl.value = documentSnapshot.getString("image") // Dohvati URL slike

                    // Dohvati dijeljene lokacije
                    groupDocRef.collection("shared_locations").get().addOnSuccessListener { querySnapshot ->
                        userLocations.value = querySnapshot.documents.mapNotNull { document ->
                            val latitude = document.getDouble("latitude")
                            val longitude = document.getDouble("longitude")
                            val user = document.getString("username")
                            if (latitude != null && longitude != null && user != null && user != sanitizedCurrentUserName) {
                                user to LatLng(latitude, longitude)
                            } else {
                                null
                            }
                        }.toMap()
                    }

                    // Provjeri je li lokacija trenutnog korisnika dijeljena
                    groupDocRef.collection("shared_locations").document(sanitizedCurrentUserName).get().addOnSuccessListener { document ->
                        isLocationSharedInGroup.value = document.exists()
                        isSharingLocation.value = isLocationSharedInGroup.value
                    }
                }
            }
        }

        // Dohvati ime trenutnog korisnika
        LaunchedEffect(currentUser.value) {
            currentUser.value?.let { user ->
                val userDocRef = db.collection("users").document(user.uid)
                userDocRef.get().addOnSuccessListener { document ->
                    currentUserName.value = document.getString("username") ?: "Nepoznati korisnik"
                }
            }
        }

        // Ažuriraj stanje korisnika ako se promijeni stanje autentifikacije
        DisposableEffect(auth) {
            val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUser.value = firebaseAuth.currentUser
            }
            auth.addAuthStateListener(authListener)
            onDispose {
                auth.removeAuthStateListener(authListener)
            }
        }

        // Funkcija za ažuriranje pozicije kamere
        fun updateCameraPosition(location: Location) {
            val newCameraPosition = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude),
                12f
            )
            cameraPositionState.position = newCameraPosition
        }

        // Funkcija za početak dijeljenja lokacije
        fun startSharingLocation() {
            locationState.value?.let { location ->
                val locationData = mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "username" to sanitizedCurrentUserName
                )
                db.collection("groups").document(sanitizedGroupName)
                    .collection("shared_locations")
                    .document(sanitizedCurrentUserName)
                    .set(locationData)
                    .addOnSuccessListener {
                        isSharingLocation.value = true
                    }
            }
        }

        // Funkcija za prestanak dijeljenja lokacije
        fun stopSharingLocation() {
            db.collection("groups").document(sanitizedGroupName)
                .collection("shared_locations")
                .document(sanitizedCurrentUserName)
                .delete()
                .addOnSuccessListener {
                    isSharingLocation.value = false
                    userLocations.value = userLocations.value - sanitizedCurrentUserName
                }
        }

        // Periodična ažuriranja lokacije dok je dijeljena
        DisposableEffect(isSharingLocation.value) {
            var job: Job? = null

            if (isSharingLocation.value) {
                job = CoroutineScope(Dispatchers.IO).launch {
                    while (isSharingLocation.value) {
                        locationState.value?.let { location ->
                            val locationData = mapOf(
                                "latitude" to location.latitude,
                                "longitude" to location.longitude,
                                "username" to sanitizedCurrentUserName
                            )
                            db.collection("groups").document(sanitizedGroupName)
                                .collection("shared_locations")
                                .document(sanitizedCurrentUserName)
                                .set(locationData)
                                .await()
                        }
                        delay(5000) // Ažuriraj svake 5 sekundi
                    }
                }
            }

            onDispose {
                job?.cancel()
            }
        }

        // Funkcija za zahtjev dozvole za lokaciju
        RequestLocationPermission(
            onPermissionGranted = {
                LaunchedEffect(Unit) {
                    try {
                        val location = fusedLocationClient.lastLocation.await()
                        locationState.value = location
                        location?.let { updateCameraPosition(it) }
                    } catch (e: SecurityException) {
                        // Obradi iznimku ako dozvole za lokaciju nisu dodijeljene
                    }
                }
            },
            onPermissionDenied = {
                // Obradi slučaj kada korisnik odbije dozvolu za lokaciju
            }
        )

        val borderColor = colorResource(id = R.color.grey)

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
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(id = R.color.primary_color))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(id = R.color.primary_color))
                            .border(BorderStroke(1.dp, colorResource(id = R.color.grey)))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.back_arrow),
                                contentDescription = "Povratak",
                                tint = colorResource(id = R.color.white),
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Slika grupe ili zamjenski prikaz
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Gray) // Rezervna boja u slučaju problema s učitavanjem
                        ) {
                            groupImageUrl.value?.let { imageUrl ->
                                Image(
                                    painter = rememberImagePainter(imageUrl),
                                    contentDescription = "Slika grupe",
                                    contentScale = ContentScale.Crop, // Obreži da odgovara krugu
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Ime grupe
                        Text(
                            text = sanitizedGroupName,
                            color = colorResource(id = R.color.white),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(compassEnabled = true)
                    ) {
                        // Dodaj oznake za lokacije korisnika
                        userLocations.value.forEach { (user, position) ->
                            Marker(
                                state = MarkerState(position = position),
                                title = user,
                                snippet = "Lokacija $user",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                        }

                        // Prikaži plavu oznaku samo ako se dijeli lokacija
                        if (isSharingLocation.value) {
                            locationState.value?.let { location ->
                                Marker(
                                    state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                                    title = sanitizedCurrentUserName,
                                    snippet = "Vaša lokacija",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    if (isSharingLocation.value) {
                                        stopSharingLocation()
                                    } else {
                                        startSharingLocation()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSharingLocation.value)
                                        colorResource(id = R.color.secondary_color)
                                    else
                                        colorResource(id = R.color.primary_color),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.toggleable(
                                    value = isSharingLocation.value,
                                    onValueChange = {
                                        if (it) {
                                            startSharingLocation()
                                        } else {
                                            stopSharingLocation()
                                        }
                                    }
                                )
                            ) {
                                Text(text = if (isSharingLocation.value) "Prekini dijeljenje" else "Dijeli lokaciju")
                            }
                        }
                    }
                }
            }
        }
    }

    // Funkcija za zahtjev dozvole za lokaciju
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestLocationPermission(
        onPermissionGranted: @Composable () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        val permissionState = rememberPermissionState(permission = ACCESS_FINE_LOCATION)

        // Pokreni zahtjev za dozvolu
        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }

        // Prati status dozvole
        when (permissionState.status) {
            is PermissionStatus.Granted -> onPermissionGranted()
            is PermissionStatus.Denied -> {
                if ((permissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                    // Možda želite prikazati obrazloženje korisniku ovdje
                }
                onPermissionDenied()
            }
            else -> {} // Obradi druge statuse ako je potrebno
        }
    }


}

