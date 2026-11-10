package com.example.petcare.ui.doctor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.Doctor
import com.example.petcare.ui.theme.PetCareTheme
import kotlinx.coroutines.launch

@Composable
fun DoctorClinicListScreen(
    viewModel: DoctorClinicViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDoctorDetail: (String) -> Unit,
    onNavigateToAddDoctor: () -> Unit,
    onNavigateToEditDoctor: (String) -> Unit,
    onNavigateToClinicDetail: (String) -> Unit = {},
    onNavigateToAddClinic: () -> Unit = {},
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
        onTabChange = viewModel::onTabSelected,
        onDoctorSearchChange = viewModel::onDoctorSearchChange,
        onOpenDoctorFilterSheet = viewModel::openDoctorFilterSheet,
        onDismissDoctorFilterSheet = viewModel::dismissDoctorFilterSheet,
        onTempDoctorClinicFilterChange = viewModel::onTempDoctorClinicFilterChange,
        onTempDoctorSpecFilterChange = viewModel::onTempDoctorSpecFilterChange,
        onTempDoctorStatusFilterChange = viewModel::onTempDoctorStatusFilterChange,
        onTempDoctorDayFilterChange = viewModel::onTempDoctorDayFilterChange,
        onResetDoctorFilters = viewModel::resetDoctorFilters,
        onApplyDoctorFilters = viewModel::applyDoctorFilters,
        onDeleteDoctor = { id -> viewModel.deleteDoctor(id) },
        onClinicSearchChange = viewModel::onClinicSearchChange,
        onClinicStatusFilterChange = viewModel::onClinicStatusFilterChange,
        onNavigateBack = onNavigateBack,
        onNavigateToDoctorDetail = onNavigateToDoctorDetail,
        onNavigateToAddDoctor = onNavigateToAddDoctor,
        onNavigateToEditDoctor = onNavigateToEditDoctor,
        onNavigateToClinicDetail = onNavigateToClinicDetail,
        onNavigateToAddClinic = onNavigateToAddClinic,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorClinicListContent(
    uiState: DoctorClinicUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onTabChange: (Int) -> Unit = {},
    onDoctorSearchChange: (String) -> Unit = {},
    onOpenDoctorFilterSheet: () -> Unit = {},
    onDismissDoctorFilterSheet: () -> Unit = {},
    onTempDoctorClinicFilterChange: (String) -> Unit = {},
    onTempDoctorSpecFilterChange: (String) -> Unit = {},
    onTempDoctorStatusFilterChange: (String) -> Unit = {},
    onTempDoctorDayFilterChange: (String) -> Unit = {},
    onResetDoctorFilters: () -> Unit = {},
    onApplyDoctorFilters: () -> Unit = {},
    onDeleteDoctor: (String) -> Unit = {},
    onClinicSearchChange: (String) -> Unit = {},
    onClinicStatusFilterChange: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToDoctorDetail: (String) -> Unit = {},
    onNavigateToAddDoctor: () -> Unit = {},
    onNavigateToEditDoctor: (String) -> Unit = {},
    onNavigateToClinicDetail: (String) -> Unit = {},
    onNavigateToAddClinic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTab,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.selectedTab) {
        if (pagerState.currentPage != uiState.selectedTab) {
            pagerState.animateScrollToPage(uiState.selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (uiState.selectedTab != pagerState.currentPage) {
            onTabChange(pagerState.currentPage)
        }
    }

    var doctorToDelete by remember { mutableStateOf<Doctor?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddDoctor,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = "Tambah Dokter"
                        )
                    },
                    text = {
                        Text(
                            text = "Tambah Dokter",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddClinic,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Tambah Klinik"
                        )
                    },
                    text = {
                        Text(
                            text = "Tambah Klinik",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // A. Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manajemen Dokter & Klinik",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Kelola seluruh cabang klinik, jadwal dokter, dan layanan Petcare",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // B. Ringkasan (Total Klinik, Total Dokter, Dokter Aktif)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CentralSummaryCard(
                    icon = Icons.Outlined.LocalHospital,
                    countValue = uiState.totalClinicsCount.toString(),
                    label = "Klinik",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                CentralSummaryCard(
                    icon = Icons.Outlined.Person,
                    countValue = uiState.totalDoctorsCount.toString(),
                    label = "Dokter",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                CentralSummaryCard(
                    icon = Icons.Outlined.CheckCircle,
                    countValue = uiState.activeDoctorsCount.toString(),
                    label = "Dokter Aktif",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // C. Tab Navigasi (1. Dokter | 2. Klinik & Layanan)
            val tabs = listOf("Dokter", "Klinik & Layanan")
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            onTabChange(index)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page == 0) {
                    DoctorsTabSection(
                        uiState = uiState,
                        onSearchChange = onDoctorSearchChange,
                        onOpenFilterSheet = onOpenDoctorFilterSheet,
                        onResetFilters = onResetDoctorFilters,
                        onDoctorClick = onNavigateToDoctorDetail,
                        onEditDoctor = onNavigateToEditDoctor,
                        onRequestDeleteDoctor = { doc -> doctorToDelete = doc }
                    )
                } else {
                    ClinicsTabSection(
                        uiState = uiState,
                        onSearchChange = onClinicSearchChange,
                        onStatusFilterChange = onClinicStatusFilterChange,
                        onClinicClick = onNavigateToClinicDetail
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet Filter Dokter
    if (uiState.isDoctorFilterSheetOpen) {
        DoctorFilterBottomSheet(
            clinics = uiState.clinics,
            selectedClinicId = uiState.tempFilterDoctorClinicId,
            selectedSpecialization = uiState.tempFilterDoctorSpecialization,
            selectedStatus = uiState.tempFilterDoctorStatus,
            selectedDay = uiState.tempFilterDoctorDay,
            onSelectClinic = onTempDoctorClinicFilterChange,
            onSelectSpecialization = onTempDoctorSpecFilterChange,
            onSelectStatus = onTempDoctorStatusFilterChange,
            onSelectDay = onTempDoctorDayFilterChange,
            onReset = onResetDoctorFilters,
            onApply = onApplyDoctorFilters,
            onDismiss = onDismissDoctorFilterSheet
        )
    }

    // Dialog Konfirmasi Hapus Dokter
    doctorToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { doctorToDelete = null },
            title = {
                Text(
                    text = "Hapus Dokter?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus data \"${doc.name}\"? Aksi ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDoctor(doc.id)
                        doctorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { doctorToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CentralSummaryCard(
    icon: ImageVector,
    countValue: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = countValue,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DoctorsTabSection(
    uiState: DoctorClinicUiState,
    onSearchChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onResetFilters: () -> Unit,
    onDoctorClick: (String) -> Unit,
    onEditDoctor: (String) -> Unit,
    onRequestDeleteDoctor: (Doctor) -> Unit
) {
    val doctors = uiState.filteredDoctors
    val activeFilterCount = uiState.activeDoctorFilterCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.doctorSearchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Cari nama dokter atau spesialisasi...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.doctorSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Bersihkan",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Tombol Filter dengan Indikator Jumlah Filter Aktif
                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(activeFilterCount.toString())
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (activeFilterCount > 0) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            )
                            .clickable(onClick = onOpenFilterSheet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter Dokter",
                            tint = if (activeFilterCount > 0) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        if (activeFilterCount > 0) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$activeFilterCount filter aktif diterapkan (${doctors.size} dokter)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Reset Filter",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onResetFilters)
                    )
                }
            }
        }

        if (doctors.isEmpty()) {
            item {
                EmptyStateBox(
                    title = "Dokter Tidak Ditemukan",
                    subtitle = "Tidak ada dokter yang sesuai dengan kata kunci pencarian atau kombinasi filter yang dipilih.",
                    showResetButton = activeFilterCount > 0 || uiState.doctorSearchQuery.isNotBlank(),
                    onReset = {
                        onSearchChange("")
                        onResetFilters()
                    }
                )
            }
        } else {
            items(doctors, key = { it.id }) { doctor ->
                DoctorListItemCard(
                    doctor = doctor,
                    onClick = { onDoctorClick(doctor.id) },
                    onEdit = { onEditDoctor(doctor.id) },
                    onDelete = { onRequestDeleteDoctor(doctor) }
                )
            }
        }
    }
}

@Composable
private fun DoctorListItemCard(
    doctor: Doctor,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Ikon profil dokter sebagai pengganti foto
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Ikon Dokter",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadgePill(isActive = doctor.isActive)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = doctor.specialization,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nama klinik tempat dokter bertugas
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = doctor.clinicName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Hari dan jam praktik
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${doctor.practiceDays} • ${doctor.practiceHours}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ketuk kartu untuk detail lengkap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Dokter",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Hapus Dokter",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicsTabSection(
    uiState: DoctorClinicUiState,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onClinicClick: (String) -> Unit
) {
    val clinics = uiState.filteredClinics
    val statusOptions = listOf("Semua", "Aktif", "Nonaktif")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.clinicSearchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Cari nama klinik atau alamat...",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.clinicSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Bersihkan",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter status klinik (Semua, Aktif, Nonaktif)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { status ->
                    val selected = uiState.filterClinicStatus == status
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusFilterChange(status) },
                        label = {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        if (clinics.isEmpty()) {
            item {
                EmptyStateBox(
                    title = "Klinik Tidak Ditemukan",
                    subtitle = "Tidak ada cabang klinik yang sesuai dengan pencarian atau filter status.",
                    showResetButton = uiState.clinicSearchQuery.isNotBlank() || uiState.filterClinicStatus != "Semua",
                    onReset = {
                        onSearchChange("")
                        onStatusFilterChange("Semua")
                    }
                )
            }
        } else {
            items(clinics, key = { it.id }) { clinic ->
                val doctorCount = uiState.doctorCountForClinic(clinic.id)
                ClinicBranchCard(
                    clinic = clinic,
                    doctorCount = doctorCount,
                    onClick = { onClinicClick(clinic.id) }
                )
            }
        }
    }
}

@Composable
private fun ClinicBranchCard(
    clinic: ClinicInfo,
    doctorCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon klinik (bukan foto)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalHospital,
                    contentDescription = "Ikon Klinik",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = clinic.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadgePill(isActive = clinic.isActive)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = clinic.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = clinic.operationalHours,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$doctorCount Dokter Terdaftar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Buka Detail Klinik",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadgePill(isActive: Boolean) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isActive) "Aktif" else "Nonaktif",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DoctorFilterBottomSheet(
    clinics: List<ClinicInfo>,
    selectedClinicId: String,
    selectedSpecialization: String,
    selectedStatus: String,
    selectedDay: String,
    onSelectClinic: (String) -> Unit,
    onSelectSpecialization: (String) -> Unit,
    onSelectStatus: (String) -> Unit,
    onSelectDay: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val specializations = listOf("Semua", "Dokter Umum", "Bedah", "Kulit", "Gigi")
    val statuses = listOf("Semua", "Aktif", "Nonaktif")
    val days = listOf("Semua", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Daftar Dokter",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onReset) {
                    Text("Reset")
                }
            }

            // A. Klinik
            Text(
                text = "Klinik Cabang",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedClinicId == "Semua",
                    onClick = { onSelectClinic("Semua") },
                    label = { Text("Semua Klinik") },
                    shape = RoundedCornerShape(50)
                )
                clinics.forEach { clinic ->
                    FilterChip(
                        selected = selectedClinicId == clinic.id,
                        onClick = { onSelectClinic(clinic.id) },
                        label = { Text(clinic.name) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // B. Spesialisasi
            Text(
                text = "Spesialisasi",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                specializations.forEach { spec ->
                    FilterChip(
                        selected = selectedSpecialization == spec,
                        onClick = { onSelectSpecialization(spec) },
                        label = {
                            Text(if (spec == "Semua") "Semua Spesialisasi" else spec)
                        },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // C. Status
            Text(
                text = "Status Praktik",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onSelectStatus(status) },
                        label = { Text(status) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // D. Hari Praktik
            Text(
                text = "Hari Praktik",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { onSelectDay(day) },
                        label = {
                            Text(if (day == "Semua") "Semua Hari" else day)
                        },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Terapkan Filter",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EmptyStateBox(
    title: String,
    subtitle: String,
    showResetButton: Boolean,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showResetButton) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Reset Pencarian & Filter")
            }
        }
    }
}

@Preview(name = "Doctor Clinic List - Light", showBackground = true)
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
                selectedTab = 1,
                clinics = listOf(
                    ClinicInfo(
                        id = "clinic-malang",
                        name = "Petcare Malang",
                        address = "Jl. Bendungan Sigura-gura No. 24, Sukun, Kota Malang",
                        operationalHours = "Senin - Minggu: 08:00 - 21:00 WIB",
                        isActive = true
                    ),
                    ClinicInfo(
                        id = "clinic-surabaya",
                        name = "Petcare Surabaya",
                        address = "Jl. Raya Darmo No. 45, Wonokromo, Kota Surabaya",
                        operationalHours = "Senin - Minggu: 08:00 - 22:00 WIB",
                        isActive = true
                    ),
                    ClinicInfo(
                        id = "clinic-batu",
                        name = "Petcare Batu",
                        address = "Jl. Panglima Sudirman No. 12, Kota Batu",
                        operationalHours = "Senin - Sabtu: 09:00 - 18:00 WIB",
                        isActive = false
                    )
                ),
                doctors = listOf(
                    Doctor(
                        id = "d1",
                        name = "drh. Andi Prasetya",
                        specialization = "Dokter Umum",
                        practiceDays = "Senin, Selasa, Rabu, Kamis",
                        practiceHours = "08:00 - 14:00",
                        phoneContact = "0812-3456-0001",
                        clinicId = "clinic-malang",
                        clinicName = "Petcare Malang",
                        isActive = true
                    )
                )
            )
        )
    }
}
