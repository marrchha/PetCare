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
import com.example.petcare.ui.doctor.DoctorClinicListScreen
import com.example.petcare.ui.doctor.DoctorClinicViewModel
import com.example.petcare.ui.doctor.DoctorDetailScreen
import com.example.petcare.ui.doctor.DoctorFormScreen
import com.example.petcare.ui.feeding.FeedingDetailScreen
import com.example.petcare.ui.feeding.FeedingFormScreen
import com.example.petcare.ui.feeding.FeedingListScreen
import com.example.petcare.ui.feeding.FeedingViewModel
import com.example.petcare.ui.health.RiwayatKesehatanDetailScreen
import com.example.petcare.ui.health.RiwayatKesehatanFormScreen
import com.example.petcare.ui.health.RiwayatKesehatanListScreen
import com.example.petcare.ui.health.RiwayatKesehatanViewModel
import com.example.petcare.ui.jadwal.JadwalVaksinDetailScreen
import com.example.petcare.ui.jadwal.JadwalVaksinFormScreen
import com.example.petcare.ui.jadwal.JadwalVaksinListScreen
import com.example.petcare.ui.jadwal.JadwalVaksinViewModel
import com.example.petcare.ui.pet.PetDetailScreen
import com.example.petcare.ui.pet.PetFormScreen
import com.example.petcare.ui.pet.PetListScreen
import com.example.petcare.ui.pet.PetViewModel
import com.example.petcare.ui.profile.ProfileScreen

object PetCareRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"

    // Route Fitur 1: Jadwal Vaksin & Kontrol (Sabitha Marsyarira Nayla - 245150401111032)
    const val JADWAL_LIST = "jadwal_list"
    const val JADWAL_DETAIL = "jadwal_detail/{scheduleId}"
    const val JADWAL_FORM = "jadwal_form"

    fun jadwalDetailRoute(scheduleId: String) = "jadwal_detail/$scheduleId"

    // Route Fitur 2: Dokter Hewan & Klinik (Andien Oktriarahmah Syarifah - 245150400111046)
    const val DOCTOR_LIST = "doctor_list"
    const val DOCTOR_DETAIL = "doctor_detail/{doctorId}"
    const val DOCTOR_FORM = "doctor_form?doctorId={doctorId}"

    fun doctorDetailRoute(doctorId: String) = "doctor_detail/$doctorId"
    fun doctorFormRoute(doctorId: String? = null) =
        if (doctorId == null) "doctor_form" else "doctor_form?doctorId=$doctorId"

    // Route Fitur 3: Jadwal Makan Harian (Jeanete Arthika Lorentz R. - 245150400111038)
    const val FEEDING_LIST = "feeding_list"
    const val FEEDING_DETAIL = "feeding_detail/{feedingId}"
    const val FEEDING_FORM = "feeding_form?feedingId={feedingId}"

    fun feedingDetailRoute(feedingId: String) = "feeding_detail/$feedingId"
    fun feedingFormRoute(feedingId: String? = null) =
        if (feedingId == null) "feeding_form" else "feeding_form?feedingId=$feedingId"

    // Route Fitur 4: Riwayat Kesehatan (Givenia Marudur Hasiana - 245150400111051)
    const val HEALTH_LIST = "health_list"
    const val HEALTH_DETAIL = "health_detail/{recordId}"
    const val HEALTH_FORM = "health_form?recordId={recordId}"

    fun healthDetailRoute(recordId: String) = "health_detail/$recordId"
    fun healthFormRoute(recordId: String? = null) =
        if (recordId == null) "health_form" else "health_form?recordId=$recordId"

    // Route Fitur 5: Data Hewan (Alchalisa Fathinna - 245150407111066)
    const val PET_LIST = "pet_list"
    const val PET_DETAIL = "pet_detail/{petId}"
    const val PET_FORM = "pet_form?petId={petId}"

    fun petDetailRoute(petId: String) = "pet_detail/$petId"
    fun petFormRoute(petId: String? = null) =
        if (petId == null) "pet_form" else "pet_form?petId=$petId"
}

@Composable
fun PetCareNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    jadwalViewModel: JadwalVaksinViewModel = viewModel(),
    doctorViewModel: DoctorClinicViewModel = viewModel(),
    feedingViewModel: FeedingViewModel = viewModel(),
    healthViewModel: RiwayatKesehatanViewModel = viewModel(),
    petViewModel: PetViewModel = viewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val jadwalUiState by jadwalViewModel.uiState.collectAsState()
    val healthListUiState by healthViewModel.listUiState.collectAsState()
    val healthDetailUiState by healthViewModel.detailUiState.collectAsState()
    val healthFormUiState by healthViewModel.formUiState.collectAsState()
    val petUiState by petViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = PetCareRoutes.LOGIN,
        modifier = modifier
    ) {
        composable(route = PetCareRoutes.LOGIN) {
            LoginScreen(
                uiState = authUiState,
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
                uiState = authUiState,
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
                currentUser = authUiState.currentUser,
                onOpenProfileClick = {
                    navController.navigate(PetCareRoutes.PROFILE)
                },
                onOpenJadwalFeatureClick = {
                    navController.navigate(PetCareRoutes.JADWAL_LIST)
                },
                onOpenDoctorFeatureClick = {
                    navController.navigate(PetCareRoutes.DOCTOR_LIST)
                },
                onOpenFeedingFeatureClick = {
                    navController.navigate(PetCareRoutes.FEEDING_LIST)
                },
                onOpenHealthFeatureClick = {
                    navController.navigate(PetCareRoutes.HEALTH_LIST)
                },
                onOpenPetFeatureClick = {
                    navController.navigate(PetCareRoutes.PET_LIST)
                },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(PetCareRoutes.LOGIN) {
                        popUpTo(PetCareRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(route = PetCareRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(PetCareRoutes.LOGIN) {
                        popUpTo(PetCareRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // ===== FITUR 1: JADWAL VAKSIN & KONTROL (SABITHA) =====
        composable(route = PetCareRoutes.JADWAL_LIST) {
            JadwalVaksinListScreen(
                uiState = jadwalUiState,
                onSearchQueryChange = jadwalViewModel::onSearchQueryChange,
                onToggleStatus = jadwalViewModel::toggleScheduleStatus,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAdd = {
                    jadwalViewModel.initFormForAdd()
                    navController.navigate(PetCareRoutes.JADWAL_FORM)
                },
                onNavigateToDetail = { scheduleId ->
                    jadwalViewModel.selectSchedule(scheduleId)
                    navController.navigate(PetCareRoutes.jadwalDetailRoute(scheduleId))
                },
                onClearMessage = jadwalViewModel::clearMessage
            )
        }

        composable(
            route = PetCareRoutes.JADWAL_DETAIL,
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId").orEmpty()
            val selectedSchedule = jadwalUiState.schedules.find { it.id == scheduleId }
                ?: jadwalUiState.selectedSchedule

            JadwalVaksinDetailScreen(
                jadwal = selectedSchedule,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { schedule ->
                    jadwalViewModel.initFormForEdit(schedule)
                    navController.navigate(PetCareRoutes.JADWAL_FORM)
                },
                onToggleStatus = jadwalViewModel::toggleScheduleStatus,
                onDeleteSchedule = { id, onDone ->
                    jadwalViewModel.deleteSchedule(id, onDone)
                }
            )
        }

        composable(route = PetCareRoutes.JADWAL_FORM) {
            JadwalVaksinFormScreen(
                uiState = jadwalUiState,
                onNamaHewanChange = jadwalViewModel::onFormNamaHewanChange,
                onJenisChange = jadwalViewModel::onFormJenisChange,
                onKeperluanChange = jadwalViewModel::onFormKeperluanChange,
                onTanggalChange = jadwalViewModel::onFormTanggalChange,
                onJamChange = jadwalViewModel::onFormJamChange,
                onKlinikChange = jadwalViewModel::onFormKlinikChange,
                onCatatanChange = jadwalViewModel::onFormCatatanChange,
                onStatusChange = jadwalViewModel::onFormStatusChange,
                onSaveClick = {
                    jadwalViewModel.saveSchedule(authUiState.currentUser?.id) {
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ===== FITUR 2: DOKTER HEWAN & KLINIK (ANDIEN) =====
        composable(route = PetCareRoutes.DOCTOR_LIST) {
            DoctorClinicListScreen(
                viewModel = doctorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate(PetCareRoutes.doctorDetailRoute(doctorId))
                },
                onNavigateToAddDoctor = {
                    navController.navigate(PetCareRoutes.doctorFormRoute(null))
                },
                onNavigateToEditDoctor = { doctorId ->
                    navController.navigate(PetCareRoutes.doctorFormRoute(doctorId))
                }
            )
        }

        composable(
            route = PetCareRoutes.DOCTOR_DETAIL,
            arguments = listOf(
                navArgument("doctorId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId").orEmpty()
            DoctorDetailScreen(
                doctorId = doctorId,
                viewModel = doctorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { idToEdit ->
                    navController.navigate(PetCareRoutes.doctorFormRoute(idToEdit))
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
                viewModel = doctorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ===== FITUR 3: JADWAL MAKAN HARIAN (JEANETE) =====
        composable(route = PetCareRoutes.FEEDING_LIST) {
            FeedingListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAdd = {
                    feedingViewModel.prepareForm(null)
                    navController.navigate(PetCareRoutes.feedingFormRoute(null))
                },
                onNavigateToDetail = { feedingId ->
                    navController.navigate(PetCareRoutes.feedingDetailRoute(feedingId))
                },
                viewModel = feedingViewModel
            )
        }

        composable(
            route = PetCareRoutes.FEEDING_DETAIL,
            arguments = listOf(
                navArgument("feedingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val feedingId = backStackEntry.arguments?.getString("feedingId").orEmpty()
            FeedingDetailScreen(
                id = feedingId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    feedingViewModel.prepareForm(feedingId)
                    navController.navigate(PetCareRoutes.feedingFormRoute(feedingId))
                },
                viewModel = feedingViewModel
            )
        }

        composable(
            route = PetCareRoutes.FEEDING_FORM,
            arguments = listOf(
                navArgument("feedingId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            FeedingFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = feedingViewModel
            )
        }

        // ===== FITUR 4: RIWAYAT KESEHATAN (GIVENIA) =====
        composable(route = PetCareRoutes.HEALTH_LIST) {
            RiwayatKesehatanListScreen(
                uiState = healthListUiState,
                onFilterSelected = healthViewModel::onFilterChange,
                onSearchQueryChange = healthViewModel::onSearchQueryChange,
                onNavigateToDetail = { recordId ->
                    navController.navigate(PetCareRoutes.healthDetailRoute(recordId))
                },
                onNavigateToCreate = {
                    navController.navigate(PetCareRoutes.healthFormRoute(null))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = PetCareRoutes.HEALTH_DETAIL,
            arguments = listOf(
                navArgument("recordId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId").orEmpty()
            RiwayatKesehatanDetailScreen(
                recordId = recordId,
                uiState = healthDetailUiState,
                onLoadDetail = healthViewModel::loadDetail,
                onNavigateToEdit = { idToEdit ->
                    navController.navigate(PetCareRoutes.healthFormRoute(idToEdit))
                },
                onDeleteConfirm = { idToDelete ->
                    healthViewModel.deleteRecord(idToDelete) {
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = PetCareRoutes.HEALTH_FORM,
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
                uiState = healthFormUiState,
                onPrepareForm = { id ->
                    if (id.isNullOrBlank()) {
                        healthViewModel.prepareFormForCreate()
                    } else {
                        healthViewModel.prepareFormForEdit(id)
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

        // ===== FITUR 5: DATA HEWAN (ALCHALISA) =====
        composable(route = PetCareRoutes.PET_LIST) {
            PetListScreen(
                uiState = petUiState,
                onSearchQueryChange = petViewModel::onSearchQueryChange,
                onSpeciesFilterChange = petViewModel::onSpeciesFilterChange,
                onPetClick = { petId ->
                    navController.navigate(PetCareRoutes.petDetailRoute(petId))
                },
                onAddPetClick = {
                    petViewModel.prepareForm(null)
                    navController.navigate(PetCareRoutes.petFormRoute(null))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = PetCareRoutes.PET_DETAIL,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId").orEmpty()
            val selectedPet = petViewModel.getPetById(petId)

            PetDetailScreen(
                pet = selectedPet,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = { idToEdit ->
                    petViewModel.prepareForm(idToEdit)
                    navController.navigate(PetCareRoutes.petFormRoute(idToEdit))
                },
                onDeleteConfirm = { idToDelete ->
                    petViewModel.deletePet(idToDelete)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = PetCareRoutes.PET_FORM,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            PetFormScreen(
                uiState = petUiState,
                onNameChange = petViewModel::onFormNameChange,
                onSpeciesChange = petViewModel::onFormSpeciesChange,
                onBreedChange = petViewModel::onFormBreedChange,
                onGenderChange = petViewModel::onFormGenderChange,
                onAgeMonthsChange = petViewModel::onFormAgeMonthsChange,
                onWeightKgChange = petViewModel::onFormWeightKgChange,
                onColorChange = petViewModel::onFormColorChange,
                onPhotoUriChange = petViewModel::onFormPhotoUriChange,
                onNotesChange = petViewModel::onFormNotesChange,
                onSaveClick = {
                    val savedId = petViewModel.savePet()
                    if (savedId != null) {
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
    val useWarmAccent: Boolean = false,
    val onClick: (() -> Unit)? = null
)

@Composable
private fun HomePlaceholderScreen(
    currentUser: UserProfile?,
    onOpenProfileClick: () -> Unit,
    onOpenJadwalFeatureClick: () -> Unit,
    onOpenDoctorFeatureClick: () -> Unit,
    onOpenFeedingFeatureClick: () -> Unit,
    onOpenHealthFeatureClick: () -> Unit,
    onOpenPetFeatureClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        ModulePreviewItem(
            title = "Jadwal Vaksin & Kontrol",
            owner = "Sabitha Marsyarira Nayla",
            description = "Tab Akan Datang & Selesai",
            icon = Icons.Outlined.CalendarMonth,
            useWarmAccent = false,
            onClick = onOpenJadwalFeatureClick
        ),
        ModulePreviewItem(
            title = "Dokter Hewan & Klinik",
            owner = "Andien Oktriarahmah S.",
            description = "Konsultasi & Riwayat Kontak",
            icon = Icons.Outlined.LocalHospital,
            useWarmAccent = true,
            onClick = onOpenDoctorFeatureClick
        ),
        ModulePreviewItem(
            title = "Jadwal Makan Harian",
            owner = "Jeanete Arthika Lorentz R.",
            description = "Jam makan, jenis & porsi",
            icon = Icons.Outlined.Restaurant,
            useWarmAccent = false,
            onClick = onOpenFeedingFeatureClick
        ),
        ModulePreviewItem(
            title = "Riwayat Kesehatan",
            owner = "Givenia Marudur Hasiana",
            description = "Filter Sakit, Obat & Operasi",
            icon = Icons.Outlined.MedicalServices,
            useWarmAccent = true,
            onClick = onOpenHealthFeatureClick
        ),
        ModulePreviewItem(
            title = "Data Profil Hewan",
            owner = "Alchalisa Fathinna",
            description = "Grid foto hewan & pencarian",
            icon = Icons.Outlined.Pets,
            useWarmAccent = false,
            onClick = onOpenPetFeatureClick
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
                    Row(
                        modifier = Modifier.clickable(onClick = onOpenProfileClick),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

            Text(
                text = "Fitur Utama Kelompok",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                modules.forEach { item ->
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
                                if (item.onClick != null) {
                                    Modifier.clickable(onClick = item.onClick)
                                } else {
                                    Modifier
                                }
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

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
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
