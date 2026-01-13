package com.example.beanflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
// Import Visibility dihapus karena bikin error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.data.model.User
import com.example.beanflow.ui.viewmodel.UserViewModel

// --- WARNA TEMA KHUSUS USER MANAGEMENT ---
private val UserManCoffeeDark = Color(0xFF4E342E)
private val UserManCoffeeMedium = Color(0xFF795548)
private val UserManCream = Color(0xFFFFF3E0)
private val UserManBackground = Brush.verticalGradient(listOf(UserManCream, Color.White))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(viewModel: UserViewModel, onBack: () -> Unit) {
    val userList by viewModel.userList.collectAsState()

    // State untuk Dialog Tambah
    var showAddDialog by remember { mutableStateOf(false) }

    // State untuk Dialog Edit (Menyimpan user yang sedang diklik)
    var userToEdit by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Kasir", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UserManCoffeeDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = UserManCoffeeDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kasir")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(UserManBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (userList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada data kasir.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(userList) { user ->
                            if (user.role == "Cashier") {
                                UserItem(
                                    user = user,
                                    onDelete = { viewModel.deleteUser(user) },
                                    onClick = { userToEdit = user }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- DIALOG TAMBAH USER ---
        if (showAddDialog) {
            UserDialog(
                title = "Tambah Kasir Baru",
                initialUsername = "",
                initialPassword = "",
                confirmButtonText = "Simpan",
                onDismiss = { showAddDialog = false },
                onConfirm = { username, password ->
                    val newUser = User(username = username, password = password, role = "Cashier")
                    viewModel.addUser(newUser)
                    showAddDialog = false
                }
            )
        }

        // --- DIALOG EDIT USER ---
        if (userToEdit != null) {
            UserDialog(
                title = "Edit Data Kasir",
                initialUsername = userToEdit!!.username,
                initialPassword = userToEdit!!.password,
                confirmButtonText = "Update",
                onDismiss = { userToEdit = null },
                onConfirm = { newUsername, newPassword ->
                    // Copy data user lama (ID tetap sama), update isinya
                    val updatedUser = userToEdit!!.copy(
                        username = newUsername,
                        password = newPassword
                    )
                    viewModel.updateUser(updatedUser)
                    userToEdit = null
                }
            )
        }
    }
}

// Komponen Kartu User
@Composable
fun UserItem(user: User, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(UserManCream),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = UserManCoffeeMedium)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = user.username, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UserManCoffeeDark)
                    Text(text = "Tap untuk lihat/edit", fontSize = 10.sp, color = Color.Gray)
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Delete, "Hapus", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// Dialog Reusable (FIXED: Ganti Icon Mata dengan Teks "LIHAT")
@Composable
fun UserDialog(
    title: String,
    initialUsername: String,
    initialPassword: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf(initialPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = UserManCoffeeDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UserManCoffeeDark,
                        focusedLabelColor = UserManCoffeeDark,
                        cursorColor = UserManCoffeeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Field (PERBAIKAN DI SINI)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    // GANTI ICON DENGAN TEXT BUTTON AGAR TIDAK ERROR
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "SEMBUNYIKAN" else "LIHAT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = UserManCoffeeMedium
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UserManCoffeeDark,
                        focusedLabelColor = UserManCoffeeDark,
                        cursorColor = UserManCoffeeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username, password) },
                colors = ButtonDefaults.buttonColors(containerColor = UserManCoffeeDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}