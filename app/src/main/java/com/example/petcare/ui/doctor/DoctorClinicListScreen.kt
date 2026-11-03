package com.example.petcare.ui.doctor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddBusiness
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.ClinicService
import com.example.petcare.data.model.Doctor
import com.example.petcare.ui.theme.PetCareTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
@Composable
fun DoctorClinicListScreen(
    viewModel: DoctorClinicViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDoctorDetail: (String) -> Unit,
    onNavigateToAddDoctor: () -> Unit,
    onNavigateToEditDoctor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    DoctorClinicListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSelectClinic = viewModel::selectClinic,
        onToggleClinicFilter = viewModel::toggleClinicFilter,
        onSaveClinicInfo = { id, name, address, phone, hours, emergency, desc ->
            viewModel.saveClinic(id, name, address, phone, hours, emergency, desc) {}
        },
        onDeleteClinic = viewModel::deleteClinic,
        onSaveService = { id, name, category, price, duration, desc ->
            viewModel.saveService(id, name, category, price, duration, desc) {}
        },
        onDeleteService = viewModel::deleteService,
        onNavigateBack = onNavigateBack,
        onNavigateToDoctorDetail = onNavigateToDoctorDetail,
        onNavigateToAddDoctor = onNavigateToAddDoctor,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorClinicListContent(
    uiState: DoctorClinicUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSearchQueryChange: (String) -> Unit = {},
    onSelectClinic: (String) -> Unit = {},
    onToggleClinicFilter: () -> Unit = {},
    onSaveClinicInfo: (String?, String, String, String, String, Boolean, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onDeleteClinic: (String) -> Unit = {},
    onSaveService: (String?, String, String, Long, Int, String) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteService: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToDoctorDetail: (String) -> Unit = {},
    onNavigateToAddDoctor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf("Dokter Praktik", "Layanan Klinik")
    val pagerState = rememberPagerState(initialPage = uiState.selectedTab) { tabs.size }

    // multi klinik dan layanan
    var clinicToEdit by remember { mutableStateOf<ClinicInfo?>(null) }
    var showClinicDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ClinicService?>(null) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<ClinicService?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dokter Hewan & Klinik",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${uiState.clinics.size} Cabang Klinik Terdaftar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali ke Beranda"
                        )
                    }
                },
                actions = {
                    // tambah klinik baru
                    IconButton(onClick = {
                        clinicToEdit = null
                        showClinicDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.AddBusiness,
                            contentDescription = "Tambah Cabang Klinik",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddDoctor,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Tambah Dokter", fontWeight = FontWeight.Bold) }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = {
                        serviceToEdit = null
                        showServiceDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Tambah Layanan", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //pilih cabang
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.clinics, key = { it.id }) { clinic ->
                    val isSelected = clinic.id == uiState.selectedClinicId
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .clickable { onSelectClinic(clinic.id) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = clinic.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                clinicToEdit = null
                                showClinicDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cabang Baru",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            //card cabang
            uiState.selectedClinic?.let { activeClinic ->
                ClinicInfoHeaderCard(
                    clinicInfo = activeClinic,
                    onEditClick = {
                        clinicToEdit = activeClinic
                        showClinicDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            //searchbar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        if (pagerState.currentPage == 0) "Cari dokter, spesialisasi, atau cabang..."
                        else "Cari nama layanan medis..."
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

           //row dokter layanan
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // filter cepat per cabang klinik saat di Tab Dokter
            if (pagerState.currentPage == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.filterBySelectedClinicOnly) "Menampilkan dokter di cabang ini"
                        else "Menampilkan semua dokter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (uiState.filterBySelectedClinicOnly) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .clickable { onToggleClinicFilter() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.filterBySelectedClinicOnly) "Cabang ini Saja" else "Semua Cabang",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        // Dokter pakr LazyColumn Kartu Dokter dengan Foto
                        DoctorListTab(
                            doctors = uiState.filteredDoctors,
                            onDoctorClick = onNavigateToDoctorDetail
                        )
                    }
                    1 -> {
                        // Layann pake LazyColumn Layanan Klinik
                        ServiceListTab(
                            services = uiState.filteredServices,
                            onEditService = { service ->
                                serviceToEdit = service
                                showServiceDialog = true
                            },
                            onDeleteService = { service ->
                                serviceToDelete = service
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit klinijk
    if (showClinicDialog) {
        ClinicInfoEditDialog(
            clinicInfo = clinicToEdit,
            onDismiss = { showClinicDialog = false },
            onSave = { id, name, address, phone, hours, emergency, desc ->
                onSaveClinicInfo(id, name, address, phone, hours, emergency, desc)
                showClinicDialog = false
            }
        )
    }

    // edit layanan
    if (showServiceDialog) {
        ServiceFormDialog(
            service = serviceToEdit,
            onDismiss = { showServiceDialog = false },
            onSave = { id, name, category, price, duration, desc ->
                onSaveService(id, name, category, price, duration, desc)
                showServiceDialog = false
            }
        )
    }

    // hapus layanan
    if (serviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = {
                Text(
                    text = "Hapus Layanan Klinik?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus layanan \"${serviceToDelete?.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        serviceToDelete?.let { onDeleteService(it.id) }
                        serviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}


@Composable
private fun ClinicInfoHeaderCard(
    clinicInfo: ClinicInfo,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = clinicInfo.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (clinicInfo.emergencyAvailable) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Emergency,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "UGD 24 Jam",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Info Klinik",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = clinicInfo.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = clinicInfo.operationalHours,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = clinicInfo.phoneNumber,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}


@Composable
private fun DoctorListTab(
    doctors: List<Doctor>,
    onDoctorClick: (String) -> Unit
) {
    if (doctors.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada dokter yang sesuai kriteria pencarian / filter cabang.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(doctors, key = { it.id }) { doctor ->
                DoctorListItemCard(
                    doctor = doctor,
                    onClick = { onDoctorClick(doctor.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

//dokter
@Composable
private fun DoctorListItemCard(
    doctor: Doctor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // tampilan dokter
            DoctorPhotoThumbnail(
                photoUri = doctor.photoUri,
                avatarIndex = doctor.avatarIndex,
                size = 54.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Badge Status Aktif/Libur
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (doctor.isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (doctor.isActive) "Aktif" else "Libur",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = doctor.specialization,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Lokasi Cabang Klinik
                Text(
                    text = doctor.clinicName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${doctor.practiceDays} • ${doctor.practiceHours}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Lihat Detail",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

//lazy kolom layanan klinik
@Composable
private fun ServiceListTab(
    services: List<ClinicService>,
    onEditService: (ClinicService) -> Unit,
    onDeleteService: (ClinicService) -> Unit
) {
    if (services.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada layanan klinik yang terdaftar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services, key = { it.id }) { service ->
                ServiceListItemCard(
                    service = service,
                    onEdit = { onEditService(service) },
                    onDelete = { onDeleteService(service) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

//card item layansn klnik
@Composable
private fun ServiceListItemCard(
    service: ClinicService,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rupiahFormat = remember {
        val localeId = Locale.Builder().setLanguage("in").setRegion("ID").build()
        NumberFormat.getCurrencyInstance(localeId).apply {
            maximumFractionDigits = 0
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = service.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Layanan",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Hapus Layanan",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = service.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rupiahFormat.format(service.estimatedPrice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (service.durationMinutes >= 60) "${service.durationMinutes / 60} Jam"
                        else "${service.durationMinutes} Menit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


@Preview(name = "Doctor Clinic List - Multi Clinic", showBackground = true)
@Preview(
    name = "Doctor Clinic List - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun DoctorClinicListPreview() {
    PetCareTheme {
        DoctorClinicListContent(
            uiState = DoctorClinicUiState(
                clinics = listOf(
                    ClinicInfo(
                        id = "c1",
                        name = "PetCare Central Vet",
                        address = "Jl. Bendungan Sigura-gura No. 24, Malang",
                        phoneNumber = "(0341) 567-890",
                        operationalHours = "08:00 - 21:00 WIB",
                        emergencyAvailable = true
                    ),
                    ClinicInfo(
                        id = "c2",
                        name = "PetCare Cabang Suhat",
                        address = "Jl. Soekarno Hatta No. 88, Malang",
                        phoneNumber = "(0341) 489-112",
                        operationalHours = "09:00 - 20:00 WIB",
                        emergencyAvailable = false
                    )
                ),
                selectedClinicId = "c1",
                doctors = listOf(
                    Doctor(
                        name = "drh. Sarah Wijayanti, M.Si",
                        specialization = "Bedah & Penyakit Dalam",
                        practiceDays = "Senin - Kamis",
                        practiceHours = "09:00 - 15:00",
                        phoneContact = "0812-3456-7891",
                        clinicName = "PetCare Central Vet",
                        avatarIndex = 1,
                        isActive = true
                    ),
                    Doctor(
                        name = "drh. Raditya Pratama",
                        specialization = "Gigi & Ortopedi Hewan",
                        practiceDays = "Senin - Sabtu",
                        practiceHours = "13:00 - 19:00",
                        phoneContact = "0813-8822-1100",
                        clinicName = "PetCare Cabang Suhat",
                        avatarIndex = 2,
                        isActive = false
                    )
                ),
                services = listOf(
                    ClinicService(
                        name = "Vaksinasi Lengkap (F3 / F4)",
                        description = "Pemberian vaksin pencegah feline panleukopenia.",
                        estimatedPrice = 180000,
                        durationMinutes = 20,
                        category = "Pencegahan"
                    )
                )
            )
        )
    }
}
