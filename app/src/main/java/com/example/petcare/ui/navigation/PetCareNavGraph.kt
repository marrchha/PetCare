package com.example.petcare.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petcare.data.model.UserProfile
import com.example.petcare.ui.auth.AuthViewModel
import com.example.petcare.ui.auth.LoginScreen
import com.example.petcare.ui.auth.RegisterScreen
import com.example.petcare.ui.doctor.DoctorClinicListScreen
import com.example.petcare.ui.doctor.DoctorClinicViewModel
import com.example.petcare.ui.doctor.DoctorDetailScreen
import com.example.petcare.ui.doctor.DoctorFormScreen

object PetCareRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DOCTOR_CLINIC_LIST = "doctor_clinic_list"
    const val DOCTOR_DETAIL = "doctor_detail/{doctorId}"
    const val DOCTOR_FORM = "doctor_form?doctorId={doctorId}"

    fun doctorDetail(doctorId: String) = "doctor_detail/$doctorId"
    fun doctorForm(doctorId: String? = null) =
        if (doctorId != null) "doctor_form?doctorId=$doctorId" else "doctor_form"
}

@Composable
fun PetCareNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    doctorClinicViewModel: DoctorClinicViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = PetCareRoutes.LOGIN,
        modifier = modifier
    ) {
        composable(route = PetCareRoutes.LOGIN) {
            LoginScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onLoginClick = authViewModel::login,
                onFillDemoClick = authViewModel::fillDemoAccount,
                onNavigateToRegister = {
                    authViewModel.clearError()
                    navController.navigate(PetCareRoutes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(PetCareRoutes.HOME) {
                        popUpTo(PetCareRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(route = PetCareRoutes.REGISTER) {
            RegisterScreen(
                uiState = uiState,
                onFullNameChange = authViewModel::onFullNameChange,
                onEmailChange = authViewModel::onEmailChange,
                onPhoneNumberChange = authViewModel::onPhoneNumberChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                onRegisterClick = authViewModel::register,
                onNavigateBackToLogin = {
                    authViewModel.clearError()
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(PetCareRoutes.HOME) {
                        popUpTo(PetCareRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(route = PetCareRoutes.HOME) {
            HomePlaceholderScreen(
                currentUser = uiState.currentUser,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(PetCareRoutes.LOGIN) {
                        popUpTo(PetCareRoutes.HOME) { inclusive = true }
                    }
                },
                onNavigateToDoctorClinic = {
                    navController.navigate(PetCareRoutes.DOCTOR_CLINIC_LIST)
                }
            )
        }

        // ==========================================
        // ROUTES FITUR 2: DOKTER HEWAN & KLINIK
        // ==========================================

        composable(route = PetCareRoutes.DOCTOR_CLINIC_LIST) {
            DoctorClinicListScreen(
                viewModel = doctorClinicViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(PetCareRoutes.doctorDetail(doctorId))
                },
                onNavigateToAddDoctor = {
                    navController.navigate(PetCareRoutes.doctorForm(null))
                },
                onNavigateToEditDoctor = { doctorId ->
                    navController.navigate(PetCareRoutes.doctorForm(doctorId))
                }
            )
        }

        composable(
            route = PetCareRoutes.DOCTOR_DETAIL,
            arguments = listOf(
                navArgument("doctorId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            DoctorDetailScreen(
                doctorId = doctorId,
                viewModel = doctorClinicViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(PetCareRoutes.doctorForm(id))
                }
            )
        }

        composable(
            route = PetCareRoutes.DOCTOR_FORM,
            arguments = listOf(
                navArgument("doctorId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId")
            DoctorFormScreen(
                doctorId = doctorId,
                viewModel = doctorClinicViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private data class ModulePreviewItem(
    val title: String,
    val owner: String,
    val description: String,
    val icon: ImageVector,
    val useWarmAccent: Boolean = false,
    val isReady: Boolean = false
)

/**
 * Halaman Beranda (Home) PetCare — Dirancang persis dengan
 * nuansa layar tengah referensi ("For Buddy"):
 * - Header Sky Blue melengkung dengan pill search bar putih di dalamnya.
 * - Pill kategori hewan.
 * - Kartu-kartu modul proyek kelompok, di mana modul Dokter Hewan & Klinik
 *   sudah aktif dan dapat langsung dibuka.
 */
@Composable
private fun HomePlaceholderScreen(
    currentUser: UserProfile?,
    onLogoutClick: () -> Unit,
    onNavigateToDoctorClinic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        ModulePreviewItem(
            title = "Jadwal Vaksin & Kontrol",
            owner = "Sabitha Marsyarira Nayla",
            description = "Tab Akan Datang & Selesai",
            icon = Icons.Outlined.CalendarMonth,
            useWarmAccent = false,
            isReady = false
        ),
        ModulePreviewItem(
            title = "Dokter Hewan & Klinik",
            owner = "Andien Oktriarahmah S.",
            description = "Manajemen dokter hewan, jadwal & layanan klinik",
            icon = Icons.Outlined.LocalHospital,
            useWarmAccent = true,
            isReady = true // Fitur 2 aktif & siap diakses
        ),
        ModulePreviewItem(
            title = "Jadwal Makan Harian",
            owner = "Jeanete Arthika Lorentz R.",
            description = "Jam makan, jenis & porsi",
            icon = Icons.Outlined.Restaurant,
            useWarmAccent = false,
            isReady = false
        ),
        ModulePreviewItem(
            title = "Riwayat Kesehatan",
            owner = "Givenia Marudur Hasiana",
            description = "Filter Sakit, Obat & Operasi",
            icon = Icons.Outlined.MedicalServices,
            useWarmAccent = true,
            isReady = false
        ),
        ModulePreviewItem(
            title = "Data Profil Hewan",
            owner = "Alchalisa Fathinna",
            description = "Grid foto hewan & pencarian",
            icon = Icons.Outlined.Pets,
            useWarmAccent = false,
            isReady = false
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ===== HEADER SKY BLUE MELENGKUNG ALA "For Buddy" =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pemilik Hewan Aktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currentUser?.fullName ?: "Pengguna PetCare",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = "Notifikasi",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable(onClick = onLogoutClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "For Buddy",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pill Search Bar Putih Bersih di dalam Header Sky Blue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onNavigateToDoctorClinic)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cari jadwal, dokter hewan, atau klinik...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ===== KATEGORI PILL HITAM INK & PUTIH (FLAT) =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kategori Hewan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "5 Modul >",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Pill aktif hitam Ink
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kucing",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Pill non-aktif flat
                listOf("Anjing", "Kelinci").forEach { label ->
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fitur Utama Kelompok",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ketuk modul aktif",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== DAFTAR KARTU MODUL =====
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                modules.forEach { item ->
                    val isDoctorClinicModule = item.isReady
                    val cardBg = if (item.useWarmAccent) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                    val cardContent = if (item.useWarmAccent) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(cardBg)
                            .clickable(enabled = isDoctorClinicModule) {
                                if (isDoctorClinicModule) {
                                    onNavigateToDoctorClinic()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = cardContent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = cardContent
                                    )
                                    if (isDoctorClinicModule) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Aktif",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = item.owner,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cardContent.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cardContent.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Icon aksi di kanan
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDoctorClinicModule) Icons.AutoMirrored.Outlined.ArrowForward
                                else Icons.Outlined.Add,
                                contentDescription = if (isDoctorClinicModule) "Buka Modul" else "Detail",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
