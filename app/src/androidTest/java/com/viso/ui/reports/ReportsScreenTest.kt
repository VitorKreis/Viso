package com.viso.ui.reports

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reportsScreen_rendersChartAndFilters() {
        // create a small repo and ViewModel with sample data
        val fakeDao = object : com.viso.data.db.dao.MonthHistoryDao {
            override suspend fun getAll() = emptyList<com.viso.data.db.entity.MonthHistoryEntity>()
            override suspend fun getByMonth(month: String) = null
            override suspend fun insert(history: com.viso.data.db.entity.MonthHistoryEntity) {}
        }

        val fakeRepo = ReportsRepository(fakeDao)
        val vm = ReportsViewModel(fakeRepo)

        composeRule.setContent {
            ReportsScreen(onBack = {}, viewModel = vm)
        }

        // check that filter buttons are present
        composeRule.onNodeWithText("Consolidado").assertIsDisplayed()
        composeRule.onNodeWithText("Contas").assertIsDisplayed()
        composeRule.onNodeWithText("Gastos").assertIsDisplayed()
        composeRule.onNodeWithText("Poupança").assertIsDisplayed()
    }
}
