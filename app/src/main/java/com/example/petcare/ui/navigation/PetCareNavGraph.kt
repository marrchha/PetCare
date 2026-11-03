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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
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
import com.example.petcare.ui.health.RiwayatKesehatanDetailScreen
import com.example.petcare.ui.health.RiwayatKesehatanFormScreen
import com.example.petcare.ui.health.RiwayatKesehatanListScreen
import com.example.petcare.ui.health.RiwayatKesehatanViewModel

object PetCareRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val RIWAYAT_KESEHATAN_LIST = "riwayat_kesehatan_list"
    const val RIWAYAT_KESEHATAN_DETAIL = "riwayat_kesehatan_detail/{recordId}"
    const val RIWAYAT_KESEHATAN_FORM = "riwayat_kesehatan_form?recordId={recordId}"

    fun createDetailRoute(recordId: String): String = "riwayat_kesehatan_detail/$recordId"
    fun createFormRoute(recordId: String? = null): String =
        if (recordId != null) "riwayat_kesehatan_form?recordId=$recordId" else "riwayat_kesehatan_form"
}

@Composable
fun PetCareNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    healthViewModel: RiwayatKesehatanViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val healthListState by healthViewModel.listUiState.collectAsState()
    val healthDetailState by healthViewModel.detailUiState.collectAsState()
    val healthFormState by healthViewModel.formUiState.collectAsState()

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
                onNavigateToRiwayatKesehatan = {
                    navController.navigate(PetCareRoutes.RIWAYAT_KESEHATAN_LIST)
                },
                onNavigateToRiwayatKesehatanForm = {
                    healthViewModel.prepareFormForCreate()
                    navController.navigate(PetCareRoutes.createFormRoute())
                }
            )
        }

        // ===== FITUR 4: RIWAYAT KESEHATAN (GIVENIA MARUDUR HASIANA) =====
        composable(route = PetCareRoutes.RIWAYAT_KESEHATAN_LIST) {
            RiwayatKesehatanListScreen(
                uiState = healthListState,
                onFilterSelected = healthViewModel::onFilterChange,
                onSearchQueryChange = healthViewModel::onSearchQueryChange,
                onNavigateToDetail = { recordId ->
                    navController.navigate(PetCareRoutes.createDetailRoute(recordId))
                },
                onNavigateToCreate = {
                    healthViewModel.prepareFormForCreate()
                    navController.navigate(PetCareRoutes.createFormRoute())
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = PetCareRoutes.RIWAYAT_KESEHATAN_DETAIL,
            arguments = listOf(
                navArgument("recordId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId").orEmpty()
            RiwayatKesehatanDetailScreen(
                recordId = recordId,
                uiState = healthDetailState,
                onLoadDetail = healthViewModel::loadDetail,
                onNavigateToEdit = { editId ->
                    healthViewModel.prepareFormForEdit(editId)
                    navController.navigate(PetCareRoutes.createFormRoute(editId))
                },
                onDeleteConfirm = { deleteId ->
                    healthViewModel.deleteRecord(deleteId) {
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = PetCareRoutes.RIWAYAT_KESEHATAN_FORM,
            arguments = listOf(
                navArgument("recordId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId")
            RiwayatKesehatanFormScreen(
                recordId = recordId,
                uiState = healthFormState,
                onPrepareForm = { id ->
                    if (id != null) {
                        healthViewModel.prepareFormForEdit(id)
                    } else {
                        healthViewModel.prepareFormForCreate()
                    }
                },
                onPetNameChange = healthViewModel::onPetNameChange,
                onPetTypeChange = healthViewModel::onPetTypeChange,
                onJenisChange = healthViewModel::onJenisChange,
                onJudulChange = healthViewModel::onJudulChange,
                onTanggalChange = healthViewModel::onTanggalChange,
                onDokterChange = healthViewModel::onDokterChange,
                onCatatanMedisChange = healthViewModel::onCatatanMedisChange,
                onBiayaChange = healthViewModel::onBiayaChange,
                onStatusPemulihanChange = healthViewModel::onStatusPemulihanChange,
                onSaveClick = {
                    healthViewModel.saveForm {
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private data class ModulePreviewItem(
    val title: String,
    val owner: String,
    val description: String,
    val icon: ImageVector,
    val useWarmAccent: Boolean = false
)

/**
 * Halaman Home setelah berhasil Login:
 * - Tampilan visual 100% konsisten persis aslinya (tanpa perubahan layout/badge tambahan).
 * - Kartu Riwayat Kesehatan (Givenia Marudur Hasiana) dapat diklik untuk membuka modul Riwayat Kesehatan.
 * - Tombol `+` pada kartu Riwayat Kesehatan dapat diklik untuk membuka formulir input baru.
 */
@Composable
private fun HomePlaceholderScreen(
    currentUser: UserProfile?,
    onLogoutClick: () -> Unit,
    onNavigateToRiwayatKesehatan: () -> Unit = {},
    onNavigateToRiwayatKesehatanForm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        ModulePreviewItem(
            title = "Jadwal Vaksin & Kontrol",
            owner = "Sabitha Marsyarira Nayla",
            description = "Tab Akan Datang & Selesai",
            icon = Icons.Outlined.CalendarMonth,
            useWarmAccent = false
        ),
        ModulePreviewItem(
            title = "Dokter Hewan & Klinik",
            owner = "Andien Oktriarahmah S.",
            description = "Konsultasi & Riwayat Kontak",
            icon = Icons.Outlined.LocalHospital,
            useWarmAccent = true
        ),
        ModulePreviewItem(
            title = "Jadwal Makan Harian",
            owner = "Jeanete Arthika Lorentz R.",
            description = "Jam makan, jenis & porsi",
            icon = Icons.Outlined.Restaurant,
            useWarmAccent = false
        ),
        ModulePreviewItem(
            title = "Riwayat Kesehatan",
            owner = "Givenia Marudur Hasiana",
            description = "Filter Sakit, Obat & Operasi",
            icon = Icons.Outlined.MedicalServices,
            useWarmAccent = true
        ),
        ModulePreviewItem(
            title = "Data Profil Hewan",
            owner = "Alchalisa Fathinna",
            description = "Grid foto hewan & pencarian",
            icon = Icons.Outlined.Pets,
            useWarmAccent = false
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
                        text = "Cari jadwal, dokter hewan, atau riwayat...",
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

            Text(
                text = "Fitur Utama Kelompok",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ===== DAFTAR KARTU PASTEL FLAT (LAVENDER & PEACH TANPA SHADOW) =====
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                modules.forEach { item ->
                    val isHealthFeature = item.title == "Riwayat Kesehatan"

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
                            .then(
                                if (isHealthFeature) {
                                    Modifier.clickable(onClick = onNavigateToRiwayatKesehatan)
                                } else Modifier
                            )
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
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = cardContent
                                )
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

                        // Lingkaran putih `+` persis seperti kartu produk di referensi
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .then(
                                    if (isHealthFeature) {
                                        Modifier.clickable(onClick = onNavigateToRiwayatKesehatanForm)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
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
