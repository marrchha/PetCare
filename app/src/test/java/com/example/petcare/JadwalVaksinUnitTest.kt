package com.example.petcare

import com.example.petcare.data.model.JadwalVaksin
import com.example.petcare.data.repository.JadwalVaksinRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class JadwalVaksinUnitTest {

    private lateinit var repository: JadwalVaksinRepository

    @Before
    fun setUp() {
        repository = JadwalVaksinRepository()
    }

    @Test
    fun testJadwalVaksinModelProperties() {
        val jadwalAkanDatang = JadwalVaksin(
            id = "test-1",
            namaHewan = "Mimi",
            status = JadwalVaksin.STATUS_AKAN_DATANG
        )
        assertFalse(jadwalAkanDatang.isCompleted)

        val jadwalSelesai = JadwalVaksin(
            id = "test-2",
            namaHewan = "Rocky",
            status = JadwalVaksin.STATUS_SELESAI
        )
        assertTrue(jadwalSelesai.isCompleted)
    }

    @Test
    fun testRepositoryCrudFlow() = runBlocking {
        val initialList = repository.getJadwalList("user-demo-001").getOrThrow()
        assertTrue(initialList.isNotEmpty())


        val newId = UUID.randomUUID().toString()
        val testJadwal = JadwalVaksin(
            id = newId,
            userId = "user-demo-001",
            namaHewan = "Luna (Kucing)",
            jenis = JadwalVaksin.JENIS_VAKSIN,
            keperluan = "Vaksin Rabies",
            tanggal = "2026-11-20",
            jam = "10:00",
            klinik = "Klinik Satwa Sehat",
            catatan = "Puasa sebelum tindakan",
            status = JadwalVaksin.STATUS_AKAN_DATANG
        )
        val insertResult = repository.insertJadwal(testJadwal)
        assertTrue(insertResult.isSuccess)

        val afterInsert = repository.getJadwalById(newId).getOrThrow()
        assertNotNull(afterInsert)
        assertEquals("Luna (Kucing)", afterInsert?.namaHewan)


        val toggleResult = repository.toggleStatus(newId)
        assertTrue(toggleResult.isSuccess)
        val toggled = repository.getJadwalById(newId).getOrThrow()
        assertEquals(JadwalVaksin.STATUS_SELESAI, toggled?.status)


        val updatedSchedule = toggled!!.copy(
            klinik = "Klinik Hewan Modern",
            catatan = "Catatan telah diupdate"
        )
        val updateResult = repository.updateJadwal(updatedSchedule)
        assertTrue(updateResult.isSuccess)
        val afterUpdate = repository.getJadwalById(newId).getOrThrow()
        assertEquals("Klinik Hewan Modern", afterUpdate?.klinik)
        assertEquals("Catatan telah diupdate", afterUpdate?.catatan)


        val deleteResult = repository.deleteJadwal(newId)
        assertTrue(deleteResult.isSuccess)
        val afterDelete = repository.getJadwalById(newId).getOrThrow()
        assertNull(afterDelete)
    }
}
